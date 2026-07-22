package ge.xcoder.playcore.direct_integration.exception.handler;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ApiErrorResponse(
        Long resultCode,
        String message
) {
}
