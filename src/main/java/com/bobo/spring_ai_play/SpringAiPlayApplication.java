package com.bobo.spring_ai_play;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/** Spring Boot entry point; enables {@link ChatProperties} for {@code app.chat.*} settings. */
@SpringBootApplication
@EnableConfigurationProperties(ChatProperties.class)
public class SpringAiPlayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiPlayApplication.class, args);
    }
}
