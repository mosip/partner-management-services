package io.mosip.pms.tasklets.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;

@Component
@EnableCaching
public class KeycloakHelper {
	private Logger log = PMSLogger.getLogger(KeycloakHelper.class);

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

			log.info("Fetching Partner Admin user IDs from URL: {}", urlWithPath);

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
				log.error("Unexpected API response format while fetching Partner Admin user IDs. {}");
				throw new BatchJobServiceException(ErrorCode.FETCH_PARTNER_ADMIN_USER_IDS_ERROR.getErrorCode(),
						"Invalid response format received from API.");
			}
			log.info("KeyCloak returned {} Partner Admin users.", keycloakPartnerAdmins.size());
			pmsPartnerAdmins = batchJobHelper.getValidPartnerAdminsInPms(keycloakPartnerAdmins);
			log.info("PMS has {} Partner Admin users.", pmsPartnerAdmins.size());
		} catch (HttpStatusCodeException e) {
			log.debug("API request failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
			throw new BatchJobServiceException(ErrorCode.API_NOT_ACCESSIBLE.getErrorCode(),
					"Failed to access the API: " + e.getMessage(), e);
		} catch (BatchJobServiceException e) {
			log.debug("Failed to fetch Partner Admin user IDs: {}", e.getMessage(), e);
		} catch (Exception e) {
			log.debug("Error occurred while fetching Partner Admin user IDs: {}", e.getMessage(), e);
		}
		return pmsPartnerAdmins;
	}

}
