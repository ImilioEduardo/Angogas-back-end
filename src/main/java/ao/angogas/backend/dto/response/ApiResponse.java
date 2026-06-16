package ao.angogas.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final List<FieldError> errors;
    private final String timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message("Operação realizada com sucesso")
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static ApiResponse<?> error(String message) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static ApiResponse<?> error(String message, List<FieldError> errors) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(Instant.now().toString())
                .build();
    }

    public record FieldError(String campo, String mensagem) {}
}
