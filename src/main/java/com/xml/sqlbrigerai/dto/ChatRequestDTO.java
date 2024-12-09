package com.xml.sqlbrigerai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatRequestDTO {
    @Schema(defaultValue = "你好,你是哪个模型，今天是几号,并介绍一下")
    private String userMessage;
    private String systemPrompt;
}
