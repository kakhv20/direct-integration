package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderException;
import ge.xcoder.playcore.direct_integration.exception.security.WrongOperatorException;

public class OperatorIdValidator {
    public static final String MISSING_HEADER_MESSAGE = "Missing operatorId";
    public static final String INVALID_OPERATOR = "Invalid operator";
    private final String operatorId;

    public OperatorIdValidator(String operatorId) {
        this.operatorId = operatorId;
    }

    public void validate(String id) {
        if (id == null || id.isBlank()) {
            throw new MissingMandatoryHeaderException(MISSING_HEADER_MESSAGE);
        } else if (!operatorId.equals(id)) {
            throw new WrongOperatorException(INVALID_OPERATOR);
        }
    }
}
