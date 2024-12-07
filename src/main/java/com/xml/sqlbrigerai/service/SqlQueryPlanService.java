package com.xml.sqlbrigerai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SqlQueryPlanService {

    public static final String EXPLAIN_SQL_PREFIX = "EXPLAIN ANALYZE ";

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
//            if (resultList != null && !resultList.isEmpty()) {
//                return String.join("\r\n", resultList);
//            }
        } catch (Exception e) {
            log.error("Failed to get query plan", e);
        }
        return null;
    }
}
