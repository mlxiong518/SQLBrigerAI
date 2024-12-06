package com.xml.sqlbrigerai.service;

import com.xml.sqlbrigerai.aiservice.AiTools;
import com.xml.sqlbrigerai.aiservice.ChatMemoryProvider;
import com.xml.sqlbrigerai.dto.AiRequest;
import com.xml.sqlbrigerai.dto.AiResult;
import com.xml.sqlbrigerai.dto.SqlAnalysisResult;
import com.xml.sqlbrigerai.dto.SqlTranslateRequestDTO;
import com.xml.sqlbrigerai.exception.SqlProcessingException;
import com.xml.sqlbrigerai.aiservice.PersistentChatMemoryStore;
import com.xml.sqlbrigerai.aiservice.ISqlService;
import com.xml.sqlbrigerai.util.JsonUtils;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SqlOrchestrationService {

    @Resource
    private SqlParserService parserService;

    @Resource
    private SqlTransformerService transformerService;

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private ChatMemoryProvider chatMemoryProvider;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private AiTools aiTools;

    public SqlAnalysisResult processSqlRequest(String userId,SqlTranslateRequestDTO request) {

            log.info("Processing SQL request: {}", request);
            SqlAnalysisResult analysisResult =  SqlAnalysisResult.builder().build();
            try {
                // 获取用户的会话上下文，如果不存在则创建新的
                ChatMemory chatMemory = chatMemoryProvider.getTokenWindowChatMemory(userId);
                // 创建 AI 服务
                ISqlService ISqlService = AiServices.builder(ISqlService.class)
                        .chatLanguageModel(chatLanguageModel)
                        .tools(aiTools)
                        .chatMemoryProvider(uid -> chatMemory)
                        .build();

                log.info("memoryId:{}",chatMemory.id());
                // 填充上一次ai返回的sql
                if (StringUtils.isBlank(request.getSql()) && chatMemory.messages().size() > 1) {
                    request.setSql(getLastMsg(chatMemory.messages()));
                }
                // 生成 AI 回复
                AiRequest aiRequest = AiRequest.builder().sql(request.getSql()).prompt(request.getPrompt()).build();
                String userMessage = JsonUtils.jsonToStr(aiRequest);
                String aiResponse = ISqlService.aiTranslateSql(userId,userMessage);
                log.info("aiResponse: {}", aiResponse);

                for (int i = 0; i < 2; i++) {
                    try {
                        AiResult aiResult = JsonUtils.getInstance().fromJson(aiResponse, AiResult.class);
                        if (null != aiResult) {
                            analysisResult.setPostgresqlSql(aiResult.getPostgresqlSql());
                            analysisResult.setResult(aiResult.getResult());
                            break;
                        }
                    }catch (Exception e) {
                        log.error("SQL processing failed", e);
                        // 重新发起Ai请求，已JSON格式化失败，则重新发起请求
                        String reTryMessage = "请以 JSON 格式输出。只返回 JSON 数据结构只有postgresqlSql,result二个字段,其中如果有error信息填充到result字段中,不需要```json说明。上次的返回的错误消息：" + e.getMessage();
                        aiResponse = ISqlService.aiTranslateSql(userId, reTryMessage);
                        log.info("try aiResponse: {}", aiResponse);
                    }
                }

               /* CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                            // 打印 提示词 SystemMessage
                            chatMemory.messages().forEach(message -> {
                                if (message instanceof SystemMessage) {
//                                    log.info(message.toString());
                                } else {
                                    log.debug(message.toString());
                                }
                            });
                        }, taskExecutor);
                future.join();*/

                // 4. 获取执行计划

                // 返回结果
                return analysisResult;
            } catch (Exception e) {
                log.error("SQL processing failed", e);
                throw new SqlProcessingException("Failed to process SQL request", e);
            }
    }

    /**
     * 获取最后一次sql信息
     * @return
     */
    private String getLastMsg(List<ChatMessage> messages) {
        if (messages.size() > 1){
                ChatMessage message = messages.get(messages.size() - 1);
                if (message instanceof AiMessage) {
                    AiMessage aiMessage = (AiMessage) message;
                    log.info(aiMessage.toString());
                    try {
                        AiResult aiResult = JsonUtils.getInstance().fromJson(aiMessage.text(), AiResult.class);
                        if (null != aiResult) {
                            return aiResult.getPostgresqlSql();
                        }
                    } catch (Exception e) {
                       log.error(e.toString());
                    }
                }
        }
        return null;
    }


}
