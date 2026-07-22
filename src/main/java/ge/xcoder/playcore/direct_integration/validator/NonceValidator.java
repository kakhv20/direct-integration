package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.api.port.NonceStore;
import ge.xcoder.playcore.direct_integration.exception.RepeatedValueException;
import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

public class NonceValidator {
    public static final String MISSING_HEADER_MESSAGE = "Missing nonce";
    public static final String NONCE_ALREADY_USED = "Nonce already used";
    private final NonceStore store;

    public NonceValidator(NonceStore store) {
        this.store = store;
    }

    public void validate(String nonce) {
        if (nonce == null || nonce.isBlank()) {
            throw new MissingMandatoryHeaderException(MISSING_HEADER_MESSAGE);
        }

        if (!store.storeIfAbsent(nonce)) {
            throw new RepeatedValueException(NONCE_ALREADY_USED, ErrorCodes.INVALID_NONCE);
        }
    }
}