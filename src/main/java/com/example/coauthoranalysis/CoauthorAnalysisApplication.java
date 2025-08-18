package com.example.coauthoranalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
// 添加这行
public class CoauthorAnalysisApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoauthorAnalysisApplication.class, args);
    }
}