package com.jiaotou.bigmodel.controller;

import com.jiaotou.bigmodel.common.Result;
import com.jiaotou.bigmodel.common.constant.Constants;
import com.jiaotou.bigmodel.service.impl.AgentServiceImpl;
import com.jiaotou.bigmodel.util.RateLimiter;
import com.jiaotou.bigmodel.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentServiceImpl agentService;
    private final RateLimiter rateLimiter;
    private final UserContextUtil userContextUtil;

    @GetMapping
    public Result<List<Map<String, Object>>> getTools() {
        // 工具列表与前端内置的一致，实际可存数据库
        return Result.ok(List.of(
            Map.of("id","file-compare","name","文件比对","category","document","type","chatflow"),
            Map.of("id","file-review","name","文件校审","category","document","type","chatflow"),
            Map.of("id","compliance-check","name","合规检查","category","document","type","chatflow"),
            Map.of("id","meeting-minutes","name","会议纪要","category","meeting","type","chatflow"),
            Map.of("id","data-report","name","数据报告","category","data","type","chatflow")
        ));
    }

    @PostMapping(value = "/{toolId}/run", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter runTool(@PathVariable String toolId,
                              @RequestBody Map<String, Object> body,
                              @AuthenticationPrincipal UserDetails userDetails) {
        rateLimiter.check(userContextUtil.getUserId(userDetails.getUsername()));
        SseEmitter emitter = new SseEmitter(Constants.SSE_TIMEOUT);
        @SuppressWarnings("unchecked")
        Map<String, Object> inputs = (Map<String, Object>) body.getOrDefault("inputs", body);
        agentService.runTool(toolId, inputs, emitter);
        return emitter;
    }
}
