package com.xml.sqlbrigerai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiRequest {
    private String sql;
    private String prompt;
}
