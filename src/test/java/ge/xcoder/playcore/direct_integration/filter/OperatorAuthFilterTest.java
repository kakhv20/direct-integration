package ge.xcoder.playcore.direct_integration.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ge.xcoder.playcore.direct_integration.InMemoryNonceStore;
import ge.xcoder.playcore.direct_integration.exception.BaseUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.RepeatedValueUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.InvalidSignatureUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.TimestampOutOfWindowUncheckedException;
import ge.xcoder.playcore.direct_integration.security.filter.OperatorAuthFilter;
import ge.xcoder.playcore.direct_integration.security.sign.SignatureGenerator;
import ge.xcoder.playcore.direct_integration.util.ErrorCodes;
import ge.xcoder.playcore.direct_integration.util.constants.HeaderNames;
import ge.xcoder.playcore.direct_integration.validator.NonceValidator;
import ge.xcoder.playcore.direct_integration.validator.OperatorIdValidator;
import ge.xcoder.playcore.direct_integration.validator.SignatureValidator;
import ge.xcoder.playcore.direct_integration.validator.TimestampValidator;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

class OperatorAuthFilterTest {
    public static final String OPERATOR_ID = "optimo-main";
    public static final long NOW_SECONDS = 1730379235;
    public static final int TIMESTAMP_PLUS_MINUS_BORDER = 30;
    public static final Map<String, Object> BODY = Map.of(
            "user_id", "1234",
            "currency", "USD",
            "game_id", "king_move",
            "session_id", "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    );
    public static final String NONCE = "b7a9d3e0f4124b6f9c1a";
    private static final String SECRET = "operator-secret-key";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, String> AUTH_HEADERS = Map.of(
            HeaderNames.X_NONCE, NONCE,
            HeaderNames.X_OPERATOR_ID, OPERATOR_ID,
            HeaderNames.X_TIMESTAMP, String.valueOf(NOW_SECONDS)
    );
    private OperatorAuthFilter authFilter;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        authFilter = new OperatorAuthFilter(
                new OperatorIdValidator(OPERATOR_ID),
                new TimestampValidator(Clock.fixed(Instant.ofEpochSecond(NOW_SECONDS), ZoneOffset.UTC), TIMESTAMP_PLUS_MINUS_BORDER),
                new NonceValidator(new InMemoryNonceStore()),
                new SignatureValidator(SECRET),
                new ObjectMapper()
        );
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent(MAPPER.writeValueAsBytes(BODY));
    }

    @Test
    void happyPath_everythingWorks() {
        AUTH_HEADERS.forEach(request::addHeader);
        request.addHeader(HeaderNames.X_SIGN, SignatureGenerator.buildSignature(BODY, AUTH_HEADERS, SECRET));

        MockFilterChain filterChain = new MockFilterChain();

        Assertions.assertDoesNotThrow(() -> authFilter.doFilter(
                request, new MockHttpServletResponse(), filterChain
        ));

        Assertions.assertNotNull(filterChain.getRequest());
    }

    @Test
    void noAuthHeaders_Reject() {
        MockFilterChain filterChain = new MockFilterChain();

        BaseUncheckedException ex = Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> authFilter.doFilter(
                request, new MockHttpServletResponse(), filterChain
        ));

        Assertions.assertEquals(ErrorCodes.MISSING_HEADER, ex.getErrorCode());
        Assertions.assertEquals(OperatorIdValidator.MISSING_HEADER_MESSAGE, ex.getMessage());

        Assertions.assertNull(filterChain.getRequest());
    }

    @Test
    void headersPutOperatorId_throwsExceptionForTimestamp() {
        request.addHeader(HeaderNames.X_OPERATOR_ID, OPERATOR_ID);

        MockFilterChain filterChain = new MockFilterChain();

        BaseUncheckedException ex = Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> authFilter.doFilter(
                request, new MockHttpServletResponse(), filterChain
        ));

        Assertions.assertEquals(ErrorCodes.MISSING_HEADER, ex.getErrorCode());
        Assertions.assertEquals(TimestampValidator.MISSING_HEADER_MESSAGE, ex.getMessage());

        Assertions.assertNull(filterChain.getRequest());
    }

    @Test
    void headersPutOperatorIdAndTimestamp_throwsExceptionForNonce() {
        request.addHeader(HeaderNames.X_OPERATOR_ID, OPERATOR_ID);
        request.addHeader(HeaderNames.X_TIMESTAMP, String.valueOf(NOW_SECONDS));

        MockFilterChain filterChain = new MockFilterChain();

        BaseUncheckedException ex = Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> authFilter.doFilter(
                request, new MockHttpServletResponse(), filterChain
        ));

        Assertions.assertEquals(ErrorCodes.MISSING_HEADER, ex.getErrorCode());
        Assertions.assertEquals(NonceValidator.MISSING_HEADER_MESSAGE, ex.getMessage());

        Assertions.assertNull(filterChain.getRequest());
    }

    @Test
    void headersPutOperatorIdAndTimestampAndNonce_throwsExceptionForSignature() {
        request.addHeader(HeaderNames.X_OPERATOR_ID, OPERATOR_ID);
        request.addHeader(HeaderNames.X_TIMESTAMP, NOW_SECONDS);
        request.addHeader(HeaderNames.X_NONCE, NONCE);

        MockFilterChain filterChain = new MockFilterChain();

        BaseUncheckedException ex = Assertions.assertThrows(MissingMandatoryHeaderUncheckedException.class, () -> authFilter.doFilter(
                request, new MockHttpServletResponse(), filterChain
        ));

        Assertions.assertEquals(ErrorCodes.MISSING_HEADER, ex.getErrorCode());
        Assertions.assertEquals(SignatureValidator.MISSING_HEADER_MESSAGE, ex.getMessage());

        Assertions.assertNull(filterChain.getRequest());
    }

    @Test
    void passesReReadableBodyDownstream() throws Exception {
        AUTH_HEADERS.forEach(request::addHeader);
        request.addHeader(HeaderNames.X_SIGN, SignatureGenerator.buildSignature(BODY, AUTH_HEADERS, SECRET));

        AtomicReference<byte[]> downstreamBody = new AtomicReference<>();
        FilterChain readingChain = (req, res) ->
                downstreamBody.set(req.getInputStream().readAllBytes());   // ← acts like the controller

        authFilter.doFilter(request, new MockHttpServletResponse(), readingChain);

        Assertions.assertArrayEquals(MAPPER.writeValueAsBytes(BODY), downstreamBody.get());
    }

    @Test
    void invalidSignature_InvalidSignatureException() {
        AUTH_HEADERS.forEach(request::addHeader);
        request.addHeader(HeaderNames.X_SIGN, "INVALID");

        MockFilterChain filterChain = new MockFilterChain();

        BaseUncheckedException ex = Assertions.assertThrows(InvalidSignatureUncheckedException.class, () -> authFilter.doFilter(
                request, new MockHttpServletResponse(), filterChain
        ));

        Assertions.assertEquals(ErrorCodes.INVALID_SIGNATURE, ex.getErrorCode());
        Assertions.assertEquals(SignatureValidator.INVALID_SIGNATURE, ex.getMessage());

        Assertions.assertNull(filterChain.getRequest());
    }

    @Test
    void invalidTimestamp_TimestampOutOfWindowException() {
        request.addHeader(HeaderNames.X_OPERATOR_ID, OPERATOR_ID);
        request.addHeader(HeaderNames.X_TIMESTAMP, String.valueOf(NOW_SECONDS - 2 * TIMESTAMP_PLUS_MINUS_BORDER));
        request.addHeader(HeaderNames.X_NONCE, NONCE);
        request.addHeader(HeaderNames.X_SIGN, SignatureGenerator.buildSignature(BODY, AUTH_HEADERS, SECRET));

        MockFilterChain filterChain = new MockFilterChain();

        BaseUncheckedException ex = Assertions.assertThrows(TimestampOutOfWindowUncheckedException.class, () -> authFilter.doFilter(
                request, new MockHttpServletResponse(), filterChain
        ));

        Assertions.assertEquals(ErrorCodes.INVALID_TIMESTAMP, ex.getErrorCode());
        Assertions.assertEquals(TimestampValidator.TIMESTAMP_IS_OUT_OF_VALID_TIMEFRAME, ex.getMessage());

        Assertions.assertNull(filterChain.getRequest());
    }

    @Test
    void repeatNonce_RepeatedValueException() {
        AUTH_HEADERS.forEach(request::addHeader);
        request.addHeader(HeaderNames.X_SIGN, SignatureGenerator.buildSignature(BODY, AUTH_HEADERS, SECRET));

        MockFilterChain firstFilterChain = new MockFilterChain();
        MockFilterChain secondFilterChain = new MockFilterChain();

        Assertions.assertDoesNotThrow(() -> authFilter.doFilter(
                request, new MockHttpServletResponse(), firstFilterChain
        ));

        Assertions.assertNotNull(firstFilterChain.getRequest());

        BaseUncheckedException ex = Assertions.assertThrows(RepeatedValueUncheckedException.class, () -> authFilter.doFilter(
                request, new MockHttpServletResponse(), secondFilterChain
        ));

        Assertions.assertEquals(ErrorCodes.INVALID_NONCE, ex.getErrorCode());
        Assertions.assertEquals(NonceValidator.NONCE_ALREADY_USED, ex.getMessage());

        Assertions.assertNull(secondFilterChain.getRequest());
    }
}
