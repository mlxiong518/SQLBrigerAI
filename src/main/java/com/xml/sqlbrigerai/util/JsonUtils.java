package com.xml.sqlbrigerai.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtils {

    private static final Gson gson = new Gson();
    // 私有构造函数，防止实例化
    private JsonUtils() {
    }

    public static Gson getInstance() {
        return gson;
    }

    public static String jsonToStr(Object object) {
        return gson.toJson(object);
    }

    /**
     * 将JSON字符串转换为指定类型的Java对象。
     *
     * @param jsonString JSON字符串
     * @param classType  要转换的目标类类型
     * @param <T>        Java对象的类型
     * @return 转换后的Java对象
     */
    public static <T> T fromJson(String jsonString, Class<T> classType) {
        try {
            return gson.fromJson(jsonString, classType);
        } catch (Exception e) {
            log.error("JSON转换失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 将JSON字符串转换为List对象。
     *
     * @param jsonString JSON字符串
     * @param classType  List中元素的类类型
     * @param <T>        List中元素的类型
     * @return 转换后的List对象
     */
    public static <T> List<T> fromJsonToList(String jsonString, Class<T> classType) {
        Type listType = new TypeToken<List<T>>(){}.getType();
        return gson.fromJson(jsonString, listType);
    }

    /**
     * 将JSON字符串转换为List<Map<String, Object>>对象。
     *
     * @param jsonString JSON字符串
     * @return 转换后的List<Map<String, Object>>对象
     */
    public static List<Map<String, Object>> fromJsonToListMap(String jsonString) {
        Type listMapType = new TypeToken<List<Map<String, Object>>>(){}.getType();
        return gson.fromJson(jsonString, listMapType);
    }
}
