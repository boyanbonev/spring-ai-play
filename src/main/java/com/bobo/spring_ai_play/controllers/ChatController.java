package com.bobo.spring_ai_play.controllers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.bobo.spring_ai_play.ChatProperties;
import com.bobo.spring_ai_play.ChatRequest;
import com.bobo.spring_ai_play.services.ChatClientFactory;

/**
 * Streams chat completions as {@code text/plain}. Model routing and options are handled by {@link ChatClientFactory}.
 */
@RestController
public class ChatController {

    private final ChatClientFactory chatClientFactory;
    private final ChatProperties chatProperties;

    public ChatController(ChatClientFactory chatClientFactory, ChatProperties chatProperties) {
        this.chatClientFactory = chatClientFactory;
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
            if (!chatProperties.allAllowedModelsList().contains(m)) {
                return ResponseEntity.badRequest().build();
            }
        }

        ChatClient chatClient = chatClientFactory.buildChatClient(model);

        StreamingResponseBody body = outputStream -> {
            chatClient.prompt(prompt).stream().content().toStream().forEach(chunk -> {
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
