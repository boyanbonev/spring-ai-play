package com.bobo.spring_ai_play;

/**
 * One selectable model in {@code GET /api/models}.
 *
 * @param id    Raw model id sent in {@code POST /chat} ({@code model} field).
 * @param label Display string for the UI; Ollama entries use {@link ChatProperties#OLLAMA_MODEL_LABEL_PREFIX}.
 */
public record ModelInfo(String id, String label) {
}
