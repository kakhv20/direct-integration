package ge.xcoder.playcore.direct_integration.formatter;

import ge.xcoder.playcore.direct_integration.exception.InvalidNumberFormatException;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmountFormatter {
    private AmountFormatter() {
    }

    public static String normalizeForSignatureCheck(String amount, int precision) {
        if (amount == null || amount.isEmpty() || !amount.matches("-?\\d+(\\.\\d+)?")) {
            throw new InvalidNumberFormatException("Amount is not a number", ErrorCodes.INVALID_AMOUNT);
        }
        return new BigDecimal(amount)
                .setScale(precision, RoundingMode.DOWN)
                .toPlainString();
    }
}
