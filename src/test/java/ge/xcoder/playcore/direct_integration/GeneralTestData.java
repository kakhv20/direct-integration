package ge.xcoder.playcore.direct_integration;

import ge.xcoder.playcore.direct_integration.validator.NonceValidator;
import ge.xcoder.playcore.direct_integration.validator.OperatorIdValidator;
import ge.xcoder.playcore.direct_integration.validator.SignatureValidator;
import ge.xcoder.playcore.direct_integration.validator.TimestampValidator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

@TestConfiguration
public class GeneralTestData {
    public static final long NOW = 1730379235;
    public static final int PLUS_MINUS_BOUNDARY = 30;
    public static final String OPERATOR_ID = "optimo-main";
    public static final String OPERATOR_SECRET_KEY = "operator-secret-key";

    public static final Map<String, Object> BALANCE_REQUEST_BODY = Map.of(
            "user_id", "1234",
            "currency", "USD",
            "game_id", "king_move",
            "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    );

    @Bean
    public OperatorIdValidator operatorIdValidator() {
        return new OperatorIdValidator(OPERATOR_ID);
    }

    @Bean
    public TimestampValidator timestampValidator() {
        return new TimestampValidator(Clock.fixed(Instant.ofEpochSecond(NOW), ZoneOffset.UTC), PLUS_MINUS_BOUNDARY);
    }

    @Bean
    public NonceValidator nonceValidator() {
        return new NonceValidator(new InMemoryNonceStore(), 30);
    }

    @Bean
    public SignatureValidator signatureValidator() {
        return new SignatureValidator(OPERATOR_SECRET_KEY);
    }

    @RestController
    static class StubController {
        @PostMapping("/test/guarded")
        String guarded() {
            return "reached the controller";
        }
    }
}