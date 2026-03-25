package com.jiaotou.bigmodel.common.constant;

public interface Constants {
    // Redis key 前缀
    String REDIS_JWT_BLACKLIST = "jwt:blacklist:";
    String REDIS_USER_SSE      = "sse:user:";
    String REDIS_RATE_LIMIT    = "rate:limit:";
    String REDIS_QUERY_LOCK    = "query:lock:";

    // SSE 超时（毫秒）
    long SSE_TIMEOUT = 5 * 60 * 1000L;

    // 文件上传限制
    long MAX_FILE_SIZE = 20 * 1024 * 1024L;   // 20MB
    String[] ALLOWED_EXTENSIONS = {"pdf", "doc", "docx", "txt", "xls", "xlsx"};

    // 限流：每用户每分钟最多10次AI请求
    int RATE_LIMIT_PER_MINUTE = 10;

    // SQL查询超时（秒）
    int QUERY_TIMEOUT_SECONDS = 30;
}
