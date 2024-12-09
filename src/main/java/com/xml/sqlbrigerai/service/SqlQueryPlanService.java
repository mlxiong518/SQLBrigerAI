package com.xml.sqlbrigerai.service;

import com.xml.sqlbrigerai.aiservice.AiChatMemoryProvider;
import com.xml.sqlbrigerai.aiservice.IAiChatService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.stereotype.Service;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SqlQueryPlanService {

    public static final String EXPLAIN_SQL_PREFIX = "EXPLAIN ANALYZE ";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private AiChatMemoryProvider chatMemoryProvider;

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

    /**
     * 获取查询计划
     * @param sql
     * @return
     */
    public List<String> getQueryPlan(String sql) {
        if (StringUtils.isBlank(sql)){
            return null;
        }
        // 使用PostgreSQL的EXPLAIN命令获取查询计划
        String explainSql = EXPLAIN_SQL_PREFIX + sql;
        try {
            List<String> resultList = jdbcTemplate.execute((StatementCallback<List<String>>) statement -> {
                statement.execute(explainSql);
                List<String> combinedResults = new ArrayList<>();
                do {
                    try (ResultSet resultSet = statement.getResultSet()) {
                        if (resultSet != null) {
                            RowMapper<String> rowMapper = ResultSet::getString;
                            while (resultSet.next()) {
                                combinedResults.add(rowMapper.mapRow(resultSet, 1));
                            }
                        }
                    }
                } while (statement.getMoreResults());
                return combinedResults;
            });
            return resultList;

        } catch (Exception e) {
            log.error("Failed to get query plan", e);
        }
        return null;
    }

    public String getQueryPlanStr(String sql) {
        List<String> resultList = getQueryPlan(sql);
        if (resultList != null && !resultList.isEmpty()) {
            return String.join("\r\n", resultList);
        }
        return null;
    }

    /**
     * 获取数据库的元数据信息
     * @return
     */
    public Map<String, List<String>> getTableInfo() {
        try {
            DatabaseMetaData metaData = jdbcTemplate.getDataSource().getConnection().getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            Map<String, List<String>> result = new HashMap<>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                ResultSet columns = metaData.getColumns(null, null, tableName, null);
                List<String> columnNameList = new ArrayList<>();
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String remarks = columns.getString("REMARKS");
                    columnNameList.add(String.format("%s(%s)",columnName, remarks));
                }
                result.put(tableName, columnNameList);
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to get table info"+e.getMessage(), e);
        }
        return null;
    }

    public String chatToSql(String sql) {
        // 获取表信息
        Map<String, List<String>> tableInfo = getTableInfo();
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
        return result;
    }
}



































