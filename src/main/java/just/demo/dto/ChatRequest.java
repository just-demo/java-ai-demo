package just.demo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /chat.
 */
public record ChatRequest(@NotBlank String question) {
}
