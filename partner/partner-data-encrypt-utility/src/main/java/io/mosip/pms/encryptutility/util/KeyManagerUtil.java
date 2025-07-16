package io.mosip.pms.encryptutility.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.CryptoRequestDto;
import io.mosip.pms.common.dto.CryptoResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.encryptutility.constants.ErrorCode;
import io.mosip.pms.encryptutility.exception.PartnerEncryptUtilityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KeyManagerUtil {

    private Logger logger = PMSLogger.getLogger(KeyManagerUtil.class);

    @Value("${pmp.encrypt.data.post.rest.uri}")
    private String keyManagerEncryptDataUrl;

    @Value("${mosip.service.keymanager.crypto.appId}")
    private String appId;

    @Value("${mosip.service.keymanager.crypto.refId}")
    private String refId;

    @Autowired
    RestUtil restUtil;

    @Autowired
    ObjectMapper objectMapper;

    public String encryptData(String data) {
        try {
            CryptoRequestDto cryptoRequestDto = new CryptoRequestDto();
            cryptoRequestDto.setData(CryptoUtil.encodeToPlainBase64(data.getBytes(StandardCharsets.UTF_8)));
            cryptoRequestDto.setApplicationId(appId);
            cryptoRequestDto.setReferenceId(refId);
            cryptoRequestDto.setTimeStamp(LocalDateTime.now());

            RequestWrapperV2<CryptoRequestDto> requestWrapper = new RequestWrapperV2<>();
            requestWrapper.setRequest(cryptoRequestDto);

            Map<String, Object> response = restUtil.postApi(
                    keyManagerEncryptDataUrl,
                    null,
                    "",
                    "",
                    MediaType.APPLICATION_JSON,
                    requestWrapper,
                    Map.class
            );
            if (response == null) {
                logger.debug("Received null response from API: {}", keyManagerEncryptDataUrl);
                throw new PartnerEncryptUtilityException(ErrorCode.API_NULL_RESPONSE.getErrorCode(),
                        ErrorCode.API_NULL_RESPONSE.getErrorMessage());
            }
            if (response.containsKey(PartnerConstants.ERRORS)) {
                List<Map<String, Object>> errorList = (List<Map<String, Object>>) response.get(PartnerConstants.ERRORS);
                if (errorList != null && !errorList.isEmpty()) {
                    logger.debug("Error occurred while fetching data: {}", errorList);
                    throw new PartnerEncryptUtilityException(String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORCODE)),
                            String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORMESSAGE)));
                }
            }
            if (!response.containsKey(PartnerConstants.RESPONSE) || response.get(PartnerConstants.RESPONSE) == null) {
                logger.debug("Missing response data in API call: {}", keyManagerEncryptDataUrl);
                throw new PartnerEncryptUtilityException(ErrorCode.API_NULL_RESPONSE.getErrorCode(),
                        ErrorCode.API_NULL_RESPONSE.getErrorMessage());
            }
            CryptoResponseDto responseDto = objectMapper.convertValue(
                    response.get("response"), CryptoResponseDto.class);
            return responseDto.getData();
        } catch (Exception ex) {
            logger.debug("Exception occurred while encrypting data: {}", ex.getMessage(), ex);
            throw new PartnerEncryptUtilityException(ErrorCode.ENCRYPTION_FAILED.getErrorCode(), ErrorCode.ENCRYPTION_FAILED.getErrorMessage());
        }
    }
} 