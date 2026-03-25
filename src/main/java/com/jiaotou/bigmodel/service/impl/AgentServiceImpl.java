package com.jiaotou.bigmodel.service.impl;

import com.jiaotou.bigmodel.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentServiceImpl {

    private final LLMClient llmClient;

    // 工具提示词配置
    private static final Map<String, String> TOOL_PROMPTS = Map.of(
        "file-compare",
            "你是一位专业的文件比对专家。请仔细比较以下两份文件内容的差异，" +
            "按照【新增内容】【删除内容】【修改内容】三个维度逐条列出差异，并给出总体评估。",
        "file-review",
            "你是一位资深的政务公文校审专家。请对以下文件进行全面校审，" +
            "重点检查：措辞是否准确规范、逻辑是否严密、标点是否正确、格式是否符合政务文件规范。" +
            "请逐项列出问题并给出修改建议。",
        "compliance-check",
            "你是一位政策法规合规专家。请对以下内容进行合规性分析，" +
            "识别潜在的合规风险，引用相关法规条款，给出合规评级（高/中/低风险）和整改建议。",
        "meeting-minutes",
            "你是一位专业的会议纪要整理专家。请将以下会议记录整理为标准格式的会议纪要，" +
            "包含：会议基本信息、议题讨论要点、形成的决议/决定、后续行动计划（责任人+时间节点）。",
        "data-report",
            "你是一位数据分析报告撰写专家。请根据以下数据，撰写一份专业的数据分析报告，" +
            "包含：数据概况、趋势分析、关键发现、结论与建议，格式规范，语言精炼。"
    );

    @Async("sseExecutor")
    public void runTool(String toolId, Map<String, Object> inputs, SseEmitter emitter) {
        String systemPrompt = TOOL_PROMPTS.getOrDefault(toolId,
                "你是一位专业的AI助手，请根据输入完成任务。");
        String userContent = buildUserContent(toolId, inputs);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userContent)
        );

        try {
            llmClient.streamChat(messages,
                chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data("data: " + chunk));
                    } catch (IOException e) {
                        log.warn("Agent SSE发送失败", e);
                    }
                },
                () -> {
                    try {
                        emitter.send(SseEmitter.event().data("data: [DONE]"));
                        emitter.complete();
                    } catch (IOException e) {
                        log.warn("Agent SSE完成失败", e);
                    }
                },
                error -> {
                    log.error("Agent执行失败: toolId={}", toolId, error);
                    emitter.completeWithError(error);
                }
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private String buildUserContent(String toolId, Map<String, Object> inputs) {
        return switch (toolId) {
            case "file-compare" -> String.format(
                    "文件一内容：\n%s\n\n文件二内容：\n%s",
                    inputs.getOrDefault("file1", ""), inputs.getOrDefault("file2", ""));
            case "file-review" -> String.format(
                    "待校审文件内容：\n%s\n\n重点关注：%s",
                    inputs.getOrDefault("file", ""), inputs.getOrDefault("focus", "全面审查"));
            case "compliance-check" -> String.format(
                    "业务领域：%s\n\n文件内容：\n%s",
                    inputs.getOrDefault("domain", ""), inputs.getOrDefault("content", ""));
            case "meeting-minutes" -> String.format(
                    "会议名称：%s\n\n会议记录：\n%s",
                    inputs.getOrDefault("meetingName", ""), inputs.getOrDefault("transcript", ""));
            case "data-report" -> String.format(
                    "报告类型：%s\n\n数据内容：\n%s",
                    inputs.getOrDefault("reportType", ""), inputs.getOrDefault("data", ""));
            default -> inputs.toString();
        };
    }
}
