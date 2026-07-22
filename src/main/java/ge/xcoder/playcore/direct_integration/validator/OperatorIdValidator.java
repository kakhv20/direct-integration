package ge.xcoder.playcore.direct_integration.validator;

import ge.xcoder.playcore.direct_integration.exception.security.MissingMandatoryHeaderException;
import ge.xcoder.playcore.direct_integration.exception.security.WrongOperatorException;

public class OperatorIdValidator {
    private final String operatorId;

    public OperatorIdValidator(String operatorId) {
        this.operatorId = operatorId;
    }

    public void validate(String id) {
        if (id == null || id.isBlank()) {
            throw new MissingMandatoryHeaderException("Missing operator id");
        } else if (!operatorId.equals(id)) {
            throw new WrongOperatorException("Invalid operator");
        }
    }
}
