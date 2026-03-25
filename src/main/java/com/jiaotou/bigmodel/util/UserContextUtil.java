package com.jiaotou.bigmodel.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaotou.bigmodel.entity.User;
import com.jiaotou.bigmodel.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContextUtil {

    private final UserMapper userMapper;

    public String getUserId(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) throw new RuntimeException("用户不存在");
        return user.getId();
    }
}
