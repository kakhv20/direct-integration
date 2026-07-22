package ge.xcoder.playcore.direct_integration.formatter;

import ge.xcoder.playcore.direct_integration.exception.InvalidNumberFormatUncheckedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AmountFormatterTest {

    @Test
    void normalize_shouldReturnNormalizedString() {
        Assertions.assertEquals("0.00", AmountFormatter.normalizeForSignatureCheck("0", 2));
        Assertions.assertEquals("1.00", AmountFormatter.normalizeForSignatureCheck("1", 2));
        Assertions.assertEquals("0.00", AmountFormatter.normalizeForSignatureCheck("0.0", 2));
        Assertions.assertEquals("1.00", AmountFormatter.normalizeForSignatureCheck("1.00", 2));
        Assertions.assertEquals("10.00", AmountFormatter.normalizeForSignatureCheck("10.006", 2));
    }

    @Test
    void normalizeInvalidValues_shouldThrowException() {
        Assertions.assertThrows(InvalidNumberFormatUncheckedException.class, () -> AmountFormatter.normalizeForSignatureCheck(".", 2));
        Assertions.assertThrows(InvalidNumberFormatUncheckedException.class, () -> AmountFormatter.normalizeForSignatureCheck("abc", 2));
        Assertions.assertThrows(InvalidNumberFormatUncheckedException.class, () -> AmountFormatter.normalizeForSignatureCheck(null, 2));
        Assertions.assertThrows(InvalidNumberFormatUncheckedException.class, () -> AmountFormatter.normalizeForSignatureCheck("null", 2));
    }
}
