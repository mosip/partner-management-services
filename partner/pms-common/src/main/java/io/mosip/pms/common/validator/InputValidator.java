package io.mosip.pms.common.validator;

import io.mosip.pms.common.constant.ValidationErrorCode;
import io.mosip.pms.common.exception.RequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
/**
 * InputValidator is responsible for validating input strings against a predefined regex pattern.
 */
@Component
public class InputValidator {

    @Value("${mosip.pms.request.input.validation.regex}")
    private String requestInputValidationRegex;

    public void validateRequestInput(String input) {
        if (input != null && !input.isBlank()) {
            if (!Pattern.compile(requestInputValidationRegex).matcher(input).matches()) {
                throw new RequestException(
                        ValidationErrorCode.INVALID_INPUT_VALUE.getErrorCode(),
                        String.format(ValidationErrorCode.INVALID_INPUT_VALUE.getErrorMessage(), input)
                );
            }
        }
    }
}