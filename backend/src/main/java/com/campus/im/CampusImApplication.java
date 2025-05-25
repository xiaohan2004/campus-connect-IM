package com.campus.im;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 校园即时通讯系统启动类
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.campus.im.mapper")
public class CampusImApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CampusImApplication.class, args);
    }
} 