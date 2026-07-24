package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.api.port.NonceStore;
import ge.xcoder.playcore.direct_integration.exception.RepeatedValueUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderUncheckedException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NonceValidator {
    public static final String MISSING_HEADER_MESSAGE = "Missing nonce";
    public static final String NONCE_ALREADY_USED = "Nonce already used";
    private final NonceStore store;
    private final long ttl;

    public NonceValidator(NonceStore store, @Value("${app.security.nonce-ttl}") long ttl) {
        this.store = store;
        this.ttl = ttl;
    }

    public void validate(String nonce) {
        if (nonce == null || nonce.isBlank()) {
            throw new MissingMandatoryHeaderUncheckedException(MISSING_HEADER_MESSAGE);
        }

        if (!store.storeIfAbsent(nonce, ttl)) {
            throw new RepeatedValueUncheckedException(NONCE_ALREADY_USED, ErrorCodes.INVALID_NONCE);
        }
    }
}
