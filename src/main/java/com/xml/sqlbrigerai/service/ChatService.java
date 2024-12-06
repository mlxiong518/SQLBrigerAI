package com.xml.sqlbrigerai.service;

import com.xml.sqlbrigerai.aiservice.AiTools;
import com.xml.sqlbrigerai.aiservice.IAiChatService;
import com.xml.sqlbrigerai.aiservice.ChatMemoryProvider;
import com.xml.sqlbrigerai.dto.R;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatService {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private ChatMemoryProvider chatMemoryProvider;

    @Autowired
    private AiTools aiTools;

    /**
     * 核心对话方法
     * @param userId 用户唯一标识
     * @param userMessage 用户输入的消息
     * @param systemPrompt 自定义系统提示（可选）
     * @return 模型的回复
     */
    public R processRequest(String userId, String userMessage, String systemPrompt) {
        // 获取用户的会话上下文，如果不存在则创建新的
        ChatMemory chatMemory = chatMemoryProvider.getMessageWindowChatMemory(userId, systemPrompt);

        // Generate response from chat language model
        IAiChatService chatAiService =  AiServices.builder(IAiChatService.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(aiTools)
                .chatMemoryProvider(uid -> chatMemory)
                .build();

        log.info("memoryId:{}",chatMemory.id());
        String result = chatAiService.aiChat(userId,userMessage);
        log.info("result: {}", result);

        chatMemory.messages().forEach(message -> {
                log.debug(message.toString());
        });
        return R.data(result);
    }
}
