package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.exception.security.TimestampOutOfWindowException;
import ge.xcoder.playcore.direct_integration.exception.InvalidNumberFormatException;
import ge.xcoder.playcore.direct_integration.exception.security.DomainMissingHeaderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

class TimestampValidatorTest {
    private static final int NOW_SECONDS = 1730379235;
    private static final int WINDOW = 30;

    private TimestampValidator validator;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.ofEpochSecond(NOW_SECONDS), ZoneOffset.UTC);
        this.validator = new TimestampValidator(fixedClock, WINDOW);
    }

    @ParameterizedTest(name = "{0}s from now → valid={1}")
    @CsvSource({
            "  0, true",    // now
            "-30, true",    // 30s in the past  — boundary, inclusive
            " 30, true",    // 30s in the future — boundary, inclusive
            "-31, false",   // just outside, past
            " 31, false",   // just outside, future
    })
    void withinDriftWindowPasses_outsideThrows(int offsetSeconds, boolean valid) {
        Executable call = () -> validator.validate(String.valueOf(NOW_SECONDS + offsetSeconds));

        if (valid) {
            Assertions.assertDoesNotThrow(call);
        } else {
            Assertions.assertThrows(TimestampOutOfWindowException.class, call);
        }
    }

    @Test
    void missingTimestamp_throwsMissingHeader() {
        Assertions.assertThrows(DomainMissingHeaderException.class, () -> validator.validate(null));
        Assertions.assertThrows(DomainMissingHeaderException.class, () -> validator.validate(""));
        Assertions.assertThrows(DomainMissingHeaderException.class, () -> validator.validate("  "));
    }

    @Test
    void malformedTimestamp_throwsInvalidNumberFormat() {
        Assertions.assertThrows(InvalidNumberFormatException.class, () -> validator.validate("abc"));
    }

    @Test
    void overlongDigitsThatOverflowLong_throwsInvalidNumberFormat() {
        // all digits, so it passes the \d+ check, but too large for a long
        Assertions.assertThrows(InvalidNumberFormatException.class,
                () -> validator.validate("99999999999999999999999"));
    }
}
