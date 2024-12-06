package com.xml.sqlbrigerai.controller;

import com.xml.sqlbrigerai.aiservice.AiTools;
import com.xml.sqlbrigerai.aiservice.ChatMemoryProvider;
import com.xml.sqlbrigerai.aiservice.IAiChatService;
import com.xml.sqlbrigerai.dto.ChatRequestDTO;
import com.xml.sqlbrigerai.dto.R;
import com.xml.sqlbrigerai.service.ChatService;
import com.xml.sqlbrigerai.util.JsonUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(produces = "application/json")
public class ChatController {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private ModerationModel moderationModel;

    @Autowired
    private ChatService chatService;

    @Autowired
    private AiTools aiTools;

    @Autowired
    private ChatMemoryProvider chatMemoryProvider;


    @GetMapping("/test")
    public R test() {
        String content = "获取今天注册的用户信息";
        ChatMemory chatMemory = chatMemoryProvider.getMessageWindowChatMemory("123", "先获取当前具体日期，然后再解决问题");
        IAiChatService aiChatService = AiServices.builder(IAiChatService.class).chatLanguageModel(chatLanguageModel)
                .tools(aiTools)
                .chatMemoryProvider(uid -> chatMemory)
                .build();

        String result = aiChatService.aiChatDate("123",content);
        log.info("chat result: {}", result);
        return R.data(result);
    }

    @GetMapping("/v1/chat")
    public R chat(@RequestParam(required = false) String content) {
        log.info("chat content: {}", content);
        content = content != null ? content : "hello I'm, 你是哪个模型，介绍一下";
        String result = chatLanguageModel.generate(content);
        log.info("chat result: {}", result);
        return R.data(result);
    }

    @PostMapping("/v1/chat/completions")
    public ResponseEntity<?> completions(@RequestHeader(value = "Session-ID", required = false) String sessionId,
                                         @RequestBody ChatRequestDTO request) {
        // 将会话 ID 添加到响应头
        if (sessionId == null){
            sessionId = String.valueOf(System.currentTimeMillis());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Session-ID", sessionId);
        log.debug("Session-ID: {}",sessionId);
        log.debug("completions content: {}", JsonUtils.jsonToStr(request));
        if (StringUtils.isNotBlank(request.getUserMessage()) && request.getUserMessage().equals("string")) {
            request.setUserMessage("你好,你是哪个模型，今天是几号,并介绍一下");
        }
        if (StringUtils.isNotBlank(request.getSystemPrompt()) && request.getSystemPrompt().equals("string")) {
            request.setSystemPrompt(null);
        }
        // 返回响应
        R r = chatService.processRequest(sessionId,request.getUserMessage(), request.getSystemPrompt());
        return ResponseEntity.ok().headers(headers).body(r);
    }

    @GetMapping("/v1/moderations")
    public R moderation(@RequestParam String content) {
        log.info("moderations content: {}", content);
        Response<Moderation> response = moderationModel.moderate(content);
        log.info("response={}",response);
        String result = response.content().flaggedText() != null ?"违规":"合规";
        return R.data(result);
    }

}
