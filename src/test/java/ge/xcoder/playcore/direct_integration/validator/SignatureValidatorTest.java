package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.InvalidSignatureUncheckedException;
import ge.xcoder.playcore.direct_integration.security.sign.SignatureGenerator;
import ge.xcoder.playcore.direct_integration.util.constants.HeaderNames;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class SignatureValidatorTest {
    private static final String SECRET = "operator-secret-key";

    private static final Map<String, Object> BODY = Map.of(
            "user_id", "1234",
            "currency", "USD",
            "game_id", "king_move",
            "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    );

    private static final Map<String, String> AUTH_HEADERS = Map.of(
            HeaderNames.X_NONCE, "b7a9d3e0f4124b6f9c1a",
            HeaderNames.X_OPERATOR_ID, "optimo-main",
            HeaderNames.X_TIMESTAMP, "1730379235"
    );

    private final SignatureValidator validator = new SignatureValidator(SECRET);

    @Test
    void correctlySignedRequest_passes() {
        String validSignature = SignatureGenerator.buildSignature(BODY, AUTH_HEADERS, SECRET);

        Assertions.assertDoesNotThrow(() -> validator.validate(BODY, AUTH_HEADERS, validSignature));
    }

    @Test
    void tamperedSignature_throwsInvalidSignature() {
        String validSignature = SignatureGenerator.buildSignature(BODY, AUTH_HEADERS, SECRET);
        String tampered = flipLastChar(validSignature);

        Assertions.assertThrows(InvalidSignatureUncheckedException.class,
                () -> validator.validate(BODY, AUTH_HEADERS, tampered));
    }

    @Test
    void signatureFromWrongSecret_throwsInvalidSignature() {
        // an attacker who does not hold our secret cannot forge a matching signature
        String forged = SignatureGenerator.buildSignature(BODY, AUTH_HEADERS, "not-the-real-secret");

        Assertions.assertThrows(InvalidSignatureUncheckedException.class,
                () -> validator.validate(BODY, AUTH_HEADERS, forged));
    }

    @Test
    void missingSignature_throwsMissingHeader() {
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class,
                () -> validator.validate(BODY, AUTH_HEADERS, null));
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class,
                () -> validator.validate(BODY, AUTH_HEADERS, ""));
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class,
                () -> validator.validate(BODY, AUTH_HEADERS, "   "));
    }

    private static String flipLastChar(String hex) {
        char last = hex.charAt(hex.length() - 1);
        char different = (last == 'a') ? 'b' : 'a';
        return hex.substring(0, hex.length() - 1) + different;
    }
}
