package io.mosip.pms.tasklets.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ApiResponseValidatorUtil {
    private Logger log = PMSLogger.getLogger(ApiResponseValidatorUtil.class);

    public void validateApiResponse(Map<String, Object> response, String apiUrl) {
        if (response == null) {
            log.error("Received null response from API: {}", apiUrl);
            throw new BatchJobServiceException(ErrorCode.API_NULL_RESPONSE.getErrorCode(),
                    ErrorCode.API_NULL_RESPONSE.getErrorMessage());
        }
        if (response.containsKey(PartnerConstants.ERRORS)) {
            List<Map<String, Object>> errorList = (List<Map<String, Object>>) response.get(PartnerConstants.ERRORS);
            if (errorList != null && !errorList.isEmpty()) {
                log.error("Error occurred while fetching data: {}", errorList);
                throw new BatchJobServiceException(String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORCODE)),
                        String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORMESSAGE)));
            }
        }
        if (!response.containsKey(PartnerConstants.RESPONSE) || response.get(PartnerConstants.RESPONSE) == null) {
            log.error("Missing response data in API call: {}", apiUrl);
            throw new BatchJobServiceException(ErrorCode.API_NULL_RESPONSE.getErrorCode(),
                    ErrorCode.API_NULL_RESPONSE.getErrorMessage());
        }
    }
}
