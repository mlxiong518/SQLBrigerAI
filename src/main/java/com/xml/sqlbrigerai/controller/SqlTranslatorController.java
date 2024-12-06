package com.xml.sqlbrigerai.controller;

import com.xml.sqlbrigerai.dto.R;
import com.xml.sqlbrigerai.dto.SqlAnalysisResult;
import com.xml.sqlbrigerai.dto.SqlTranslateRequestDTO;
import com.xml.sqlbrigerai.service.SqlOrchestrationService;
import com.xml.sqlbrigerai.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping(produces = "application/json")
public class SqlTranslatorController {

    @Resource
    private SqlOrchestrationService orchestrationService;

    @PostMapping("/api/sql/translate")
    public ResponseEntity<?> translateSql(@RequestHeader(value = "Session-ID", required = false) String sessionId,
            @RequestBody SqlTranslateRequestDTO request){
        log.info("Running in thread: {}", Thread.currentThread().getName());
        // 将会话 ID 添加到响应头
        if (sessionId == null){
            sessionId = String.valueOf(System.currentTimeMillis());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Session-ID", sessionId);
        log.debug("Session-ID: {}",sessionId);
        log.debug("completions content: {}", JsonUtils.jsonToStr(request));

         // 验证非法输入
         if (StringUtils.isNotBlank(request.getPrompt()) && request.getPrompt().equalsIgnoreCase("string")) {
             request.setPrompt(null);
         }
        if (StringUtils.isNotBlank(request.getSql()) && request.getSql().equalsIgnoreCase("string")) {
            request.setSql(null);
        }
          if (StringUtils.isAllBlank(request.getPrompt(), request.getSql())) {
            return ResponseEntity.ok().headers(headers).body(R.error("Invalid input text"));
          }
        try {
            // 处理请求
            SqlAnalysisResult result = orchestrationService.processSqlRequest(sessionId,request);
            // 返回响应
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(R.data(result));
        }catch (Exception e) {
            log.error("Failed to process SQL translation request", e);
            return ResponseEntity.internalServerError()
                    .body(R.error("Processing failed:"+e.getMessage()));
        }
    }
}
