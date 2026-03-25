package com.jiaotou.bigmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_record")
public class FileRecord {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String originalName;
    private String storedName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private String parsedContent;  // 文档解析后的纯文本
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
