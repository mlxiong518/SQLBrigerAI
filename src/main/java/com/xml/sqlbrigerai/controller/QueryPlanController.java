package com.xml.sqlbrigerai.controller;

import com.xml.sqlbrigerai.aiservice.ChatMemoryProvider;
import com.xml.sqlbrigerai.aiservice.IAiChatService;
import com.xml.sqlbrigerai.dto.R;
import com.xml.sqlbrigerai.service.SqlQueryPlanService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping(produces = "application/json")
public class QueryPlanController {

    private final String FILTER_INSTRUCTION = """
    你需要根据指定的Input从Instruction中筛选出最相关的表信息（可能是单个表或多个表），
    首先，我将给你展示一个示例，Instruction后面跟着Input和对应的Response，
    然后，我会给你一个新的Instruction和新的Input，你需要生成一个新的Response来完成任务。

    ### Example1 Instruction:
    job(id, name, age), user(id, name, age), student(id, name, age, info)
    ### Example1 Input:
    Find the age of student table
    ### Example1 Response:
    student(id, name, age, info)
    ###New Instruction:
    {instruction}
    ###New Input:
    {input}
    ###New Response:
    """;

    private final String GENERATE_INSTRUCTION = """
    你扮演一个SQL终端，您只需要返回SQL命令给我，而不需要返回其他任何字符。下面是一个描述任务的Instruction，返回适当的结果完成Input对应的请求.
    ###Instruction:
    {instruction}
    ###Input:
    {input}
    ###Response:
    """;

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private SqlQueryPlanService sqlQueryPlanService;

    @Autowired
    private ChatMemoryProvider chatMemoryProvider;

    @GetMapping("/explain")
    public R getQueryPlan(@RequestParam String sql) {
       return R.data(sqlQueryPlanService.getQueryPlan(sql));
    }
    @GetMapping("/tableInfo")
    public R getTableInfo() {
        return R.data(sqlQueryPlanService.getTableInfo());
    }

    @GetMapping("/chat")
    public R chat(@RequestParam String sql) {
        // 获取表信息
        Map<String, List<String>> tableInfo = sqlQueryPlanService.getTableInfo();
        List<String> tableInfoList  = tableInfo.entrySet().stream().map(entry -> {
            String tableName = entry.getKey();
            List<String> columnList = entry.getValue();
            return String.format("%s(%s)", tableName, StringUtils.join(columnList, ", "));
        }).toList();

        String tableInfoPrompt = StringUtils.join(tableInfoList, ",");


        String filterSqlMessage = FILTER_INSTRUCTION.replace("{instruction}", tableInfoPrompt)
                .replace("{input}", sql);

        String userId = "123";
        ChatMemory chatMemory = chatMemoryProvider.getMessageWindowChatMemory(userId, null);
        IAiChatService aiChatService = AiServices.builder(IAiChatService.class).chatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(uid -> chatMemory)
                .build();

        String resultTab = aiChatService.aiChat(userId,filterSqlMessage);
        log.info("chat resultTab: {}", resultTab);

        String reqSqlMessage = GENERATE_INSTRUCTION.replace("{instruction}", resultTab)
                                             .replace("{input}", sql);

        String result = aiChatService.aiChat(userId,reqSqlMessage);
        result = result.replace("```sql", "").replace("```", "");
        log.info("chat result: {}", result);
        return R.data(result);


    }
}
