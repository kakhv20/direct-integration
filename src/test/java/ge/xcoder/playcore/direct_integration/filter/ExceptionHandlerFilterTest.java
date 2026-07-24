package ge.xcoder.playcore.direct_integration.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ge.xcoder.playcore.direct_integration.zhelper.GeneralTestData;
import ge.xcoder.playcore.direct_integration.security.sign.SignatureGenerator;
import ge.xcoder.playcore.direct_integration.util.ResultCodes;
import ge.xcoder.playcore.direct_integration.util.constants.HeaderNames;
import ge.xcoder.playcore.direct_integration.validator.NonceValidator;
import ge.xcoder.playcore.direct_integration.validator.OperatorIdValidator;
import ge.xcoder.playcore.direct_integration.validator.SignatureValidator;
import ge.xcoder.playcore.direct_integration.validator.TimestampValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(GeneralTestData.class)
class ExceptionHandlerFilterTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private String bodyJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(GeneralTestData.BALANCE_REQUEST_BODY);
    }

    private static String validSignature(String nonce) {
        Map<String, String> authHeaders = Map.of(
                HeaderNames.X_OPERATOR_ID, GeneralTestData.OPERATOR_ID,
                HeaderNames.X_TIMESTAMP, String.valueOf(GeneralTestData.NOW),
                HeaderNames.X_NONCE, nonce
        );
        return SignatureGenerator.buildSignature(GeneralTestData.BALANCE_REQUEST_BODY, authHeaders, GeneralTestData.OPERATOR_SECRET_KEY);
    }

    @Test
    void missingHeaders_returns200WithResultCodeBody() throws Exception {
        mockMvc.perform(post("/test/guarded")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user_id\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result_code").value(ResultCodes.MISSING_HEADER))
                .andExpect(jsonPath("$.message").value(OperatorIdValidator.MISSING_HEADER_MESSAGE));
    }

    @Test
    void fullyValidRequest_passesTheGate_andReachesController() throws Exception {
        String nonce = "nonce-happy-path";                            // fresh: avoids the shared nonce store
        String sign = validSignature(nonce);

        mockMvc.perform(post("/test/guarded")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson())
                        .header(HeaderNames.X_OPERATOR_ID, GeneralTestData.OPERATOR_ID)
                        .header(HeaderNames.X_TIMESTAMP, String.valueOf(GeneralTestData.NOW))
                        .header(HeaderNames.X_NONCE, nonce)
                        .header(HeaderNames.X_SIGN, sign))
                .andExpect(status().isOk())
                .andExpect(content().string("reached the controller"));   // the stub controller ran
    }

    @Test
    void missingTimestamp() throws Exception {
        mockMvc.perform(post("/test/guarded")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson())
                        .header(HeaderNames.X_OPERATOR_ID, GeneralTestData.OPERATOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result_code").value(ResultCodes.MISSING_HEADER))
                .andExpect(jsonPath("$.message").value(TimestampValidator.MISSING_HEADER_MESSAGE));
    }

    @Test
    void missingNonce() throws Exception {
        mockMvc.perform(post("/test/guarded")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson())
                        .header(HeaderNames.X_OPERATOR_ID, GeneralTestData.OPERATOR_ID)
                        .header(HeaderNames.X_TIMESTAMP, String.valueOf(GeneralTestData.NOW)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result_code").value(ResultCodes.MISSING_HEADER))
                .andExpect(jsonPath("$.message").value(NonceValidator.MISSING_HEADER_MESSAGE));
    }

    @Test
    void missingSignature() throws Exception {
        mockMvc.perform(post("/test/guarded")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson())
                        .header(HeaderNames.X_OPERATOR_ID, GeneralTestData.OPERATOR_ID)
                        .header(HeaderNames.X_TIMESTAMP, String.valueOf(GeneralTestData.NOW))
                        .header(HeaderNames.X_NONCE, "nonce-missing-sig"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result_code").value(ResultCodes.MISSING_HEADER))
                .andExpect(jsonPath("$.message").value(SignatureValidator.MISSING_HEADER_MESSAGE));
    }

    @Test
    void invalidSignature() throws Exception {
        mockMvc.perform(post("/test/guarded")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson())
                        .header(HeaderNames.X_OPERATOR_ID, GeneralTestData.OPERATOR_ID)
                        .header(HeaderNames.X_TIMESTAMP, String.valueOf(GeneralTestData.NOW))
                        .header(HeaderNames.X_NONCE, "nonce-invalid-sig")
                        .header(HeaderNames.X_SIGN, "INVALID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result_code").value(ResultCodes.INVALID_SIGNATURE))
                .andExpect(jsonPath("$.message").value(SignatureValidator.INVALID_SIGNATURE));
    }

    @Test
    void invalidTimestamp() throws Exception {
        long stale = GeneralTestData.NOW - GeneralTestData.PLUS_MINUS_BOUNDARY * 2;   // outside the +/-30s window
        mockMvc.perform(post("/test/guarded")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson())
                        .header(HeaderNames.X_OPERATOR_ID, GeneralTestData.OPERATOR_ID)
                        .header(HeaderNames.X_TIMESTAMP, String.valueOf(stale)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result_code").value(ResultCodes.INVALID_TIMESTAMP))
                .andExpect(jsonPath("$.message").value(TimestampValidator.TIMESTAMP_IS_OUT_OF_VALID_TIMEFRAME));
    }

    @Test
    void repeatNonce() throws Exception {
        String nonce = "nonce-repeat";
        String sign = validSignature(nonce);

        // first use - passes the whole chain
        mockMvc.perform(post("/test/guarded")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson())
                        .header(HeaderNames.X_OPERATOR_ID, GeneralTestData.OPERATOR_ID)
                        .header(HeaderNames.X_TIMESTAMP, String.valueOf(GeneralTestData.NOW))
                        .header(HeaderNames.X_NONCE, nonce)
                        .header(HeaderNames.X_SIGN, sign))
                .andExpect(status().isOk());

        // replay - same nonce, now rejected
        mockMvc.perform(post("/test/guarded")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson())
                        .header(HeaderNames.X_OPERATOR_ID, GeneralTestData.OPERATOR_ID)
                        .header(HeaderNames.X_TIMESTAMP, String.valueOf(GeneralTestData.NOW))
                        .header(HeaderNames.X_NONCE, nonce)
                        .header(HeaderNames.X_SIGN, sign))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result_code").value(ResultCodes.INVALID_NONCE))
                .andExpect(jsonPath("$.message").value(NonceValidator.NONCE_ALREADY_USED));
    }
}
