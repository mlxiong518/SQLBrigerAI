package com.xml.sqlbrigerai.aiservice;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatMemoryProvider {

    @Value("${langchain4j.chat-store:MEMORY}")
    private String chatMemoryStoreDb;

    @Resource
    private PersistentChatMemoryStore persistentChatMemoryStore;
    /**
     * 存储每个用户的会话上下文
     */
    private final Map<String, ChatMemory> userChatMemory = new ConcurrentHashMap<>();

    public ChatMemory getMessageWindowChatMemory(String userId) {
        return getMessageWindowChatMemory(userId,null);
    }
    public ChatMemory getMessageWindowChatMemory(String userId, String systemPrompt) {
        ChatMemory memory = userChatMemory.computeIfAbsent(userId, id -> {
            MessageWindowChatMemory.Builder builder = MessageWindowChatMemory.builder()
                    .maxMessages(1000).id(id);

            // 默认为 InMemoryChatMemoryStore
            if (chatMemoryStoreDb.equals("DB")){
                log.info("ChatMemoryStore store: PersistentChatMemoryStore");
                builder.chatMemoryStore(persistentChatMemoryStore);
            } else {
                log.info("ChatMemoryStore store: InMemoryChatMemoryStore");
            }
            return builder.build();
        });

        ChatMemory chatMemory = userChatMemory.get(userId);
        if (StringUtils.isNotBlank(systemPrompt) && chatMemory.messages().isEmpty()) {
            // 添加系统提示词
            memory.add(SystemMessage.from(systemPrompt));
        }
        // 读取历史消息 db
        setDbHistoryChatMessages(userId,chatMemory);
        return memory;

    }

    public ChatMemory getTokenWindowChatMemory(String userId) {
        return getTokenWindowChatMemory(userId,null);
    }
    public ChatMemory getTokenWindowChatMemory(String userId, String systemPrompt) {
        userChatMemory.computeIfAbsent(userId, id -> {
            // 读取系统提示词文件
            TokenWindowChatMemory.Builder builder = TokenWindowChatMemory.builder()
                    .maxTokens(32*1000,new OpenAiTokenizer(OpenAiChatModelName.GPT_4_O_MINI))
                    .id(id);
            // 默认为 InMemoryChatMemoryStore
            if (chatMemoryStoreDb.equals("DB")){
                log.info("ChatMemoryStore store: PersistentChatMemoryStore");
                builder.chatMemoryStore(persistentChatMemoryStore);
            } else {
                log.info("ChatMemoryStore store: InMemoryChatMemoryStore");
            }
            return builder.build();
        });

        ChatMemory chatMemory = userChatMemory.get(userId);
        if (StringUtils.isNotBlank(systemPrompt) && chatMemory.messages().isEmpty()) {
            // 添加系统提示词
            chatMemory.add(SystemMessage.from(systemPrompt));
        }
        // 读取历史消息 db
        setDbHistoryChatMessages(userId,chatMemory);
        return chatMemory;
    }

    private void setDbHistoryChatMessages(String userId,ChatMemory chatMemory) {
        if (chatMemoryStoreDb.equals("DB")){
            List<ChatMessage> chatMessages = persistentChatMemoryStore.getMessages(userId);
            if (!chatMessages.isEmpty()){
                chatMemory.messages().addAll(chatMessages);
            }
        }
    }
}