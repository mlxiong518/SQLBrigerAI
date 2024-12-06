package com.xml.sqlbrigerai.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SqlTranslateRequestDTO {
    private String dataType;
    private String sql; // sql语句
    private String prompt; // 用户提示
}