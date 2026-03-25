package com.jiaotou.bigmodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaotou.bigmodel.common.exception.BusinessException;
import com.jiaotou.bigmodel.entity.WriteDoc;
import com.jiaotou.bigmodel.mapper.WriteDocMapper;
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
public class WriteServiceImpl {

    private final WriteDocMapper writeDocMapper;
    private final LLMClient llmClient;

    public Map<String, String> generateSummary(String docType, String title,
                                                String supplement, String matters) {
        String prompt = String.format("""
                你是一位专业的政务公文写作助手。
                请根据以下信息生成一份公文摘要，摘要需简洁准确，100-200字。
                公文类型：%s
                标题：%s
                补充信息：%s
                报告事项：%s
                请直接输出摘要内容，不要有任何前缀。
                """, docType, title,
                supplement != null ? supplement : "",
                matters != null ? matters : "");

        String summary = llmClient.chat(prompt);
        return Map.of("summary", summary);
    }

    @Async("sseExecutor")
    public void streamGenerate(String docType, String title, String summary,
                               boolean deepThink, SseEmitter emitter) {
        String prompt = String.format("""
                你是一位专业的政务公文写作助手。
                请根据以下摘要，生成一份完整的%s。
                标题：%s
                摘要：%s
                要求：格式规范、语言正式、内容完整、符合政务公文写作规范。
                %s
                请直接输出公文正文内容（HTML格式，含段落标签），不要有任何前缀。
                """, docType, title, summary,
                deepThink ? "请进行深度思考，确保内容逻辑严密、论述充分。" : "");

        StringBuilder fullContent = new StringBuilder();
        try {
            llmClient.streamChat(
                    List.of(Map.of("role", "user", "content", prompt)),
                    chunk -> {
                        try {
                            emitter.send(SseEmitter.event().data("data: " + chunk));
                            fullContent.append(chunk);
                        } catch (IOException e) {
                            log.warn("写作SSE发送失败", e);
                        }
                    },
                    () -> {
                        try {
                            emitter.send(SseEmitter.event().data("data: [DONE]"));
                            emitter.complete();
                        } catch (IOException e) {
                            log.warn("写作SSE完成失败", e);
                        }
                    },
                    error -> {
                        log.error("写作生成失败", error);
                        emitter.completeWithError(error);
                    }
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    public Map<String, String> polishText(String text) {
        String prompt = "请对以下政务文本进行润色，保持原意，优化措辞和表达，直接输出润色后的内容：\n" + text;
        return Map.of("result", llmClient.chat(prompt));
    }

    public List<WriteDoc> getDocs(String userId) {
        return writeDocMapper.selectList(
                new LambdaQueryWrapper<WriteDoc>()
                        .eq(WriteDoc::getUserId, userId)
                        .orderByDesc(WriteDoc::getUpdatedAt));
    }

    public WriteDoc saveDoc(String userId, String id, String docType, String title,
                            String content, String summary) {
        WriteDoc doc;
        if (id != null && !id.isEmpty()) {
            doc = writeDocMapper.selectById(id);
            if (doc == null || !doc.getUserId().equals(userId)) {
                throw new BusinessException("文稿不存在");
            }
        } else {
            doc = new WriteDoc();
            doc.setUserId(userId);
        }
        doc.setDocType(docType);
        doc.setTitle(title);
        doc.setContent(content);
        doc.setSummary(summary);
        doc.setWordCount(content != null ? content.replaceAll("<[^>]*>", "").length() : 0);
        if (doc.getId() == null) {
            writeDocMapper.insert(doc);
        } else {
            writeDocMapper.updateById(doc);
        }
        return doc;
    }

    public void deleteDoc(String id, String userId) {
        WriteDoc doc = writeDocMapper.selectById(id);
        if (doc == null || !doc.getUserId().equals(userId)) {
            throw new BusinessException("文稿不存在");
        }
        writeDocMapper.deleteById(id);
    }
}
