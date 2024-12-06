package com.xml.sqlbrigerai.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = false)
@Data
public class R<T> extends HashMap<String, Object> {
    /**
     * 返回码
     */
    public static int CODE = 1;
    /**
     * 返回消息
     */
    public static String MESSAGE = "success";
    /**
     * 数据列表
     */
    private T data;

    public R(){
        put("code", CODE);
        put("message", MESSAGE);
    }
    public static R ok(){
        return new R();
    }
    public static  R ok(String message){
        R r = new R();
        r.put("message", message);
        return r;
    }
    public static R ok(int code){
        R r = new R();
        r.put("code", code);
        return r;
    }
    public static R ok(int code,String message){
        R r = new R();
        r.put("code", code);
        r.put("message", message);
        return r;
    }
    public static <T> R<T> data(T data){
        R r = new R();
        r.put("data", data);
        return r;
    }

    public static R error(String message){
        R r = new R();
        r.put("code", 0);
        r.put("message", message);
        return r;
    }
}
