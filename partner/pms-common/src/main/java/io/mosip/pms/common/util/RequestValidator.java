package io.mosip.pms.common.util;

import io.micrometer.core.lang.NonNull;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.ValidationErrorCode;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.request.dto.ErrorResponse;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class RequestValidator {

    public static final String VERSION = "1.0";

    private static final Logger LOGGER = PMSLogger.getLogger(RequestValidator.class);

    public void validateId(String operation, String requestId) {
        if (Objects.nonNull(requestId)) {
            if (!requestId.equals(operation)) {
                LOGGER.error("", "", "validateId", "\n" + "Id is not correct");
                throw new RequestException(ValidationErrorCode.INVALID_REQUEST_ID.getErrorCode(),
                        ValidationErrorCode.INVALID_REQUEST_ID.getErrorMessage());
            }
        } else {
            LOGGER.error("", "", "validateId", "\n" + "Id is null");
            throw new RequestException(ValidationErrorCode.INVALID_REQUEST_ID.getErrorCode(),
                    ValidationErrorCode.INVALID_REQUEST_ID.getErrorMessage());
        }
    }

    public <T> Optional<ResponseWrapperV2<T>> validate(@NonNull String operation, RequestWrapperV2<?> requestWrapper) {
        try {
            validateId(operation, requestWrapper.getId());
            validateReqTime(requestWrapper.getRequestTime());
            validateVersion(requestWrapper.getVersion());
            validateRequest(requestWrapper.getRequest());
        } catch (RequestException ex) {
            ResponseWrapperV2<T> responseWrapper = new ResponseWrapperV2<>();
            responseWrapper.setId(operation);
            responseWrapper.setVersion(VERSION);
            responseWrapper.setErrors(setErrorResponse(ex.getErrors().get(0).getErrorCode(), ex.getErrors().get(0).getMessage()));
            return Optional.of(responseWrapper);
        }
        return Optional.empty();
    }

    protected void validateReqTime(LocalDateTime reqTime) {
        if (Objects.isNull(reqTime)) {
            LOGGER.error("", "", "validateReqTime", "requesttime is null");
            throw new RequestException(ValidationErrorCode.INVALID_REQUEST_DATETIME.getErrorCode(),
                    ValidationErrorCode.INVALID_REQUEST_DATETIME.getErrorMessage());
        } else {
            LocalDate localDate = reqTime.toLocalDate();
            LocalDate serverDate = new Date().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
            if (localDate.isBefore(serverDate) || localDate.isAfter(serverDate)) {
                throw new RequestException(ValidationErrorCode.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE.getErrorCode(),
                        ValidationErrorCode.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE.getErrorMessage());
            }
        }
    }

    protected void validateVersion(String ver) {
        if (Objects.isNull(ver)) {
            LOGGER.error("", "", "validateVersion", "version is null");
            throw new RequestException(ValidationErrorCode.INVALID_REQUEST_VERSION.getErrorCode(),
                    ValidationErrorCode.INVALID_REQUEST_VERSION.getErrorMessage());
        } else if (!VERSION.equalsIgnoreCase(ver)) {
            LOGGER.error("", "", "validateVersion", "version is not correct");
            throw new RequestException(ValidationErrorCode.INVALID_REQUEST_VERSION.getErrorCode(),
                    ValidationErrorCode.INVALID_REQUEST_VERSION.getErrorMessage());
        }
    }

    protected void validateRequest(Object request) {
        if (Objects.isNull(request)) {
            LOGGER.error("", "", "validateRequest", "\n" + "request is null");
            throw new RequestException(ValidationErrorCode.INVALID_REQUEST_BODY.getErrorCode(),
                    ValidationErrorCode.INVALID_REQUEST_BODY.getErrorMessage());
        }
    }

    public static List<ErrorResponse> setErrorResponse(String errorCode, String errorMessage) {
        List<ErrorResponse> errorResponseList = new ArrayList<>();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(errorCode);
        errorResponse.setMessage(errorMessage);
        errorResponseList.add(errorResponse);
        return errorResponseList;
    }
}
