package com.xml.sqlbrigerai.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface IAiChatService {

    String aiChat(@MemoryId String userId, @UserMessage String userMessage);

    @SystemMessage("先获取当前具体日期，然后再解决用户问题")
    String aiChatDate(@MemoryId String userId, @UserMessage String userMessage);
}
