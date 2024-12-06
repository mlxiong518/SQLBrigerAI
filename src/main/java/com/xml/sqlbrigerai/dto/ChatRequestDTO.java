package com.xml.sqlbrigerai.dto;

import lombok.Data;

@Data
public class ChatRequestDTO {
    private String userMessage;
    private String systemPrompt;
}
