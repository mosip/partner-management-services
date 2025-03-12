package io.mosip.pms.batchjob.util;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@EnableCaching
public class KeycloakHelper {
    private static final Logger LOGGER = LoggerConfiguration.logConfig(KeycloakHelper.class);

    private static final String PARTNER_ADMIN = "PARTNER_ADMIN";
    private static final String USER_ROLE = "userRole";
    private static final String USER_NAME = "username";

    @Value("${mosip.iam.role-users-url}")
    private String roleUsersUrl;

    @Autowired
    RestHelper restHelper;

    @Cacheable(value = "partnerAdminIdsCache", key = "'partnerAdminIds'", unless = "#result.isEmpty()")
    public List<String> getPartnerIdsWithPartnerAdminRole() {
        List<String> partnerIds = new ArrayList<>();

        try {
            Map<String, String> pathSegments = Map.of(USER_ROLE, PARTNER_ADMIN);

            // Build the URL
            String urlWithPath = UriComponentsBuilder.fromUriString(roleUsersUrl)
                    .buildAndExpand(pathSegments)
                    .toUriString();

            LOGGER.info("Fetching Partner Admin user IDs from URL: {}", urlWithPath);

            // Send API request
            Object response = restHelper.executeApiCall(urlWithPath, HttpMethod.GET, null, Object.class, MediaType.APPLICATION_JSON);

            if (response instanceof List<?> usersList) {
                for (Object userObj : usersList) {
                    if (userObj instanceof LinkedHashMap<?, ?> userMap) {
                        partnerIds.add(String.valueOf(userMap.get(USER_NAME)));
                    }
                }
            } else {
                LOGGER.error("Unexpected API response format while fetching Partner Admin user IDs.");
                throw new BatchJobServiceException(
                        ErrorCodes.FETCH_PARTNER_ADMIN_USER_IDS_ERROR.getCode(),
                        "Invalid response format received from API."
                );
            }
        } catch (BatchJobServiceException e) {
            LOGGER.error("Failed to fetch Partner Admin user IDs: {}", e.getMessage(), e);
            throw e;
        } catch (HttpStatusCodeException e) {
            LOGGER.error("API request failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BatchJobServiceException(
                    ErrorCodes.API_NOT_ACCESSIBLE.getCode(),
                    "Failed to access the API: " + e.getMessage(),
                    e
            );
        } catch (Exception e) {
            LOGGER.error("Error occurred while fetching Partner Admin user IDs: {}", e.getMessage(), e);
            throw new BatchJobServiceException(
                    ErrorCodes.FETCH_PARTNER_ADMIN_USER_IDS_ERROR.getCode(),
                    "An Error occurred while retrieving Partner Admin user IDs.",
                    e
            );
        }
        return partnerIds;
    }

}
