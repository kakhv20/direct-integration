package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderUncheckedException;
import ge.xcoder.playcore.direct_integration.exception.security.WrongOperatorUncheckedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OperatorIdValidator {
    public static final String MISSING_HEADER_MESSAGE = "Missing operatorId";
    public static final String INVALID_OPERATOR = "Invalid operator";
    private final String operatorId;

    public OperatorIdValidator(@Value("${app.security.operator-id}") String operatorId) {
        this.operatorId = operatorId;
    }

    public void validate(String id) {
        if (id == null || id.isBlank()) {
            throw new MissingMandatoryHeaderUncheckedException(MISSING_HEADER_MESSAGE);
        } else if (!operatorId.equals(id)) {
            throw new WrongOperatorUncheckedException(INVALID_OPERATOR);
        }
    }
}
