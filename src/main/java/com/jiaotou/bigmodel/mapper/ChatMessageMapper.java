package com.jiaotou.bigmodel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiaotou.bigmodel.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
