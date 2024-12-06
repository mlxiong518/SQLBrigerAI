package com.xml.sqlbrigerai.aiservice;

import com.google.common.collect.Lists;
import com.xml.sqlbrigerai.dto.R;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class AiTools {

    @Tool("用来获取当前具体日期")
    public String dateUtil() {
        String date = java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 19);
        log.info("调用日期 : {}", date);
        return date;
    }

//    @Tool("先获取当前具体日期的用户列表")
//    public List<R> getResult(String date) {
//        log.info("getResult 当前日期 : {}", date);
//        return Lists.newArrayList(R.data(date));
//    }

}
