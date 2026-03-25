package com.jiaotou.bigmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String sessionId;
    private String role;           // user / assistant
    private String content;
    private String attachmentIds;  // JSON数组
    private Integer tokens;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
