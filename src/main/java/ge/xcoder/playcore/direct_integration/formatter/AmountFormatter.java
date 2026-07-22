package ge.xcoder.playcore.direct_integration.formatter;

import ge.xcoder.playcore.direct_integration.exception.InvalidNumberFormatUncheckedException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmountFormatter {
    private AmountFormatter() {
    }

    public static String normalizeForSignatureCheck(String amount, int precision) {
        if (amount == null || amount.isEmpty() || !amount.matches("-?\\d+(\\.\\d+)?")) {
            throw new InvalidNumberFormatUncheckedException("Amount is not a number");
        }
        return new BigDecimal(amount)
                .setScale(precision, RoundingMode.DOWN)
                .toPlainString();
    }
}
