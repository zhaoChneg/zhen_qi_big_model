package com.jiaotou.bigmodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaotou.bigmodel.common.exception.BusinessException;
import com.jiaotou.bigmodel.entity.ChatMessage;
import com.jiaotou.bigmodel.entity.ChatSession;
import com.jiaotou.bigmodel.mapper.ChatMessageMapper;
import com.jiaotou.bigmodel.mapper.ChatSessionMapper;
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
public class ChatServiceImpl {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final LLMClient llmClient;

    public List<ChatSession> getSessions(String userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .orderByDesc(ChatSession::getUpdatedAt));
    }

    public ChatSession createSession(String userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        session.setMessageCount(0);
        sessionMapper.insert(session);
        return session;
    }

    public void deleteSession(String sessionId, String userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在");
        }
        sessionMapper.deleteById(sessionId);
        // 级联删除消息
        messageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId));
    }

    public void updateSessionTitle(String sessionId, String userId, String title) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在");
        }
        session.setTitle(title);
        sessionMapper.updateById(session);
    }

    public List<ChatMessage> getMessages(String sessionId, String userId) {
        // 验证会话归属
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException("会话不存在");
        }
        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreatedAt));
    }

    @Async("sseExecutor")
    public void streamChat(String sessionId, String userId, String userMessage,
                           List<String> attachmentIds, SseEmitter emitter) {
        // 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        messageMapper.insert(userMsg);

        // 构建历史上下文（最近10条）
        List<ChatMessage> history = messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByDesc(ChatMessage::getCreatedAt)
                        .last("LIMIT 10"));

        List<Map<String, String>> messages = history.stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .toList();

        // 保存 AI 消息占位
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent("");
        messageMapper.insert(aiMsg);

        StringBuilder fullContent = new StringBuilder();
        try {
            llmClient.streamChat(messages, chunk -> {
                try {
                    emitter.send(SseEmitter.event().data("data: " + chunk));
                    fullContent.append(chunk);
                } catch (IOException e) {
                    log.warn("SSE发送失败: {}", e.getMessage());
                }
            }, () -> {
                // 完成回调：更新AI消息内容
                aiMsg.setContent(fullContent.toString());
                messageMapper.updateById(aiMsg);
                // 更新会话消息数
                ChatSession session = sessionMapper.selectById(sessionId);
                if (session != null) {
                    session.setMessageCount(session.getMessageCount() + 2);
                    sessionMapper.updateById(session);
                }
                try {
                    emitter.send(SseEmitter.event().data("data: [DONE]"));
                    emitter.complete();
                } catch (IOException e) {
                    log.warn("SSE完成发送失败", e);
                }
            }, error -> {
                log.error("LLM调用失败", error);
                try {
                    emitter.send(SseEmitter.event().data("data: [ERROR]"));
                    emitter.completeWithError(error);
                } catch (IOException ex) {
                    log.warn("SSE错误发送失败", ex);
                }
            });
        } catch (Exception e) {
            log.error("流式对话异常", e);
            emitter.completeWithError(e);
        }
    }
}
