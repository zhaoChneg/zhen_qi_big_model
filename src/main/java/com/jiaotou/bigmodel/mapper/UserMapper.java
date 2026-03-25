package com.jiaotou.bigmodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiaotou.bigmodel.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
