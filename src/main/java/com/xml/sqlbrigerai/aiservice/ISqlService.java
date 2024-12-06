package com.xml.sqlbrigerai.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ISqlService {

    @SystemMessage(fromResource = "/sql_system_prompt.txt")
    String aiTranslateSql(@MemoryId String userId, @UserMessage String userMessage);
}
