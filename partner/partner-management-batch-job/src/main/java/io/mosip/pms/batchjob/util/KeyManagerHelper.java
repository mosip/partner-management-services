package io.mosip.pms.batchjob.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.dto.OriginalCertDownloadResponseDto;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Component
public class KeyManagerHelper {

    private static final Logger LOGGER = LoggerConfiguration.logConfig(KeyManagerHelper.class);

    private static final String PARTNER_CERT_ID = "partnerCertId";

    @Value("${pmp.partner.original.certificate.get.rest.uri}")
    private String keyManagerPartnerCertificateUrl;

    @Autowired
    RestUtil restUtil;

    @Autowired
    ObjectMapper objectMapper;

    public OriginalCertDownloadResponseDto getPartnerCertificate(String certificateAlias) {
        try {
            Map<String, String> pathSegments = new HashMap<>();
            pathSegments.put(PARTNER_CERT_ID, certificateAlias);

            // Build the URL
            String urlWithPath = UriComponentsBuilder.fromUriString(keyManagerPartnerCertificateUrl)
                    .buildAndExpand(pathSegments)
                    .toUriString();

            // Call the API using RestUtil
            Map<String, Object> response = restUtil.sendRequest(urlWithPath, HttpMethod.GET, null, Map.class);

            if (response == null || response.isEmpty()) {
                throw new BatchJobServiceException(ErrorCodes.API_NULL_RESPONSE.getCode(), ErrorCodes.API_NULL_RESPONSE.getMessage());
            }

            return objectMapper.convertValue(response.get("response"), OriginalCertDownloadResponseDto.class);

        } catch (BatchJobServiceException e) {
            LOGGER.error("Error fetching partner certificate: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while fetching partner certificate", e);
            throw new BatchJobServiceException(ErrorCodes.PARTNER_CERTIFICATE_FETCH_ERROR.getCode(), ErrorCodes.PARTNER_CERTIFICATE_FETCH_ERROR.getMessage());
        }
    }

}
