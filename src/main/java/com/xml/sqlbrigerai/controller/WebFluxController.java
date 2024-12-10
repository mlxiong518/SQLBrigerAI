package com.xml.sqlbrigerai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

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



}
