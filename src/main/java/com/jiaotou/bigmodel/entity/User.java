package com.jiaotou.bigmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String username;
    private String password;
    private String displayName;
    private String avatar;
    private String department;
    private String role;          // admin / user
    private Integer status;       // 1启用 0禁用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
