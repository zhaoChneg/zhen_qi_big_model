package com.jiaotou.bigmodel.service.impl;

import com.jiaotou.bigmodel.common.constant.Constants;
import com.jiaotou.bigmodel.common.exception.BusinessException;
import com.jiaotou.bigmodel.entity.User;
import com.jiaotou.bigmodel.mapper.UserMapper;
import com.jiaotou.bigmodel.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public Map<String, Object> login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        if (user == null) throw new BusinessException("用户不存在");
        String token = jwtUtil.generateToken(username, user.getId());
        return Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "displayName", user.getDisplayName() != null ? user.getDisplayName() : username,
                        "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                        "department", user.getDepartment() != null ? user.getDepartment() : "",
                        "role", user.getRole()
                )
        );
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null && jwtUtil.isTokenValid(token)) {
            // 加入黑名单，TTL与token剩余有效期一致
            redisTemplate.opsForValue().set(
                    Constants.REDIS_JWT_BLACKLIST + token,
                    "1",
                    jwtUtil.getExpiration(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public User getCurrentUser(String username) {
        return userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
    }
}
