package ge.xcoder.playcore.direct_integration.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

class TimestampValidatorTest {

    @ParameterizedTest(name = "{0}s from now → valid={1}")
    @CsvSource({
            "  0, true",    // now
            "-30, true",    // 30s in the past  — boundary, inclusive
            " 30, true",    // 30s in the future — boundary, inclusive
            "-31, false",   // just outside, past
            " 31, false",   // just outside, future
    })
    void isValidOnlyWithinDriftWindow(int offsetSeconds, boolean expected) {
        int nowSeconds = 1730379235;
        int window = 30;
        Clock fixedClock = Clock.fixed(Instant.ofEpochSecond(nowSeconds), ZoneOffset.UTC);
        var validator = new TimestampValidator(fixedClock, window);

        Assertions.assertEquals(
                expected,
                validator.isValid(String.valueOf(nowSeconds + offsetSeconds)));
    }

    // TODO: test with a wrong format of the timestamp
}
