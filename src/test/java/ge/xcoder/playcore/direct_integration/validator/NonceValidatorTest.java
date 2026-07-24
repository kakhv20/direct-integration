package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.InMemoryNonceStore;
import ge.xcoder.playcore.direct_integration.exception.RepeatedValueUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderUncheckedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NonceValidatorTest {
    private static final long TTL = 77;
    private NonceValidator validator;
    private InMemoryNonceStore store;

    @BeforeEach
    void setup() {
        store = new InMemoryNonceStore();
        validator = new NonceValidator(store, TTL);
    }

    @Test
    void firstUseSucceeds_reuseThrows() {
        String nonce = "nonce-abc";

        Assertions.assertDoesNotThrow(() -> validator.validate(nonce));
        Assertions.assertThrows(RepeatedValueUncheckedException.class, () -> validator.validate(nonce));
    }

    @Test
    void nullOrBlankNonce_throwsMissingHeader() {
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> validator.validate(null));
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> validator.validate(""));
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> validator.validate("   "));
    }

    @Test
    void firstUseSucceedsTTLIsPassed_returnsCorrectTTL() {
        String nonce = "nonce-abc";

        Assertions.assertDoesNotThrow(() -> validator.validate(nonce));
        Assertions.assertEquals(TTL, store.getLastTtlUsed());
    }
}
