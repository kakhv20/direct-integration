package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.exception.security.InvalidSignatureException;
import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderException;
import ge.xcoder.playcore.direct_integration.security.sign.SignatureGenerator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

public class SignatureValidator {
    public static final String MISSING_HEADER_MESSAGE = "Missing signature";
    public static final String INVALID_SIGNATURE = "Invalid signature";
    private final String secret;

    public SignatureValidator(String secret) {
        this.secret = secret;
    }

    public void validate(Map<String, Object> body,
                         Map<String, String> authHeaders,
                         String providedSignature
    ) {
        if (providedSignature == null || providedSignature.isBlank()) {
            throw new MissingMandatoryHeaderException(MISSING_HEADER_MESSAGE);
        }
        String expected = SignatureGenerator.buildSignature(body, authHeaders, secret);
        // Constant-time comparison: MessageDigest.isEqual does not short-circuit on the
        // first differing byte, so it does not leak (via timing) how much of the signature
        // matched. A plain String.equals() would be a timing side-channel.

        // Example:
        // String.equals (and naive byte loops) short-circuit — they stop at the first differing byte. An attacker who can measure response times can exploit that: a guess that matches the first
        // 10 bytes takes microscopically longer to reject than one that fails at byte 1, so they can brute-force the signature one byte at a time. MessageDigest.isEqual compares all bytes
        // regardless, leaking no timing signal.
        boolean matches = MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8));

        if (!matches) {
            throw new InvalidSignatureException(INVALID_SIGNATURE);
        }
    }
}
