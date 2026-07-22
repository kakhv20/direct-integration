package ge.xcoder.playcore.direct_integration.security.sign;

import ge.xcoder.playcore.direct_integration.exception.money.CurrencyNotSupportedUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.HmacComputationUncheckedException;
import ge.xcoder.playcore.direct_integration.formatter.AmountFormatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

public class SignatureGenerator {
    private static final String ALGORITHM = "HmacSHA1";

    private SignatureGenerator() {
    }

    public static String mapToStringSorted(Map<String, String> payload) {
        return payload.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("");
    }

    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

// TODO: this should be tested once the DTOs are in
//    public static Map<String, String> objectToMap(Object body) {
//        final Map<String, Object> unfilteredBodyFields = objectMapper.copy()
//                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
//                .convertValue(body, new TypeReference<>() {
//                });
//
//        return objectMapToStringMap(unfilteredBodyFields);
//    }

    /**
     * Renders a parsed request body into the flat {@code String -> String} map that feeds the
     * signing string: drops {@code attributes}, drops null-valued fields, stringifies every
     * remaining value, and normalizes {@code amount} to its currency's precision.
     * <p>
     * Null-valued fields (e.g. a null {@code parent_transaction_id} on a JACKPOT or
     * CLOSE_ROUND win) are excluded from the signing string entirely - this matches the
     * aggregator's own signing (confirmed with the account manager).
     */
    public static Map<String, String> objectMapToStringMap(Map<String, Object> unfilteredBodyFields) {
        final Map<String, String> filteredBodyFields = unfilteredBodyFields.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("attributes"))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.valueOf(entry.getValue())
                ));

        if (filteredBodyFields.containsKey("amount")) {
            filteredBodyFields.compute("amount", (k, amount) -> AmountFormatter.normalizeForSignatureCheck(amount, precisionFor(filteredBodyFields.get("currency"))));
        }
        return filteredBodyFields;
    }

    private static int precisionFor(String currency) {
        if (currency == null) {
            throw new CurrencyNotSupportedUncheckedException("Currency is not specified");
        }
        return switch (currency) {
            case "USD", "EUR", "GEL" -> 2;
            default -> throw new CurrencyNotSupportedUncheckedException(currency + " is not supported");
        };
    }

    public static String hmacSha1(String data, String key) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new HmacComputationUncheckedException("Failed to compute HMAC signature", e);
        }
    }

    public static String buildSignature(Map<String, Object> body, Map<String, String> authHeaders, String secret) {
        Map<String, String> input = new HashMap<>(objectMapToStringMap(body));
        input.putAll(authHeaders);

        return hmacSha1(urlEncode(mapToStringSorted(input)), secret);
    }
}
