package ge.xcoder.playcore.direct_integration.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.xcoder.playcore.direct_integration.exception.handler.ApiErrorResponse;
import ge.xcoder.playcore.direct_integration.exception.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void baseException_mapsTo200WithCodeAndMessage() {
        long errorCode = 1002L;
        String badOp = "bad op";
        ResponseEntity<ApiErrorResponse> response = handler.handle(new BaseUncheckedException(badOp, errorCode));

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = response.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(errorCode, body.resultCode());
        Assertions.assertEquals(badOp, body.message());
    }

    @Test
    void serializesResultCodeAsSnakeCase() throws Exception {
        String json = new ObjectMapper()
                .writeValueAsString(new ApiErrorResponse(1002L, "bad op"));
        Assertions.assertTrue(json.contains("\"result_code\":1002"));
    }
}
