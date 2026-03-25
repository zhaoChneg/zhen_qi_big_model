package com.jiaotou.bigmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("write_doc")
public class WriteDoc {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String docType;
    private String title;
    private String summary;
    private String content;
    private Integer wordCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
