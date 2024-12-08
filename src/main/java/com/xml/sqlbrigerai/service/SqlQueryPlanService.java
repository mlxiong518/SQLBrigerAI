package com.xml.sqlbrigerai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Service;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SqlQueryPlanService {

    public static final String EXPLAIN_SQL_PREFIX = "EXPLAIN ANALYZE ";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取查询计划
     * @param sql
     * @return
     */
    public List<String> getQueryPlan(String sql) {
        if (StringUtils.isBlank(sql)){
            return null;
        }
        // 使用PostgreSQL的EXPLAIN命令获取查询计划
        String explainSql = EXPLAIN_SQL_PREFIX + sql;
        try {
            List<String> resultList = jdbcTemplate.execute((StatementCallback<List<String>>) statement -> {
                statement.execute(explainSql);
                List<String> combinedResults = new ArrayList<>();
                do {
                    try (ResultSet resultSet = statement.getResultSet()) {
                        if (resultSet != null) {
                            RowMapper<String> rowMapper = ResultSet::getString;
                            while (resultSet.next()) {
                                combinedResults.add(rowMapper.mapRow(resultSet, 1));
                            }
                        }
                    }
                } while (statement.getMoreResults());
                return combinedResults;
            });
            return resultList;

        } catch (Exception e) {
            log.error("Failed to get query plan", e);
        }
        return null;
    }

    public String getQueryPlanStr(String sql) {
        List<String> resultList = getQueryPlan(sql);
        if (resultList != null && !resultList.isEmpty()) {
            return String.join("\r\n", resultList);
        }
        return null;
    }

    /**
     * 获取数据库的元数据信息
     * @return
     */
    public Map<String, List<String>> getTableInfo() {
        try {
            DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            Map<String, List<String>> result = new HashMap<>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                ResultSet columns = metaData.getColumns(null, null, tableName, null);
                List<String> columnNameList = new ArrayList<>();
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String remarks = columns.getString("REMARKS");
                    columnNameList.add(String.format("%s(%s)",columnName, remarks));
                }
                result.put(tableName, columnNameList);
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to get table info"+e.getMessage(), e);
        }
        return null;
    }

}



































