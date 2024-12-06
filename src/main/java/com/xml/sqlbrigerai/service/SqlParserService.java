package com.xml.sqlbrigerai.service;

import com.xml.sqlbrigerai.dto.SqlAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SqlParserService {
    public SqlAnalysisResult analyzeSql(String inputText) {
        // TODO: 解析SQL语句
        return SqlAnalysisResult.builder().build();
    }
}
