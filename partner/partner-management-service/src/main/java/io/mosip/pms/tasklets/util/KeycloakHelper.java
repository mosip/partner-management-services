package io.mosip.pms.tasklets.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.mosip.pms.partner.dto.AdminDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.logger.spi.Logger;
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
	private static final String EMAIL = "email";
	private static final String ENABLED = "enabled";
	private static final String ATTRIBUTES = "attributes";
	private static final String LANG_CODE = "LangCode";
	private static final String LOCALE = "locale";

	@Value("${mosip.iam.role-users-url}")
	private String roleUsersUrl;

	@Autowired
	RestUtil restUtil;

	@Autowired
	BatchJobHelper batchJobHelper;
	
	@Cacheable(value = "partnerAdminIdsCache", key = "'partnerAdminIds'", unless = "#result.isEmpty()")
	public List<AdminDetailsDto> getPartnerIdsWithPartnerAdminRole() {
		List<AdminDetailsDto> keycloakPartnerAdmins = new ArrayList<>();
		List<AdminDetailsDto> validPartnerAdmins = new ArrayList<AdminDetailsDto>();

		try {
			Map<String, String> pathSegments = Map.of(USER_ROLE, PARTNER_ADMIN);

			// Build the URL
			String urlWithPath = UriComponentsBuilder.fromUriString(roleUsersUrl).buildAndExpand(pathSegments)
					.toUriString();

			log.info("Fetching Partner Admin user IDs from URL: {}", urlWithPath);

			// Send API request
			Object response = restUtil.getApiWithContentType(roleUsersUrl, pathSegments, Object.class,
					MediaType.APPLICATION_JSON);

			// List to track all skipped users with their reason
			List<String> skippedUsers = new ArrayList<>();

			if (response instanceof List<?> usersList) {
				for (Object userObj : usersList) {
					if (userObj instanceof LinkedHashMap<?, ?> userMap) {
						// Username is mandatory in Keycloak, so it will always be present
						String username = userMap.get(USER_NAME).toString();
						
						// Check if user is enabled
						boolean isEnabled = Boolean.TRUE.equals(userMap.get(ENABLED));
						
						// If user is not enabled, log and continue to next iteration
						if (!isEnabled) {
							log.debug("Skipping disabled user: {}", username);
							skippedUsers.add(username + " (disabled)");
							continue;
						}
						
						// Check if email field exists
						if (!userMap.containsKey(EMAIL)) {
							log.debug("Skipping user with no email field: {}", username);
							skippedUsers.add(username + " (no email field)");
							continue;
						}
						
						Object emailObj = userMap.get(EMAIL);
						if (emailObj == null) {
							log.debug("Skipping user with null email: {}", username);
							skippedUsers.add(username + " (null email)");
							continue;
						}
						
						String email = emailObj.toString().trim();
						if (email.isEmpty()) {
							log.debug("Skipping user with missing or empty email: {}", username);
							skippedUsers.add(username + " (empty email)");
							continue;
						}
						
						AdminDetailsDto adminDetailsDto = new AdminDetailsDto();
						adminDetailsDto.setUserName(username);
						adminDetailsDto.setEmailId(email);
						String langCode = "eng";
						if (userMap.containsKey(ATTRIBUTES)) {
							Object attributesObj = userMap.get(ATTRIBUTES);
							if (attributesObj instanceof Map<?, ?> attributesMap) {

								Object langCodeObj = attributesMap.get(LANG_CODE);
								Object localeObj = attributesMap.get(LOCALE);

								if (langCodeObj instanceof List<?> langList && !langList.isEmpty()) {
									langCode = String.valueOf(langList.get(0));
								} else if (localeObj instanceof List<?> localeList && !localeList.isEmpty()) {
									langCode = String.valueOf(localeList.get(0));
								}
							}
						}
						adminDetailsDto.setLangCode(langCode);
						keycloakPartnerAdmins.add(adminDetailsDto);
					}
				}
			} else {
				log.error("Unexpected API response format while fetching Partner Admin user IDs.");
				throw new BatchJobServiceException(ErrorCode.FETCH_PARTNER_ADMIN_USER_IDS_ERROR.getErrorCode(),
						"Invalid response format received from API.");
			}
			
			// Log summary of skipped users
			log.info("Summary of skipped users - Total count: {}, Skipped users: {}", skippedUsers.size(), skippedUsers);
			
			log.info("KeyCloak returned {} Partner Admin users.", keycloakPartnerAdmins.size());
			validPartnerAdmins = batchJobHelper.getValidPartnerAdmins(keycloakPartnerAdmins);
			log.info("Keycloak has {} Partner Admin users.", validPartnerAdmins.size());
		} catch (HttpStatusCodeException e) {
			log.debug("API request failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
			throw new BatchJobServiceException(ErrorCode.API_NOT_ACCESSIBLE.getErrorCode(),
					"Failed to access the API: " + e.getMessage(), e);
		} catch (BatchJobServiceException e) {
			log.debug("Failed to fetch Partner Admin user IDs: {}", e.getMessage(), e);
		} catch (Exception e) {
			log.debug("Error occurred while fetching Partner Admin user IDs: {}", e.getMessage(), e);
		}
		return validPartnerAdmins;
	}

}
