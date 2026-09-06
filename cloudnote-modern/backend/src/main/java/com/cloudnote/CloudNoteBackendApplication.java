package com.cloudnote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 云笔记现代化后端入口 (Spring Boot 3)
 */
@SpringBootApplication
@MapperScan("com.cloudnote.mapper")
public class CloudNoteBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudNoteBackendApplication.class, args);
    }
}
