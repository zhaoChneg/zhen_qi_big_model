package com.jiaotou.bigmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("data_query_record")
public class DataQueryRecord {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String userId;
    private String sourceId;
    private String question;
    private String sql;
    private String status;         // pending/running/success/error
    private String resultJson;     // 查询结果（JSON）
    private String errorMsg;
    private String aiSummary;
    private Long duration;         // 执行耗时(ms)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
