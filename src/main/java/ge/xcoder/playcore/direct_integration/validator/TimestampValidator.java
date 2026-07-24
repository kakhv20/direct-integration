package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.exception.InvalidNumberFormatUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.TimestampOutOfWindowUncheckedException;
import ge.xcoder.playcore.direct_integration.util.ResultCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class TimestampValidator {
    public static final String MISSING_HEADER_MESSAGE = "Missing timestamp";
    public static final String TIMESTAMP_IS_NOT_A_NUMBER = "Timestamp is not a number";
    public static final String TIMESTAMP_IS_OUT_OF_RANGE = "Timestamp is out of range";
    public static final String TIMESTAMP_IS_OUT_OF_VALID_TIMEFRAME = "Timestamp is out of valid timeframe";
    private final Clock clock;
    private final int plusMinusBoundary;

    public TimestampValidator(Clock clock,
                              @Value("${app.security.timestamp-plus-minus-boundary}") int plusMinusBoundary) {
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
            throw new MissingMandatoryHeaderUncheckedException(MISSING_HEADER_MESSAGE);
        }
        if (!number.matches("\\d+")) {
            throw new InvalidNumberFormatUncheckedException(TIMESTAMP_IS_NOT_A_NUMBER, ResultCodes.INVALID_TIMESTAMP);
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(number);
        } catch (NumberFormatException e) {
            // all-digits but too large to fit in the long
            throw new InvalidNumberFormatUncheckedException(TIMESTAMP_IS_OUT_OF_RANGE, ResultCodes.INVALID_TIMESTAMP, e);
        }

        long now = clock.instant().getEpochSecond();
        long diff = now - timestamp;
        if (diff < -plusMinusBoundary || diff > plusMinusBoundary) {
            throw new TimestampOutOfWindowUncheckedException(TIMESTAMP_IS_OUT_OF_VALID_TIMEFRAME, ResultCodes.INVALID_TIMESTAMP);
        }
    }
}
