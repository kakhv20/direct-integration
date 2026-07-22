package ge.xcoder.playcore.direct_integration.exception.handler;

import ge.xcoder.playcore.direct_integration.exception.BaseUncheckedException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BaseUncheckedException.class)
    public ResponseEntity<ApiErrorResponse> handle(BaseUncheckedException exception) {
        return ResponseEntity.ok(from(exception));
    }

    private static @NonNull ApiErrorResponse from(BaseUncheckedException exception) {
        return new ApiErrorResponse(exception.getErrorCode(), exception.getMessage());
    }
}
