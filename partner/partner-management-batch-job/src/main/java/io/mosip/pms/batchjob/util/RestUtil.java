package io.mosip.pms.batchjob.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
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

import java.util.List;
import java.util.Map;

@Component
public class RestUtil {

    private static final Logger LOGGER = LoggerConfiguration.logConfig(RestUtil.class);
    private static final String RESPONSE = "response";
    private static final String ERRORS = "errors";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_MESSAGE = "message";

    @Qualifier("selfTokenRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public <T> T sendRequest(
            String baseUrl,
            HttpMethod method,
            Object requestBody,
            Class<T> responseType,
            MediaType mediaType
    ) {
        try {
            // Execute API call
            Map<String, Object> responseBody = executeApiCall(baseUrl, method, requestBody, Map.class, mediaType);

            // Validate and handle response errors
            handleApiErrors(responseBody, baseUrl);

            if (!responseBody.containsKey(RESPONSE) || responseBody.get(RESPONSE) == null) {
                LOGGER.error("API request to '{}' succeeded but response is missing or contains null for key '{}'", baseUrl, RESPONSE);
                throw new BatchJobServiceException(
                        ErrorCodes.API_NULL_RESPONSE.getCode(),
                        ErrorCodes.API_NULL_RESPONSE.getMessage()
                );
            }

            return objectMapper.convertValue(responseBody.get(RESPONSE), responseType);
        } catch (BatchJobServiceException e) {
            LOGGER.error("API request to '{}' failed: {}", baseUrl, e.getMessage());
            throw e;
        } catch (HttpStatusCodeException e) {
            LOGGER.error("API request to '{}' failed with HTTP {} - Response: {}",
                    baseUrl, e.getStatusCode(), e.getResponseBodyAsString());
            throw new BatchJobServiceException(
                    ErrorCodes.API_NOT_ACCESSIBLE.getCode(),
                    ErrorCodes.API_NOT_ACCESSIBLE.getMessage(),
                    e
            );
        } catch (Exception e) {
            LOGGER.error("Unexpected error during API request to '{}': ", baseUrl, e);
            throw new BatchJobServiceException(
                    ErrorCodes.API_NOT_ACCESSIBLE.getCode(),
                    ErrorCodes.API_NOT_ACCESSIBLE.getMessage(),
                    e
            );
        }
    }

    private <T> T executeApiCall(
            String url,
            HttpMethod method,
            Object requestBody,
            Class<T> responseType,
            MediaType mediaType
    ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(mediaType);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, httpHeaders);
        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

        return response.getBody();
    }

    private void handleApiErrors(Map<String, Object> responseBody, String baseUrl) {
        if (responseBody == null) {
            LOGGER.error("API request to '{}' failed: response is null.", baseUrl);
            throw new BatchJobServiceException(
                    ErrorCodes.API_NULL_RESPONSE.getCode(),
                    ErrorCodes.API_NULL_RESPONSE.getMessage()
            );
        }

        if (responseBody.containsKey(ERRORS) && responseBody.get(ERRORS) != null) {
            List<Map<String, Object>> errorList = (List<Map<String, Object>>) responseBody.get(ERRORS);
            if (!errorList.isEmpty()) {
                Map<String, Object> error = errorList.getFirst();
                String errorCode = error.getOrDefault(ERROR_CODE, ErrorCodes.UNABLE_TO_PROCESS.getCode()).toString();
                String errorMessage = error.getOrDefault(ERROR_MESSAGE, ErrorCodes.UNABLE_TO_PROCESS.getMessage()).toString();

                LOGGER.error("API request to '{}' failed: errorCode={}, errorMessage={}", baseUrl, errorCode, errorMessage);
                throw new BatchJobServiceException(errorCode, errorMessage);
            }
        }
    }

}
