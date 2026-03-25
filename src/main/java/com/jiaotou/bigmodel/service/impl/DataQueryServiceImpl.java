package com.jiaotou.bigmodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiaotou.bigmodel.common.constant.Constants;
import com.jiaotou.bigmodel.common.exception.BusinessException;
import com.jiaotou.bigmodel.entity.DataQueryRecord;
import com.jiaotou.bigmodel.mapper.DataQueryRecordMapper;
import com.jiaotou.bigmodel.util.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataQueryServiceImpl {

    private final DataQueryRecordMapper recordMapper;
    private final LLMClient llmClient;
    private final ObjectMapper objectMapper;

    @Value("${datasource.query.url:#{null}}")
    private String queryDsUrl;
    @Value("${datasource.query.username:#{null}}")
    private String queryDsUser;
    @Value("${datasource.query.password:#{null}}")
    private String queryDsPassword;

    /**
     * 自然语言转SQL
     */
    public Map<String, String> nl2sql(String question, String sourceId) {
        String schemaHint = getSchemaHint(sourceId);
        String prompt = String.format("""
                你是一个专业的SQL生成助手，专门服务于交通投资业务系统。
                数据库表结构信息：
                %s

                用户问题：%s

                请生成一条准确的MySQL查询SQL，要求：
                1. 只返回SQL语句，不要任何解释
                2. SQL必须安全，禁止包含UPDATE/DELETE/INSERT/DROP/ALTER/TRUNCATE等写操作
                3. 结果限制在1000条以内（使用LIMIT）
                4. SQL末尾不加分号

                直接输出SQL：
                """, schemaHint, question);

        String sql = llmClient.chat(prompt).trim();
        // 安全校验：禁止写操作
        validateSqlSafety(sql);

        String explainPrompt = String.format("请用一句话解释以下SQL的查询意图（不超过50字）：\n%s", sql);
        String explanation = llmClient.chat(explainPrompt);

        return Map.of("sql", sql, "explanation", explanation);
    }

    /**
     * 执行SQL查询
     */
    public Map<String, Object> executeQuery(String sql, String sourceId, int page, int pageSize) {
        validateSqlSafety(sql);

        long start = System.currentTimeMillis();
        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Connection conn = getConnection(sourceId);
             Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(Constants.QUERY_TIMEOUT_SECONDS);
            // 分页处理
            String pagedSql = sql + String.format(" LIMIT %d OFFSET %d",
                    pageSize, (page - 1) * pageSize);
            ResultSet rs = stmt.executeQuery(pagedSql);
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                columns.add(meta.getColumnLabel(i));
            }
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String col : columns) {
                    row.put(col, rs.getObject(col));
                }
                rows.add(row);
            }
            // 查询总数
            long total = rows.size();
            try (Statement countStmt = conn.createStatement()) {
                String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS _cnt";
                ResultSet countRs = countStmt.executeQuery(countSql);
                if (countRs.next()) total = countRs.getLong(1);
            } catch (Exception ignored) {}

            long duration = System.currentTimeMillis() - start;
            return Map.of("columns", columns, "rows", rows, "total", total, "duration", duration);
        } catch (Exception e) {
            throw new BusinessException("查询执行失败：" + e.getMessage());
        }
    }

    /**
     * AI生成数据解读
     */
    public Map<String, String> generateSummary(String question, Map<String, Object> data) {
        String prompt = String.format("""
                用户问题：%s
                查询结果（共%s条记录，字段：%s）：
                %s

                请用2-3句话解读这份数据的核心信息，语言简洁专业。
                """,
                question,
                data.get("total"),
                data.get("columns"),
                formatPreview(data));
        return Map.of("summary", llmClient.chat(prompt));
    }

    public Map<String, Object> getHistory(String userId, int page, int pageSize) {
        Page<DataQueryRecord> p = recordMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<DataQueryRecord>()
                        .eq(DataQueryRecord::getUserId, userId)
                        .orderByDesc(DataQueryRecord::getCreatedAt));
        return Map.of("list", p.getRecords(), "total", p.getTotal());
    }

    public void deleteHistory(String id, String userId) {
        DataQueryRecord record = recordMapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        recordMapper.deleteById(id);
    }

    // ==================== 私有方法 ====================

    private void validateSqlSafety(String sql) {
        String upper = sql.toUpperCase().trim();
        List<String> dangerous = List.of("INSERT", "UPDATE", "DELETE", "DROP", "ALTER",
                "TRUNCATE", "CREATE", "EXEC", "EXECUTE", "GRANT", "REVOKE");
        for (String kw : dangerous) {
            if (upper.startsWith(kw) || upper.contains(";" + kw) || upper.contains(" " + kw + " ")) {
                throw new BusinessException("不允许执行写操作SQL");
            }
        }
    }

    private Connection getConnection(String sourceId) throws SQLException {
        if (queryDsUrl == null) {
            throw new BusinessException("数据源未配置，请联系管理员");
        }
        return DriverManager.getConnection(queryDsUrl, queryDsUser, queryDsPassword);
    }

    private String getSchemaHint(String sourceId) {
        // 实际项目中应从数据库或配置中读取表结构
        return """
                表：traffic_toll（收费记录）- 字段：id, station_name(收费站), toll_amount(收费金额), vehicle_type(车辆类型), toll_time(收费时间)
                表：traffic_flow（流量记录）- 字段：id, road_name(路段名称), flow_count(流量), record_date(记录日期), peak_type(高峰类型:morning/evening/normal)
                表：equipment_fault（设备故障）- 字段：id, equipment_id, equipment_name, fault_type, status(pending/resolved), fault_time, resolve_time
                """;
    }

    @SuppressWarnings("unchecked")
    private String formatPreview(Map<String, Object> data) {
        List<Map<String, Object>> rows = (List<Map<String, Object>>) data.get("rows");
        if (rows == null || rows.isEmpty()) return "无数据";
        // 只取前5行预览
        return rows.stream().limit(5)
                .map(Object::toString)
                .reduce("", (a, b) -> a + "\n" + b);
    }
}
