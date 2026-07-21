package ge.xcoder.playcore.direct_integration.validator;

import java.time.Clock;

public class TimestampValidator {
    private final Clock clock;
    private final int plusMinusBoundary;

    public TimestampValidator(Clock clock, int plusMinusBoundary) {
        this.clock = clock;
        this.plusMinusBoundary = plusMinusBoundary;
    }

    /**
     * +- 30 seconds is valid
     *
     * @param number timestamp in string
     * @return true if timestamp is valid
     */
    public boolean isValid(String number) {
        long timestamp = Long.parseLong(number);
        long now = clock.instant().getEpochSecond();
        long diff = now - timestamp;
        return !(diff < -plusMinusBoundary || diff > plusMinusBoundary);
    }
}
