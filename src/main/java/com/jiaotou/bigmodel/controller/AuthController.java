package com.jiaotou.bigmodel.controller;

import com.jiaotou.bigmodel.common.Result;
import com.jiaotou.bigmodel.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req.getUsername(), req.getPassword()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        authService.logout(token);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<Object> me(@AuthenticationPrincipal UserDetails userDetails) {
        var user = authService.getCurrentUser(userDetails.getUsername());
        return Result.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                "department", user.getDepartment() != null ? user.getDepartment() : "",
                "role", user.getRole()
        ));
    }

    @Data
    static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }
}
