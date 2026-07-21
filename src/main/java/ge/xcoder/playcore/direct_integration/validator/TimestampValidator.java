package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.exception.InvalidNumberFormatException;
import ge.xcoder.playcore.direct_integration.exception.security.DomainMissingHeaderException;
import ge.xcoder.playcore.direct_integration.exception.security.TimestampOutOfWindowException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

import java.time.Clock;

public class TimestampValidator {
    private final Clock clock;
    private final int plusMinusBoundary;

    public TimestampValidator(Clock clock, int plusMinusBoundary) {
        this.clock = clock;
        this.plusMinusBoundary = plusMinusBoundary;
    }

    /**
     * Passes when the timestamp is within +/- the configured boundary of now;
     * throws a coded exception when it is missing, malformed, or outside the window.
     *
     * @param number the X-Timestamp header value (epoch seconds)
     */
    public void validate(String number) {
        if (number == null || number.isBlank()) {
            throw new DomainMissingHeaderException("Missing timestamp");
        }
        if (!number.matches("\\d+")) {
            throw new InvalidNumberFormatException("Timestamp is not a number", ErrorCodes.INVALID_TIMESTAMP);
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(number);
        } catch (NumberFormatException e) {
            // all-digits but too large to fit in a long
            throw new InvalidNumberFormatException("Timestamp is out of range", ErrorCodes.INVALID_TIMESTAMP, e);
        }

        long now = clock.instant().getEpochSecond();
        long diff = now - timestamp;
        if (diff < -plusMinusBoundary || diff > plusMinusBoundary) {
            throw new TimestampOutOfWindowException("Timestamp off by " + diff, ErrorCodes.INVALID_TIMESTAMP);
        }
    }
}
