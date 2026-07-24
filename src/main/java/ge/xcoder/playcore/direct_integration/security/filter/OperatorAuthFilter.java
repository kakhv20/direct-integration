package ge.xcoder.playcore.direct_integration.security.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ge.xcoder.playcore.direct_integration.api.CachedBodyHttpServletRequest;
import ge.xcoder.playcore.direct_integration.util.constants.HeaderNames;
import ge.xcoder.playcore.direct_integration.validator.NonceValidator;
import ge.xcoder.playcore.direct_integration.validator.OperatorIdValidator;
import ge.xcoder.playcore.direct_integration.validator.SignatureValidator;
import ge.xcoder.playcore.direct_integration.validator.TimestampValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@Order(2)
public class OperatorAuthFilter extends OncePerRequestFilter {
    private final OperatorIdValidator operatorIdValidator;
    private final TimestampValidator timestampValidator;
    private final NonceValidator nonceValidator;
    private final SignatureValidator signatureValidator;
    private final ObjectMapper mapper;

    public OperatorAuthFilter(OperatorIdValidator operatorIdValidator,
                              TimestampValidator timestampValidator,
                              NonceValidator nonceValidator,
                              SignatureValidator signatureValidator,
                              ObjectMapper mapper
    ) {
        this.operatorIdValidator = operatorIdValidator;
        this.timestampValidator = timestampValidator;
        this.nonceValidator = nonceValidator;
        this.signatureValidator = signatureValidator;
        this.mapper = mapper;
    }

    /**
     * Authenticates a single operator request before it is allowed to reach the controller.
     * <p>
     * The request is first wrapped in a {@link CachedBodyHttpServletRequest} so its body can be
     * read more than once - here, to recompute the signature over the raw bytes, and again
     * downstream by the controller. A raw servlet input stream is single-use, so without this the
     * controller would receive an empty body.
     * <p>
     * The checks run in a fixed order: operatorId, timestamp and nonce (header presence and
     * validity) first, then the signature last, since it recomputes the HMAC over the body plus
     * those same headers. Each validator throws a {@code BaseUncheckedException} on failure, and
     * those are intentionally <b>not</b> caught here: they propagate to the
     * {@link ExceptionHandlerFilter} (ordered ahead of this filter), which turns them into an
     * HTTP 200 response carrying the business {@code result_code}. When every check passes, the
     * cached request is forwarded down the chain.
     *
     * @param request     the incoming request; wrapped so its body survives re-reading
     * @param response    the response, passed through untouched on the success path
     * @param filterChain the rest of the chain, invoked only once all checks pass
     * @throws ServletException if a downstream filter or servlet raises it
     * @throws IOException      if reading the request body fails
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {
        CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);
        validate(cached);

        filterChain.doFilter(cached, response);
    }

    private void validate(HttpServletRequest request) throws IOException {
        String operatorId = request.getHeader(HeaderNames.X_OPERATOR_ID);
        String timestamp = request.getHeader(HeaderNames.X_TIMESTAMP);
        String nonce = request.getHeader(HeaderNames.X_NONCE);

        operatorIdValidator.validate(operatorId);
        timestampValidator.validate(timestamp);
        nonceValidator.validate(nonce);

        byte[] body = request.getInputStream().readAllBytes();
        Map<String, Object> bodyMap = mapper.readValue(body, new TypeReference<>() {
        });

        signatureValidator.validate(
                bodyMap,
                Map.of(
                        HeaderNames.X_OPERATOR_ID, operatorId,
                        HeaderNames.X_TIMESTAMP, timestamp,
                        HeaderNames.X_NONCE, nonce
                ),
                request.getHeader(HeaderNames.X_SIGN)
        );
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return false;
    }
}
