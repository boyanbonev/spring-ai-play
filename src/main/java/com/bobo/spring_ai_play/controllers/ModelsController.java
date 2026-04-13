package com.bobo.spring_ai_play.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bobo.spring_ai_play.ChatProperties;
import com.bobo.spring_ai_play.ModelInfo;

/**
 * Exposes all configured models for the UI. Ollama entries use a {@code (local) } label prefix; {@code id} stays the
 * raw tag for {@code POST /chat}.
 */
@RestController
@RequestMapping("/api")
public class ModelsController {

    private final ChatProperties chatProperties;

    public ModelsController(ChatProperties chatProperties) {
        this.chatProperties = chatProperties;
    }

    @GetMapping("/models")
    public List<ModelInfo> models() {
        List<ModelInfo> result = new ArrayList<>();
        for (String id : chatProperties.vertexAllowedModelsList()) {
            result.add(new ModelInfo(id, id));
        }
        for (String id : chatProperties.ollamaAllowedModelsList()) {
            result.add(new ModelInfo(id, ChatProperties.OLLAMA_MODEL_LABEL_PREFIX + id));
        }
        return result;
    }
}
