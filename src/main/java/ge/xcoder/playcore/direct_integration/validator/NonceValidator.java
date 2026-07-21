package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.api.port.NonceStore;
import ge.xcoder.playcore.direct_integration.exception.RepeatedValueException;
import ge.xcoder.playcore.direct_integration.exception.security.DomainMissingHeaderException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class NonceValidator {
    private final NonceStore store;

    public NonceValidator(NonceStore store) {
        this.store = store;
    }

    public void validate(String nonce) {
        if (nonce == null || nonce.isBlank()) {
            throw new DomainMissingHeaderException("Missing nonce");
        }

        if (!store.storeIfAbsent(nonce)) {
            throw new RepeatedValueException("Nonce already used", ErrorCodes.INVALID_NONCE);
        }
    }
}