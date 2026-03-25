package com.jiaotou.bigmodel.controller;

import com.jiaotou.bigmodel.common.Result;
import com.jiaotou.bigmodel.common.constant.Constants;
import com.jiaotou.bigmodel.entity.WriteDoc;
import com.jiaotou.bigmodel.service.impl.WriteServiceImpl;
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
@RequestMapping("/api/write")
@RequiredArgsConstructor
public class WriteController {

    private final WriteServiceImpl writeService;
    private final RateLimiter rateLimiter;
    private final UserContextUtil userContextUtil;

    @PostMapping("/summary")
    public Result<Map<String, String>> generateSummary(
            @RequestBody SummaryRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        rateLimiter.check(userContextUtil.getUserId(userDetails.getUsername()));
        return Result.ok(writeService.generateSummary(
                req.getDocType(), req.getTitle(), req.getSupplement(), req.getMatters()));
    }

    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generate(@RequestBody GenerateRequest req,
                               @AuthenticationPrincipal UserDetails userDetails) {
        rateLimiter.check(userContextUtil.getUserId(userDetails.getUsername()));
        SseEmitter emitter = new SseEmitter(Constants.SSE_TIMEOUT);
        writeService.streamGenerate(req.getDocType(), req.getTitle(),
                req.getSummary(), Boolean.TRUE.equals(req.getDeepThink()), emitter);
        return emitter;
    }

    @PostMapping("/polish")
    public Result<Map<String, String>> polish(@RequestBody Map<String, String> body,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        rateLimiter.check(userContextUtil.getUserId(userDetails.getUsername()));
        return Result.ok(writeService.polishText(body.get("text")));
    }

    @GetMapping("/docs")
    public Result<List<WriteDoc>> getDocs(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        return Result.ok(writeService.getDocs(userId));
    }

    @PostMapping("/docs")
    public Result<WriteDoc> saveDoc(@RequestBody SaveDocRequest req,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        return Result.ok(writeService.saveDoc(userId, req.getId(), req.getDocType(),
                req.getTitle(), req.getContent(), req.getSummary()));
    }

    @DeleteMapping("/docs/{id}")
    public Result<Void> deleteDoc(@PathVariable String id,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        writeService.deleteDoc(id, userId);
        return Result.ok();
    }

    @Data static class SummaryRequest {
        private String docType; private String title;
        private String supplement; private String matters;
    }
    @Data static class GenerateRequest {
        private String docType; private String title;
        private String summary; private Boolean deepThink;
    }
    @Data static class SaveDocRequest {
        private String id; private String docType; private String title;
        private String content; private String summary;
    }
}
