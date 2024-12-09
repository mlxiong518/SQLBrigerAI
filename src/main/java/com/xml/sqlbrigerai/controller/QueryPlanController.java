package com.xml.sqlbrigerai.controller;

import com.xml.sqlbrigerai.aiservice.AiChatMemoryProvider;
import com.xml.sqlbrigerai.dto.R;
import com.xml.sqlbrigerai.service.SqlQueryPlanService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping( value = "/queryplan", produces = "application/json;charset=UTF-8")
public class QueryPlanController {



    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private SqlQueryPlanService sqlQueryPlanService;

    @Autowired
    private AiChatMemoryProvider chatMemoryProvider;

    // 添加sql的swagger 注解的样例数据 select current_database();
    @GetMapping("/explain")
    public R getQueryPlan(@RequestParam(defaultValue = "select current_database();") String sql) {
       return R.data(sqlQueryPlanService.getQueryPlan(sql));
    }
    @GetMapping("/tableInfo")
    public R getTableInfo() {
        return R.data(sqlQueryPlanService.getTableInfo());
    }

    @GetMapping("/chatToSql")
    public R chatToSql(@RequestParam(defaultValue = "统计用户购买商品金额、数量，最贵的商品名称和单价并按最大排序") String sql) {
        return R.data(sqlQueryPlanService.chatToSql(sql));
    }
}
