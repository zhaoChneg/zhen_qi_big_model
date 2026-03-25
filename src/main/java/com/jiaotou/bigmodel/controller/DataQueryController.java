package com.jiaotou.bigmodel.controller;

import com.jiaotou.bigmodel.common.Result;
import com.jiaotou.bigmodel.service.impl.DataQueryServiceImpl;
import com.jiaotou.bigmodel.util.RateLimiter;
import com.jiaotou.bigmodel.util.UserContextUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataQueryController {

    private final DataQueryServiceImpl dataQueryService;
    private final RateLimiter rateLimiter;
    private final UserContextUtil userContextUtil;

    @GetMapping("/sources")
    public Result<List<Map<String, Object>>> getSources() {
        // 数据源列表（实际项目可从配置或数据库读取）
        return Result.ok(List.of(
                Map.of("id", "default", "name", "交投业务数据库",
                        "description", "包含收费、流量、设备等核心业务数据",
                        "type", "mysql", "database", "jiaotou_biz")
        ));
    }

    @PostMapping("/nl2sql")
    public Result<Map<String, String>> nl2sql(@RequestBody Nl2SqlRequest req,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        rateLimiter.check(userContextUtil.getUserId(userDetails.getUsername()));
        return Result.ok(dataQueryService.nl2sql(req.getQuestion(), req.getSourceId()));
    }

    @PostMapping("/execute")
    public Result<Map<String, Object>> execute(@RequestBody ExecuteRequest req,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        return Result.ok(dataQueryService.executeQuery(
                req.getSql(), req.getSourceId(),
                req.getPage() != null ? req.getPage() : 1,
                req.getPageSize() != null ? req.getPageSize() : 20));
    }

    @PostMapping("/summary")
    public Result<Map<String, String>> summary(@RequestBody SummaryRequest req,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        rateLimiter.check(userContextUtil.getUserId(userDetails.getUsername()));
        return Result.ok(dataQueryService.generateSummary(req.getQuestion(), req.getData()));
    }

    @GetMapping("/history")
    public Result<Map<String, Object>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        return Result.ok(dataQueryService.getHistory(userId, page, pageSize));
    }

    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(@PathVariable String id,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userContextUtil.getUserId(userDetails.getUsername());
        dataQueryService.deleteHistory(id, userId);
        return Result.ok();
    }

    @Data static class Nl2SqlRequest { private String question; private String sourceId; }
    @Data static class ExecuteRequest {
        private String sql; private String sourceId;
        private Integer page; private Integer pageSize;
    }
    @Data static class SummaryRequest { private String question; private Map<String, Object> data; }
}
