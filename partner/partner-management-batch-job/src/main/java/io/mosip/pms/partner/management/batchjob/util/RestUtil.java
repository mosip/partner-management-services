package io.mosip.pms.partner.management.batchjob.util;

import io.mosip.pms.partner.management.batchjob.config.LoggerConfiguration;
import io.mosip.pms.partner.management.batchjob.constants.ErrorCodes;
import io.mosip.pms.partner.management.batchjob.exceptions.PartnerBatchJobServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.slf4j.Logger;

@Component
public class RestUtil {

    private static final Logger LOGGER = LoggerConfiguration.logConfig(RestUtil.class);

    @Qualifier("selfTokenRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    public <T> T sendRequest(
            String url,
            HttpMethod method,
            Object requestBody,
            Class<T> responseType
    ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, httpHeaders);

        try {
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            LOGGER.error("HTTP error: Status Code {} - Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PartnerBatchJobServiceException(ErrorCodes.API_NOT_ACCESSIBLE.getCode(), ErrorCodes.API_NOT_ACCESSIBLE.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error occurred while making HTTP request: ", e);
            throw new PartnerBatchJobServiceException(ErrorCodes.API_NOT_ACCESSIBLE.getCode(), ErrorCodes.API_NOT_ACCESSIBLE.getMessage(), e);
        }
    }
}
