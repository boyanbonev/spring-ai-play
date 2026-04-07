package com.bobo.spring_ai_play.controller;

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

import com.bobo.spring_ai_play.ChatRequest;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody ChatRequest request) {
        if (request == null || request.prompt() == null || request.prompt().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String prompt = request.prompt().trim();
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
