package com.bobo.spring_ai_play;

/**
 * Body for {@code POST /chat}. {@code model} is optional; when set it must be a raw id from {@code GET /api/models}
 * ({@link ModelInfo#id()}), not the display label.
 */
public record ChatRequest(String prompt, String model) {
}
