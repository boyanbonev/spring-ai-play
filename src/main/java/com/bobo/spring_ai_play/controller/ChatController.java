package com.bobo.spring_ai_play.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.bobo.spring_ai_play.ChatProperties;
import com.bobo.spring_ai_play.ChatRequest;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final ChatProperties chatProperties;

    public ChatController(ChatClient.Builder chatClientBuilder, ChatProperties chatProperties) {
        this.chatClient = chatClientBuilder.build();
        this.chatProperties = chatProperties;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody ChatRequest request) {
        if (request == null || request.prompt() == null || request.prompt().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String prompt = request.prompt().trim();
        String model = request.model();
        if (model != null && !model.isBlank()) {
            String m = model.trim();
            if (!chatProperties.allowedModelsList().contains(m)) {
                return ResponseEntity.badRequest().build();
            }
        }

        StreamingResponseBody body = outputStream -> {
            var spec = chatClient.prompt(prompt);
            if (model != null && !model.isBlank()) {
                spec = spec.options(VertexAiGeminiChatOptions.builder().model(model.trim()).build());
            }
            spec.stream().content().toStream().forEach(chunk -> {
                try {
                    outputStream.write(chunk.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        };
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(body);
    }
}
