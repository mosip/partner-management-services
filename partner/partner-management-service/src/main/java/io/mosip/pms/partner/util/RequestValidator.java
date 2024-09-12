package io.mosip.pms.partner.util;

import io.micrometer.core.lang.NonNull;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Component
public class RequestValidator {

    public static final String VERSION = "1.0";

    private static final Logger LOGGER = PMSLogger.getLogger(RequestValidator.class);

    public void validateId(String operation, String requestId) {
        if (Objects.nonNull(requestId)) {
            if (!requestId.equals(operation)) {
                LOGGER.error("", "", "validateId", "\n" + "Id is not correct");
                throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_ID.getErrorCode(),
                        ErrorCode.INVALID_REQUEST_ID.getErrorMessage());
            }
        } else {
            LOGGER.error("", "", "validateId", "\n" + "Id is null");
            throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_ID.getErrorCode(),
                    ErrorCode.INVALID_REQUEST_ID.getErrorMessage());
        }
    }

    public <T> Optional<ResponseWrapperV2<T>> validate(@NonNull String operation, RequestWrapper<?> requestWrapper) {
        try {
            validateId(operation, requestWrapper.getId());
            validateReqTime(requestWrapper.getRequesttime());
            validateVersion(requestWrapper.getVersion());
            validateRequest(requestWrapper.getRequest());
        } catch (PartnerServiceException ex) {
            ResponseWrapperV2<T> responseWrapper = new ResponseWrapperV2<>();
            responseWrapper.setId(operation);
            responseWrapper.setVersion(VERSION);
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
            return Optional.of(responseWrapper);
        }
        return Optional.empty();
    }

    protected void validateReqTime(LocalDateTime reqTime) {
        if (Objects.isNull(reqTime)) {
            LOGGER.error("", "", "validateReqTime", "requesttime is null");
            throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_DATETIME.getErrorCode(), ErrorCode.INVALID_REQUEST_DATETIME.getErrorMessage());
        } else {
            LocalDate localDate = reqTime.toLocalDate();
            LocalDate serverDate = new Date().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
            if (localDate.isBefore(serverDate) || localDate.isAfter(serverDate)) {
                throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE.getErrorCode(),
                        ErrorCode.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE.getErrorMessage());
            }
        }
    }

    protected void validateVersion(String ver) {
        if (Objects.isNull(ver)) {
            LOGGER.error("", "", "validateVersion", "version is null");
            throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_VERSION.getErrorCode(),
                    ErrorCode.INVALID_REQUEST_VERSION.getErrorMessage());
        } else if (!VERSION.equalsIgnoreCase(ver)) {
            LOGGER.error("", "", "validateVersion", "version is not correct");
            throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_VERSION.getErrorCode(),
                    ErrorCode.INVALID_REQUEST_VERSION.getErrorMessage());
        }
    }

    protected void validateRequest(Object request) {
        if (Objects.isNull(request)) {
            LOGGER.error("", "", "validateRequest", "\n" + "request is null");
            throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_BODY.getErrorCode(),
                    ErrorCode.INVALID_REQUEST_BODY.getErrorMessage());
        }
    }
}
