package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.InMemoryNonceStore;
import ge.xcoder.playcore.direct_integration.exception.RepeatedValueUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderUncheckedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NonceValidatorTest {

    @Test
    void firstUseSucceeds_reuseThrows() {
        NonceValidator validator = new NonceValidator(new InMemoryNonceStore());

        String nonce = "nonce-abc";
        Assertions.assertDoesNotThrow(() -> validator.validate(nonce));
        Assertions.assertThrows(RepeatedValueUncheckedException.class, () -> validator.validate(nonce));
    }

    @Test
    void nullOrBlankNonce_throwsMissingHeader() {
        NonceValidator validator = new NonceValidator(new InMemoryNonceStore());

        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> validator.validate(null));
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> validator.validate(""));
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> validator.validate("   "));
    }
}
