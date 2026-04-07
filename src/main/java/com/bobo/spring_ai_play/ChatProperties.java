package com.bobo.spring_ai_play;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.chat")
public class ChatProperties {

    /**
     * Comma-separated Vertex model IDs clients may select.
     */
    private String allowedModels = "gemini-2.5-flash";

    public String getAllowedModels() {
        return allowedModels;
    }

    public void setAllowedModels(String allowedModels) {
        this.allowedModels = allowedModels;
    }

    public List<String> allowedModelsList() {
        return Arrays.stream(allowedModels.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
