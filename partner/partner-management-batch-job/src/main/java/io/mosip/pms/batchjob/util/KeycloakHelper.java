package io.mosip.pms.batchjob.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.util.RestUtil;

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
	RestUtil restUtil;

	@Autowired
	BatchJobHelper batchJobHelper;
	
	@Cacheable(value = "partnerAdminIdsCache", key = "'partnerAdminIds'", unless = "#result.isEmpty()")
	public List<Partner> getPartnerIdsWithPartnerAdminRole() {
		List<String> keycloakPartnerAdmins = new ArrayList<>();
		List<Partner> pmsPartnerAdmins = new ArrayList<Partner>();

		try {
			Map<String, String> pathSegments = Map.of(USER_ROLE, PARTNER_ADMIN);

			// Build the URL
			String urlWithPath = UriComponentsBuilder.fromUriString(roleUsersUrl).buildAndExpand(pathSegments)
					.toUriString();

			LOGGER.info("Fetching Partner Admin user IDs from URL: {}", urlWithPath);

			// Send API request
			Object response = restUtil.getApiWithContentType(roleUsersUrl, pathSegments, Object.class,
					MediaType.APPLICATION_JSON);

			if (response instanceof List<?> usersList) {
				for (Object userObj : usersList) {
					if (userObj instanceof LinkedHashMap<?, ?> userMap) {
						keycloakPartnerAdmins.add(String.valueOf(userMap.get(USER_NAME)));
					}
				}
			} else {
				LOGGER.error("Unexpected API response format while fetching Partner Admin user IDs. {}");
				throw new BatchJobServiceException(ErrorCodes.FETCH_PARTNER_ADMIN_USER_IDS_ERROR.getCode(),
						"Invalid response format received from API.");
			}
			LOGGER.info("KeyCloak returned {} Partner Admin users.", keycloakPartnerAdmins.size());
			pmsPartnerAdmins = batchJobHelper.getValidPartnerAdminsInPms(keycloakPartnerAdmins);
			LOGGER.info("PMS has {} Active Partner Admin users.", pmsPartnerAdmins.size());
		} catch (HttpStatusCodeException e) {
			LOGGER.error("API request failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
			throw new BatchJobServiceException(ErrorCodes.API_NOT_ACCESSIBLE.getCode(),
					"Failed to access the API: " + e.getMessage(), e);
		} catch (BatchJobServiceException e) {
			LOGGER.error("Failed to fetch Partner Admin user IDs: {}", e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error("Error occurred while fetching Partner Admin user IDs: {}", e.getMessage(), e);
		}
		return pmsPartnerAdmins;
	}

}
