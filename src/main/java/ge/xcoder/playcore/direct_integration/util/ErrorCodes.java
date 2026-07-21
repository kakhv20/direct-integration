package ge.xcoder.playcore.direct_integration.util;

public final class ErrorCodes {
    public static long MISSING_HEADER = 1200;
    public static long INVALID_TIMESTAMP = 1202;
    public static long INVALID_NONCE = 1203;
    public static long INVALID_AMOUNT = 1001; // TODO: check out the documentation
    public static long CURRENCY_NOT_SUPPORTED = 1005;

    private ErrorCodes() {
    }
}
