package com.salon;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 美发沙龙会员管理系统 - 启动类
 * <p>
 * 基于 Spring Boot 3 + MyBatis Plus 的全栈会员管理系统
 * </p>
 */
@EnableScheduling
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "美发沙龙会员管理系统 API",
                version = "1.0.0",
                description = "提供会员管理、服务项目管理、消费收银等 REST API",
                contact = @Contact(name = "Salon Team")
        )
)
public class SalonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalonApplication.class, args);
    }
}
