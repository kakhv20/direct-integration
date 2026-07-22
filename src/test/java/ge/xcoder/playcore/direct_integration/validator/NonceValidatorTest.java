package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.InMemoryNonceStore;
import ge.xcoder.playcore.direct_integration.exception.RepeatedValueException;
import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NonceValidatorTest {

    @Test
    void firstUseSucceeds_reuseThrows() {
        NonceValidator validator = new NonceValidator(new InMemoryNonceStore());

        String nonce = "nonce-abc";
        Assertions.assertDoesNotThrow(() -> validator.validate(nonce));
        Assertions.assertThrows(RepeatedValueException.class, () -> validator.validate(nonce));
    }

    @Test
    void nullOrBlankNonce_throwsMissingHeader() {
        NonceValidator validator = new NonceValidator(new InMemoryNonceStore());

        Assertions.assertThrows(MissingMandatoryHeaderException.class, () -> validator.validate(null));
        Assertions.assertThrows(MissingMandatoryHeaderException.class, () -> validator.validate(""));
        Assertions.assertThrows(MissingMandatoryHeaderException.class, () -> validator.validate("   "));
    }
}
