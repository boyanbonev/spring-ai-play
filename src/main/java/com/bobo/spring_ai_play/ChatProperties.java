package com.bobo.spring_ai_play;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.annotation.PostConstruct;

/**
 * User-facing chat configuration: which Vertex (cloud) and Ollama (local) model IDs are allowed.
 * <p>
 * Vertex IDs come from {@code app.chat.allowed-models}; Ollama tags from {@code app.chat.ollama-allowed-models}.
 * The two lists must not overlap — validated at startup.
 */
@ConfigurationProperties(prefix = "app.chat")
public class ChatProperties {

    /**
     * Shown before Ollama model names in the UI ({@code /api/models} {@code label} field). Must match frontend copy.
     */
    public static final String OLLAMA_MODEL_LABEL_PREFIX = "(local) ";

    /**
     * Comma-separated Vertex (Gemini) model IDs clients may select.
     */
    private String allowedModels = "gemini-2.5-flash";

    /**
     * Comma-separated Ollama model tags (e.g. {@code deepseek-r1:1.5b}). Ollama must be reachable at
     * {@code spring.ai.ollama.base-url} when these are used.
     */
    private String ollamaAllowedModels = "";

    public String getAllowedModels() {
        return allowedModels;
    }
    
    public void setAllowedModels(String allowedModels) {
        this.allowedModels = allowedModels;
    }

    public String getOllamaAllowedModels() {
        return ollamaAllowedModels;
    }

    public void setOllamaAllowedModels(String ollamaAllowedModels) {
        this.ollamaAllowedModels = ollamaAllowedModels;
    }

    /** Vertex / cloud model IDs from {@code app.chat.allowed-models}. */
    public List<String> vertexAllowedModelsList() {
        return splitCsv(allowedModels);
    }

    /** Local Ollama model tags from {@code app.chat.ollama-allowed-models}. */
    public List<String> ollamaAllowedModelsList() {
        return splitCsv(ollamaAllowedModels);
    }

    /** Union of Vertex and Ollama IDs — used to validate {@code POST /chat} {@code model}. */
    public List<String> allAllowedModelsList() {
        return java.util.stream.Stream.concat(
                vertexAllowedModelsList().stream(),
                ollamaAllowedModelsList().stream())
                .toList();
    }

    public boolean isVertexModel(String modelId) {
        return modelId != null && vertexAllowedModelsList().contains(modelId.trim());
    }

    public boolean isOllamaModel(String modelId) {
        return modelId != null && ollamaAllowedModelsList().contains(modelId.trim());
    }

    @PostConstruct
    void assertVertexAndOllamaListsDisjoint() {
        Set<String> vertex = new HashSet<>(vertexAllowedModelsList());
        Set<String> ollama = new HashSet<>(ollamaAllowedModelsList());
        vertex.retainAll(ollama);
        if (!vertex.isEmpty()) {
            throw new IllegalStateException(
                    "app.chat.allowed-models and app.chat.ollama-allowed-models must not contain the same id. "
                            + "Overlap: "
                            + vertex);
        }
    }

    private static List<String> splitCsv(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
