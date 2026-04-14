package com.bobo.spring_ai_play.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.bobo.spring_ai_play.ChatProperties;

/**
 * Builds a {@link ChatClient} for the correct backend (Vertex Gemini vs local Ollama) and sets the model via
 * {@link ChatClient.Builder#defaultOptions(org.springframework.ai.chat.prompt.ChatOptions)} so callers only run
 * {@code prompt(...).stream()} without per-request options.
 * <p>
 * For Ollama, options are based on {@link OllamaChatModel#getDefaultOptions()} (including
 * {@code spring.ai.ollama.chat.options.*}) and only the model id is overridden. Building a fresh
 * {@code OllamaChatOptions.builder().model(id)} would drop global settings such as {@code num-predict}, which often
 * causes very short local replies.
 */
@Service
public class ChatClientFactory {

    private final ApplicationContext applicationContext;
    private final ChatProperties chatProperties;
    private final ChatMemory chatMemory;

    public ChatClientFactory(
        ApplicationContext applicationContext,
        ChatProperties chatProperties,
        ChatMemory chatMemory) {
        this.applicationContext = applicationContext;
        this.chatProperties = chatProperties;
        this.chatMemory = chatMemory;
    }

    /**
     * @param modelName selected model modelId, or {@code null} / blank to use global Vertex defaults from
     *                  {@code spring.ai.vertex.ai.gemini.chat.options.*}
     */
    public ChatClient buildChatClient(String modelName) {
        ChatClient.Builder builder = null;
        modelName = modelName.trim();
        if (modelName == null || modelName.isBlank()) {
            VertexAiGeminiChatModel chatModel = this.applicationContext.getBean(VertexAiGeminiChatModel.class);
            builder = ChatClient.builder(chatModel);
        } else if (chatProperties.isOllamaModel(modelName)) {
            OllamaChatModel chatModel = this.applicationContext.getBean(OllamaChatModel.class);
            builder = ChatClient.builder(chatModel)
                    .defaultOptions(ollamaOptionsWithModel(modelName, chatModel));
        } else if (chatProperties.isVertexModel(modelName)) {
            VertexAiGeminiChatModel chatModel = this.applicationContext.getBean(VertexAiGeminiChatModel.class);
            builder = ChatClient.builder(chatModel)
                    .defaultOptions(vertexOptionsWithModel(modelName, chatModel));
        } else {
            throw new IllegalArgumentException("Model is not in Vertex or Ollama allowlists: " + modelName);
        }

        return builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }

    private OllamaChatOptions ollamaOptionsWithModel(String modelName, OllamaChatModel chatModel) {
        ChatOptions chatOptions = chatModel.getDefaultOptions();
        OllamaChatOptions opts;
        if (chatOptions instanceof OllamaChatOptions o) {
            opts = OllamaChatOptions.fromOptions(o);
        } else {
            opts = new OllamaChatOptions();
        }
        opts.setModel(modelName);
        return opts;
    }

    private VertexAiGeminiChatOptions vertexOptionsWithModel(String modelName, VertexAiGeminiChatModel chatModel) {
        ChatOptions chatOptions = chatModel.getDefaultOptions();
        VertexAiGeminiChatOptions opts;
        if (chatOptions instanceof VertexAiGeminiChatOptions v) {
            opts = VertexAiGeminiChatOptions.fromOptions(v);
        } else {
            opts = new VertexAiGeminiChatOptions();
        }
        opts.setModel(modelName);
        return opts;
    }
}
