package com.xml.sqlbrigerai.aiservice;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;
import static org.mapdb.Serializer.STRING;

/**
 * ChatMemoryStore的实现类来实现将ChatMessage持久化到磁盘
 */
@Slf4j
@Service
public class PersistentChatMemoryStore implements ChatMemoryStore, CommandLineRunner {

    private final DB db = DBMaker.fileDB("chat-memory.db").transactionEnable().make();
    private final Map<String, String> map = db.hashMap("messages", STRING, STRING).createOrOpen();

    private static final long DEFAULT_EXPIRE_UNIT = 24 * 60 * 60 * 100;

    @Value("${langchain4j.store-expires:1}")
    private Long storeExpires;

    @Override
    public void run(String... args) throws Exception {
        // 清空数据库
        int dbSize = map.size();
        cleanExpiredMessages();
        log.debug("run MapDB:{}->{}", dbSize,map.size());
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = map.get((String) memoryId);
        return messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messagesList) {
        String json = messagesToJson(messagesList);
        map.put((String) memoryId, json);
        db.commit();
    }

    @Override
    public void deleteMessages(Object memoryId) {
        map.remove((String) memoryId);
        db.commit();
    }

    /**
     * 定期清理过期对话
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天午夜执行
    public void cleanExpiredMessages() {
        if (map.isEmpty()) {
            log.info("No messages to clean.");
            return;
        }
        long oneDayAgoMillis = System.currentTimeMillis() - storeExpires*DEFAULT_EXPIRE_UNIT;
        log.info("Cleaning expired messages at {}", oneDayAgoMillis);
        String id = "default";
        map.remove(id);
        map.keySet().removeIf(key -> {
            // 判断 key 是数字类型的字符串
            if (key.matches("\\d+") && Long.parseLong(key) < oneDayAgoMillis) {
                log.info("Deleting expired messages for memoryId: {}", key);
                return true;
            }
            return false;
        });
        db.commit();
    }
}
