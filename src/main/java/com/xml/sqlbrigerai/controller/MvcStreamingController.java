package com.xml.sqlbrigerai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Slf4j
@RestController
@RequestMapping(value = "/mvc",produces = MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
public class MvcStreamingController {

    @GetMapping(value ="/stream")
    public ResponseEntity<StreamingResponseBody> stream(){
        StreamingResponseBody streaming = outputStream -> {
            try {
                for (int i = 0; i < 3; i++) {
                    String data = "Data chunk " + i + "\n";
                    outputStream.write(data.getBytes());
                    outputStream.flush();
                    Thread.sleep(1000);
                }
            }catch (Exception e){
               log.error(e.getMessage(),e);
            }
        };
        return ResponseEntity.ok().body(streaming);
    }



}
