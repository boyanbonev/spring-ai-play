package com.bobo.spring_ai_play.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bobo.spring_ai_play.ChatProperties;

@RestController
@RequestMapping("/api")
public class ModelsController {

    private final ChatProperties chatProperties;

    public ModelsController(ChatProperties chatProperties) {
        this.chatProperties = chatProperties;
    }

    @GetMapping("/models")
    public List<String> models() {
        return chatProperties.allowedModelsList();
    }
}
