package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.WrongOperatorUncheckedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OperatorIdValidatorTest {
    public static final String VALID_ID = "123456789";
    private OperatorIdValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OperatorIdValidator(VALID_ID);
    }

    @Test
    void inputCorrectId_passes() {
        Assertions.assertDoesNotThrow(() -> validator.validate(VALID_ID));
    }

    @Test
    void inputIncorrectId_fails() {
        Assertions.assertThrows(WrongOperatorUncheckedException.class, () -> validator.validate(VALID_ID + " "));
    }

    @Test
    void inputEmptyValues_throwsException() {
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> validator.validate(null));
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> validator.validate(""));
        Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> validator.validate("  "));
    }
}
