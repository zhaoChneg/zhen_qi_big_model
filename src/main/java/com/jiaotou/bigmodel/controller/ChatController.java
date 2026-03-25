package com.jiaotou.bigmodel.controller;

import com.jiaotou.bigmodel.common.Result;
import com.jiaotou.bigmodel.common.constant.Constants;
import com.jiaotou.bigmodel.entity.ChatSession;
import com.jiaotou.bigmodel.service.impl.ChatServiceImpl;
import com.jiaotou.bigmodel.util.RateLimiter;
import com.jiaotou.bigmodel.util.UserContextUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatServiceImpl chatService;
    private final RateLimiter rateLimiter;
    private final UserContextUtil userContextUtil;

    @GetMapping("/sessions")
    public Result<List<ChatSession>> getSessions(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        return Result.ok(chatService.getSessions(userId));
    }

    @PostMapping("/sessions")
    public Result<ChatSession> createSession(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestBody(required = false) Map<String, String> body) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        String title = body != null ? body.get("title") : null;
        return Result.ok(chatService.createSession(userId, title));
    }

    @DeleteMapping("/sessions/{id}")
    public Result<Void> deleteSession(@PathVariable String id,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        chatService.deleteSession(id, userId);
        return Result.ok();
    }

    @PutMapping("/sessions/{id}")
    public Result<Void> updateSession(@PathVariable String id,
                                      @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        chatService.updateSessionTitle(id, userId, body.get("title"));
        return Result.ok();
    }

    @GetMapping("/sessions/{id}/messages")
    public Result<Object> getMessages(@PathVariable String id,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        return Result.ok(chatService.getMessages(id, userId));
    }

    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody StreamChatRequest req,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        rateLimiter.check(userId); // 限流校验

        SseEmitter emitter = new SseEmitter(Constants.SSE_TIMEOUT);
        chatService.streamChat(req.getSessionId(), userId, req.getMessage(),
                req.getAttachmentIds(), emitter);
        return emitter;
    }

    @Data
    static class StreamChatRequest {
        private String sessionId;
        private String message;
        private List<String> attachmentIds;
    }
}
