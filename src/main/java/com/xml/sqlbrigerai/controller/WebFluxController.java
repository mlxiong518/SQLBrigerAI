package com.xml.sqlbrigerai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping(value = "/webflux",produces = MediaType.TEXT_EVENT_STREAM_VALUE+";charset=UTF-8")
public class WebFluxController {

    @GetMapping("/mono")
    public Mono<String> mono() {
        return Mono.just("Hello from Spring WebFlux!");
    }

    @GetMapping(value ="/stream")
    public Flux<String> streamFlux(){
//        return Flux.just("Hello","World","From","Spring","WebFlux");
        return Flux.interval(Duration.ofSeconds(1)) // 每秒生成一个数据
//                .map(i->"Data chunk "+i+"\n")
                .map(i->"Data chunk "+i)
                .take(3); // 只生成 10 个数据
    }

//    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private ThreadPoolTaskExecutor executor;

    // 执行命令cmd  steam 输出日志到前端
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/execute-command", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFluxCommand(){

        return Flux.create(sink -> {
            String command = "C://windows/system32/cmd.exe /c cd /d D:\\tmp && D:\\tools\\javatools\\jdk-17\\bin\\java -jar D:/tmp/SQLBrigerAI-0.0.1.jar";
            executor.submit(() -> {
                try {
                    Process process = Runtime.getRuntime().exec(command);
                    readStream(process.getInputStream(), sink);
                    readStream(process.getErrorStream(), sink);
                    process.waitFor();
                    sink.complete();
                } catch (IOException | InterruptedException e) {
                    sink.error(e);
                }
            });
        });
    }

    private void readStream(InputStream inputStream, FluxSink<String> sink) {
        new BufferedReader(new InputStreamReader(inputStream)).lines()
                .forEach(line -> sink.next(line));
    }




}
