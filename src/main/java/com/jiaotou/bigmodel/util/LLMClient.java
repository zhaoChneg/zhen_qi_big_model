package com.jiaotou.bigmodel.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiaotou.bigmodel.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 大模型调用客户端（兼容 OpenAI API 格式）
 * 支持：通义千问、智谱GLM、文心一言、私有化部署等任何兼容 OpenAI 格式的服务
 */
@Slf4j
@Component
public class LLMClient {

    @Value("${llm.base-url}")
    private String baseUrl;

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.max-tokens:4096}")
    private int maxTokens;

    @Value("${llm.temperature:0.7}")
    private double temperature;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 普通同步调用
     */
    public String chat(String userMessage) {
        return chat(List.of(Map.of("role", "user", "content", userMessage)));
    }

    public String chat(List<Map<String, String>> messages) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", messages,
                    "max_tokens", maxTokens,
                    "temperature", temperature,
                    "stream", false
            ));
            Request request = buildRequest(body);
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new BusinessException("LLM调用失败：" + response.code());
                }
                JsonNode root = objectMapper.readTree(response.body().string());
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM调用异常", e);
            throw new BusinessException("AI服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 流式调用（SSE）
     * @param messages  对话上下文
     * @param onChunk   每个文本块回调
     * @param onDone    完成回调
     * @param onError   错误回调
     */
    public void streamChat(List<Map<String, String>> messages,
                           Consumer<String> onChunk,
                           Runnable onDone,
                           Consumer<Throwable> onError) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", messages,
                    "max_tokens", maxTokens,
                    "temperature", temperature,
                    "stream", true
            ));
            Request request = buildRequest(body);
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    log.error("LLM流式请求失败", e);
                    onError.accept(e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        onError.accept(new BusinessException("LLM调用失败：" + response.code()));
                        return;
                    }
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) {
                                    onDone.run();
                                    break;
                                }
                                try {
                                    JsonNode root = objectMapper.readTree(data);
                                    JsonNode delta = root.path("choices").get(0).path("delta");
                                    String content = delta.path("content").asText("");
                                    if (!content.isEmpty()) {
                                        onChunk.accept(content);
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    } catch (Exception e) {
                        log.error("SSE读取异常", e);
                        onError.accept(e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("LLM流式调用构建失败", e);
            onError.accept(e);
        }
    }

    private Request buildRequest(String jsonBody) {
        return new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                .build();
    }
}
