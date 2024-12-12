package com.xml.sqlbrigerai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
public class FileController {

    // 文件存储在这个目录下
    private final Path fileStorageLocation = Paths.get("D:/tmp");

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName){
        try {
            // 构建文件下载路径
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            // 构建文件下载资源
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            }else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while downloading file", e);
        }
    }
}
