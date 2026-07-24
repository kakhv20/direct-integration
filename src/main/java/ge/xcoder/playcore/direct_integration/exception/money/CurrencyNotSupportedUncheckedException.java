package ge.xcoder.playcore.direct_integration.exception.money;

import ge.xcoder.playcore.direct_integration.exception.BaseUncheckedException;
import ge.xcoder.playcore.direct_integration.util.ResultCodes;

public class CurrencyNotSupportedUncheckedException extends BaseUncheckedException {
    public CurrencyNotSupportedUncheckedException(String message) {
        super(message, ResultCodes.CURRENCY_NOT_SUPPORTED);
    }
}
