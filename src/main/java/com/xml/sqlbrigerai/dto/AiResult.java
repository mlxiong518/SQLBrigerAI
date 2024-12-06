package com.xml.sqlbrigerai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiResult {
    private String postgresqlSql;
    private String result;
}
