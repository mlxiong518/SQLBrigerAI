package com.xml.sqlbrigerai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SqlAnalysisResult {
    private String sqlType;
    private String databaseType;
    private String errorMessage;
    private String postgresqlSql;
    private String result;
    private List<String> executionPlan;
    private String estimatedTime;
}