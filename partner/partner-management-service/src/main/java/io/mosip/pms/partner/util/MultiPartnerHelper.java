package io.mosip.pms.partner.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MultiPartnerHelper {

    private static final Logger LOGGER = PMSLogger.getLogger(MultiPartnerHelper.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Environment environment;

    @Autowired
    PartnerServiceRepository partnerRepository;

    @Autowired
    RestUtil restUtil;

    public <T> T getCertificateFromKeyMgr(PartnerCertDownloadRequestDto certDownloadRequestDto,
                                             String uriProperty, Class<T> responseType)
            throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
        String userId = getUserId();
        List<Partner> partnerList = partnerRepository.findByUserId(userId);
        if (partnerList.isEmpty()) {
            LOGGER.error("sessionId", "idType", "id", "User id does not exists.");
            throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                    ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
        }
        boolean isPartnerBelongsToTheUser = false;
        for (Partner partner: partnerList) {
            if (partner.getId().equals(certDownloadRequestDto.getPartnerId())) {
                isPartnerBelongsToTheUser = true;
            }
        }
        T responseObject = null;
        if (isPartnerBelongsToTheUser) {
            Optional<Partner> partnerFromDb = partnerRepository.findById(certDownloadRequestDto.getPartnerId());
            if (partnerFromDb.isEmpty()) {
                LOGGER.error("Partner not exists with id {}", certDownloadRequestDto.getPartnerId());
                throw new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
                        ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
            }
            if (partnerFromDb.get().getCertificateAlias() == null || partnerFromDb.get().getCertificateAlias().isEmpty()) {
                LOGGER.error("Cert is not uploaded for given partner {}", certDownloadRequestDto.getPartnerId());
                throw new PartnerServiceException(ErrorCode.CERTIFICATE_NOT_UPLOADED_EXCEPTION.getErrorCode(),
                        ErrorCode.CERTIFICATE_NOT_UPLOADED_EXCEPTION.getErrorMessage());
            }

            Map<String, String> pathsegments = new HashMap<>();
            pathsegments.put("partnerCertId", partnerFromDb.get().getCertificateAlias());
            Map<String, Object> getApiResponse = restUtil
                    .getApi(environment.getProperty(uriProperty), pathsegments, Map.class);
            responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")), responseType);

            if (responseObject == null && getApiResponse.containsKey(PartnerConstants.ERRORS)) {
                List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) getApiResponse
                        .get(PartnerConstants.ERRORS);
                if (!certServiceErrorList.isEmpty()) {
                    LOGGER.error("Error occurred while getting the cert from keymanager");
                    throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
                            certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
                } else {
                    LOGGER.error("Error occurred while getting the cert {}", getApiResponse);
                    throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
                            ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
                }
            }

            if (responseObject == null) {
                LOGGER.error("Got null response from {}", environment.getProperty(uriProperty));
                throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
                        ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
            }
        } else {
            LOGGER.error("sessionId", "idType", "id", "The given partner ID does not belong to the user.");
            throw new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_BELONG_TO_THE_USER.getErrorCode(),
                    ErrorCode.PARTNER_DOES_NOT_BELONG_TO_THE_USER.getErrorMessage());
        }

        return responseObject;
    }

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getUserId() {
        String userId = authUserDetails().getUserId();
        return userId;
    }
}