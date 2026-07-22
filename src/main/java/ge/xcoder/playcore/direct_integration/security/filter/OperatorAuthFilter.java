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
     * check operatorId -> check timestamp -> check nonce -> check signature
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return false;
    }
}
