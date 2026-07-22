package ge.xcoder.playcore.direct_integration.security;

import ge.xcoder.playcore.direct_integration.exception.money.CurrencyNotSupportedUncheckedException;
import ge.xcoder.playcore.direct_integration.security.sign.SignatureGenerator;
import ge.xcoder.playcore.direct_integration.util.constants.HeaderNames;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class SignatureGeneratorTest {
    /**
     * Spec Example 1, step 2 output - the sorted query string. It is also step 3's input,
     * which is why it is a shared constant rather than an inline literal.
     */
    private static final String SPEC_QUERY_STRING =
            "X-Nonce=b7a9d3e0f4124b6f9c1a"
                    + "&X-Operator-Id=optimo-main"
                    + "&X-Timestamp=1730379235"
                    + "&currency=USD"
                    + "&game_id=king_move"
                    + "&session_id=f47ac10b-58cc-4372-a567-0e02b2c3d479"
                    + "&user_id=1234";
    /**
     * Spec Example 1, step 3 output - the URL-encoded string that gets HMAC'd.
     */
    private static final String SPEC_ENCODED_STRING =
            "X-Nonce%3Db7a9d3e0f4124b6f9c1a"
                    + "%26X-Operator-Id%3Doptimo-main"
                    + "%26X-Timestamp%3D1730379235"
                    + "%26currency%3DUSD"
                    + "%26game_id%3Dking_move"
                    + "%26session_id%3Df47ac10b-58cc-4372-a567-0e02b2c3d479"
                    + "%26user_id%3D1234";

    /**
     * Golden vector from the integration docs - Security > X-Sign Calculation, Example 1
     * (balance request). Expected value is copied verbatim from the spec; do not "fix" it.
     * <p>
     * Note the X-* header keys sort BEFORE the lowercase body fields ('X' is 0x58, 'a' is
     * 0x61). That only holds under ASCII/natural ordering - a case-insensitive comparator
     * would put them last and every signature would mismatch.
     */
    @Test
    void mapToStringSorted_shouldMatchSpecExample() {
        Map<String, String> input = Map.of(
                "user_id", "1234",
                "currency", "USD",
                "game_id", "king_move",
                "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                HeaderNames.X_OPERATOR_ID, "optimo-main",
                HeaderNames.X_TIMESTAMP, "1730379235",
                HeaderNames.X_NONCE, "b7a9d3e0f4124b6f9c1a"
        );

        Assertions.assertEquals(SPEC_QUERY_STRING, SignatureGenerator.mapToStringSorted(input));
    }

    /**
     * Golden vector from the integration docs - Example 1, step 3. Fed the spec's own
     * step-2 string rather than the output of {@code mapToStringSorted}, so a bug in the
     * sort cannot mask a bug in the encoder.
     * <p>
     * The whole joined string is encoded as one unit - that is why '=' becomes %3D and
     * '&' becomes %26. It looks like a double-encoded query string; it is what the spec
     * requires.
     */
    @Test
    void urlEncode_shouldMatchSpecExample() {
        Assertions.assertEquals(SPEC_ENCODED_STRING, SignatureGenerator.urlEncode(SPEC_QUERY_STRING));
    }

    /**
     * Pins which URL-encoding dialect we use. {@link java.net.URLEncoder} implements
     * application/x-www-form-urlencoded, NOT RFC 3986 percent-encoding. The two disagree on
     * exactly these characters, and no spec example exercises any of them.
     * <p>
     * OPEN QUESTION for the account manager: does the aggregator use form-encoding or
     * RFC 3986? If RFC 3986, these expectations flip (' ' -> %20, '~' -> ~, '*' -> %2A)
     * and any signature containing those characters currently mismatches. Exposure is the
     * free-form ID fields: user_id, transaction_id, round_id, gift_id.
     */
    @Test
    void urlEncode_shouldUseFormEncodingNotRfc3986() {
        Assertions.assertEquals("+", SignatureGenerator.urlEncode(" "));
        Assertions.assertEquals("%7E", SignatureGenerator.urlEncode("~"));
        Assertions.assertEquals("*", SignatureGenerator.urlEncode("*"));

        // A literal '+' must survive as %2B, otherwise it would be ambiguous with an
        // encoded space.
        Assertions.assertEquals("%2B", SignatureGenerator.urlEncode("+"));
    }

    @Test
    void objectMapToStringMap_shouldDropAttributesAndStringifyValues() {
        final Map<String, Object> body = Map.of(
                "user_id", "1234",
                "currency", "USD",
                "game_id", "king_move",
                "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                "attributes", Map.of("test1", "test value1")
        );

        final Map<String, String> expected = Map.of(
                "user_id", "1234",
                "currency", "USD",
                "game_id", "king_move",
                "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479"
        );

        Assertions.assertEquals(expected, SignatureGenerator.objectMapToStringMap(body));
    }

    @Test
    void objectMapToStringMap_shouldNormalizeAmountAtCurrencyPrecision() {
        final Map<String, Object> body = Map.of(
                "user_id", "1234",
                "currency", "USD",
                "amount", new BigDecimal("10000000.0"),
                "game_id", "king_move",
                "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                "attributes", Map.of("test1", "test value1")
        );

        final Map<String, String> expected = Map.of(
                "user_id", "1234",
                "currency", "USD",
                "amount", "10000000.00",
                "game_id", "king_move",
                "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479"
        );
        Assertions.assertEquals(expected, SignatureGenerator.objectMapToStringMap(body));
    }

    @Test
    void objectMapToStringMap_shouldDropNullValuedFields() {
        // The aggregator excludes null fields from the signing string (confirmed with the
        // account manager) - e.g. a null parent_transaction_id on a JACKPOT/CLOSE_ROUND win.
        final Map<String, Object> body = new HashMap<>();
        body.put("user_id", "1234");
        body.put("parent_transaction_id", null);

        final Map<String, String> expected = Map.of(
                "user_id", "1234"
        );
        Assertions.assertEquals(expected, SignatureGenerator.objectMapToStringMap(body));
    }

    @Test
    void objectMapToStringMapWrongCurrency_shouldRejectUnsupportedCurrency() {
        final Map<String, Object> body = Map.of(
                "user_id", "1234",
                "currency", "BHD",
                "amount", new BigDecimal("100.0"),
                "game_id", "king_move",
                "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                "attributes", Map.of("test1", "test value1")
        );
        Assertions.assertThrows(CurrencyNotSupportedUncheckedException.class, () -> SignatureGenerator.objectMapToStringMap(body));
    }

    @Test
    void objectMapToStringMapNoCurrency_shouldRejectUnsupportedCurrency() {
        final Map<String, Object> body = Map.of(
                "user_id", "1234",
                "amount", new BigDecimal("100.0"),
                "game_id", "king_move",
                "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                "attributes", Map.of("test1", "test value1")
        );
        Assertions.assertThrows(CurrencyNotSupportedUncheckedException.class, () -> SignatureGenerator.objectMapToStringMap(body));
    }

    @Test
    void hmacSha1_matchesKnownVector() {
        String data = "The quick brown fox jumps over the lazy dog";
        String result = "de7c9b85b8b78aa6bc8a7a36f70a90701c9db4d9";
        String generated = SignatureGenerator.hmacSha1(data, "key");
        Assertions.assertEquals(result, generated);
        Assertions.assertEquals(generated.toLowerCase(), generated);
        Assertions.assertEquals(40, generated.length());
    }

    /**
     * End-to-end wiring test for the whole X-Sign pipeline: render body fields (drop
     * attributes) -> merge auth headers -> sort -> join -> URL-encode -> HMAC-SHA1.
     * <p>
     * The body + headers are spec Example 1 (balance request), so the intermediate
     * URL-encoded string is exactly {@link #SPEC_ENCODED_STRING}. The expected digest was
     * computed independently with {@code openssl dgst -sha1 -hmac} over that string with
     * the secret below (the docs give no final HMAC - the header table value is a
     * placeholder), and cross-checked against a standalone JDK Mac run.
     * <p>
     * {@code attributes} is included in the input specifically to prove it is dropped by
     * the full chain, not just by the unit under {@code objectMapToStringMap}.
     */
    @Test
    void buildSignature_matchesEndToEndVector() {
        final String secret = "operator-secret-key";

        final Map<String, Object> body = Map.of(
                "user_id", "1234",
                "currency", "USD",
                "game_id", "king_move",
                "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                "attributes", Map.of("test1", "test value1")
        );

        final Map<String, String> authHeaders = Map.of(
                HeaderNames.X_NONCE, "b7a9d3e0f4124b6f9c1a",
                HeaderNames.X_OPERATOR_ID, "optimo-main",
                HeaderNames.X_TIMESTAMP, "1730379235"
        );

        Assertions.assertEquals(
                "a8d166c038a97de174674c93277378065e849ce5",
                SignatureGenerator.buildSignature(body, authHeaders, secret));
    }
}
