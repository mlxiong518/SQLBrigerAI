package com.xml.sqlbrigerai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SqlAnalysisResult {
    private String sqlType;
    private String databaseType;
    private String errorMessage;
    private String postgresqlSql;
    private String result;
    private String executionPlan;
    private String estimatedTime;
}