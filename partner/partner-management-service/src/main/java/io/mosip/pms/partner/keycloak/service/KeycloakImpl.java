package io.mosip.pms.partner.keycloak.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.KeycloakPasswordDTO;
import io.mosip.pms.partner.dto.KeycloakRequestDto;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.Role;
import io.mosip.pms.partner.dto.Roles;
import io.mosip.pms.partner.dto.RolesListDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.partner.exception.PartnerServiceException;

@Service
public class KeycloakImpl{
	
	@Value("${mosip.iam.realm.operations.base-url}")
	private String keycloakBaseUrl;

	@Value("${mosip.iam.admin-url}")
	private String keycloakAdminUrl;

	@Value("${mosip.iam.admin-realm-id}")
	private String adminRealmId;
	
	@Value("${mosip.iam.default.realm-id}")
	private String defaultRealmId;

	@Value("${mosip.iam.roles-extn-url}")
	private String roles;

	@Value("${mosip.iam.users-extn-url}")
	private String users;

	@Value("${mosip.iam.role-user-mapping-url}")
	private String roleUserMappingurl;

	@Qualifier(value = "keycloakRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	@Value("${mosip.keycloak.max-no-of-users:100}")
	private String maxUsers;	

	@Autowired
	private ObjectMapper objectMapper;

	private String individualRoleID;
	
	private static final Logger LOGGER= PMSLogger.getLogger(KeycloakImpl.class);
	
	public RolesListDto getAllRoles(String appId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("realmId", appId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakAdminUrl + roles);
		HttpHeaders httpHeaders = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		List<Role> rolesList = new ArrayList<>();
		try {
			JsonNode node = objectMapper.readTree(response);
			for (JsonNode jsonNode : node) {
				Role role = new Role();
				String name = jsonNode.get("name").textValue();
				role.setRoleId(name);
				role.setRoleName(name);
				rolesList.add(role);
			}
		} catch (IOException e) {
			throw new PartnerServiceException(ErrorCode.IO_EXCEPTION.getErrorCode(),
					ErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		RolesListDto rolesListDto = new RolesListDto();
		rolesListDto.setRoles(rolesList);
		return rolesListDto;
	}
	
	public MosipUserDto registerUser(UserRegistrationRequestDto userId) {
		Map<String, String> pathParams = new HashMap<>();
		KeycloakRequestDto keycloakRequestDto = mapUserRequestToKeycloakRequestDto(userId);
		String realm = getDefaultRealmId();		
		pathParams.put("realmId", realm);
		HttpEntity<KeycloakRequestDto> httpEntity = new HttpEntity<>(keycloakRequestDto);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl.concat("/users"));
		if (!isUserAlreadyPresent(userId.getUserName(), realm)) {
			callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(), HttpMethod.POST,
					httpEntity);
			if (keycloakRequestDto.getRealmRoles().contains(userId.getRole())) {
				String userID = getIDfromUserID(userId.getUserName(), realm);
				roleMapper(userID, realm,userId.getRole());
			}
		}

		MosipUserDto mosipUserDTO = new MosipUserDto();
		mosipUserDTO.setUserId(userId.getUserName());
		return mosipUserDTO;

	}

	private void roleMapper(String userID, String realmId, String roleId) {
		Map<String, String> pathParams = new HashMap<>();

		pathParams.put("realmId", realmId);
		pathParams.put("userID", userID);
		try {			
			individualRoleID = getRoleId(roleId,realmId);
		}
		catch(Exception ex){
			LOGGER.error("Role " + roleId + " not found in " + realmId + " for user " + userID);
		}
		
		Roles role = new Roles(individualRoleID, roleId);
		List<Roles> roles = new ArrayList<>();
		roles.add(role);
		pathParams.put("realmId", realmId);
		HttpEntity<List<Roles>> httpEntity = new HttpEntity<>(roles);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl.concat("/users/{userID}/role-mappings/realm"));
		callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(), HttpMethod.POST, httpEntity);
	}

	private String getIDfromUserID(String userName, String realmId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("realmId", realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl.concat("/users?username=").concat(userName));
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, null);
		JsonNode jsonNodes = null;
		try {
			if (response == null) {
				return null;
			}
			jsonNodes = objectMapper.readTree(response);
		} catch (IOException e) {
			throw new PartnerServiceException(ErrorCode.IO_EXCEPTION.getErrorCode(),
					ErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		if (jsonNodes.size() > 0) {
			for (JsonNode jsonNode : jsonNodes) {
				if (userName.equals(jsonNode.get("username").asText())) {
					return jsonNode.get("id").asText();
				}
			}

		}
		return null;
	}

	/**
	 * Checks if is user already present.
	 *
	 * @param userName
	 *            the user name
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean isUserAlreadyPresent(String userName, String realmId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("realmId", realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl.concat("/users?username=").concat(userName));
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, null);
		JsonNode jsonNodes = null;
		try {
			if (response == null) {
				return false;
			}
			jsonNodes = objectMapper.readTree(response);
		} catch (IOException e) {
			throw new PartnerServiceException(ErrorCode.IO_EXCEPTION.getErrorCode(),
					ErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		if (jsonNodes.size() > 0) {
			for (JsonNode jsonNode : jsonNodes) {
				if (userName.equals(jsonNode.get("username").asText())) {
					return true;
				}
			}

		}
		return false;
	}

	private KeycloakRequestDto mapUserRequestToKeycloakRequestDto(UserRegistrationRequestDto userRegDto) {
		KeycloakRequestDto keycloakRequestDto = new KeycloakRequestDto();
		List<String> roles = new ArrayList<>();
		List<KeycloakPasswordDTO> credentialObject = new ArrayList<>();
		
		roles.add(userRegDto.getRole());
		credentialObject = new ArrayList<>();
		KeycloakPasswordDTO dto = new KeycloakPasswordDTO();
		dto.setType("password");
		dto.setValue(userRegDto.getUserPassword());
		credentialObject.add(dto);
		
		List<Object> contactNoList = new ArrayList<>();
		List<Object> orgNameList = new ArrayList<>();
		List<Object> partnerIdList = new ArrayList<>();
		orgNameList.add(userRegDto.getOrganizationName());
		contactNoList.add(userRegDto.getContactNo());
		partnerIdList.add(userRegDto.getPartnerId());
		HashMap<String, List<Object>> attributes = new HashMap<>();
		attributes.put("mobile", contactNoList);

		attributes.put("organizationName", orgNameList);
		attributes.put("partnerId", partnerIdList);
		keycloakRequestDto.setUsername(userRegDto.getUserName());
		keycloakRequestDto.setFirstName(userRegDto.getFirstName());
		keycloakRequestDto.setEmail(userRegDto.getEmailID());
		keycloakRequestDto.setRealmRoles(roles);
		keycloakRequestDto.setAttributes(attributes);
		keycloakRequestDto.setEnabled(true);
		if (!credentialObject.isEmpty()) {
			keycloakRequestDto.setCredentials(credentialObject);
		}
		return keycloakRequestDto;
	}

	/**
	 * Call keycloak service.
	 *
	 * @param url
	 *            the url
	 * @param httpMethod
	 *            the http method
	 * @param requestEntity
	 *            the request entity
	 * @return the string
	 */
	private String callKeycloakService(String url, HttpMethod httpMethod, HttpEntity<?> requestEntity) {
		ResponseEntity<String> responseEntity = null;
		String response = null;
		try {
			responseEntity = restTemplate.exchange(url, httpMethod, requestEntity, String.class);
		} catch (HttpServerErrorException | HttpClientErrorException ex) {
			List<ServiceError> validationErrorsList = getServiceErrorList(ex.getResponseBodyAsString());            			
			if (ex.getRawStatusCode() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new BadCredentialsException("Authentication failed from AuthManager");
				}
			}
			if (ex.getRawStatusCode() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AccessDeniedException("Access denied from AuthManager");
				}
			}
			if(ex.getRawStatusCode() == 409) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);					
				}
			}

			throw new PartnerServiceException(ErrorCode.SERVER_ERROR.getErrorCode(),
					ErrorCode.SERVER_ERROR.getErrorMessage());

		}
		if (responseEntity != null && responseEntity.hasBody() && responseEntity.getStatusCode() == HttpStatus.OK) {
			response = responseEntity.getBody();
		}

		return response;
	}

	/**
	 * This method gives service error list for response receive from service.
	 * 
	 * @param responseBody the service response body.
	 * @return the list of {@link ServiceError}
	 */
	public static List<ServiceError> getServiceErrorList(String responseBody) {
		ObjectMapper mapper = new ObjectMapper();
		List<ServiceError> validationErrorsList = new ArrayList<>();
		try {
			JsonNode errorResponse = mapper.readTree(responseBody);
			if (errorResponse.has("errors")) {
				JsonNode errors = errorResponse.get("errors");
				Iterator<JsonNode> iter = errors.iterator();
				while (iter.hasNext()) {
					JsonNode parameterNode = iter.next();
					ServiceError serviceError = new ServiceError(getJsonValue(parameterNode, "errorCode"),
							getJsonValue(parameterNode, "message"));
					validationErrorsList.add(serviceError);
				}
			}
			if(errorResponse.has("errorMessage")) {
				ServiceError serviceError = new ServiceError(ErrorCode.SERVER_ERROR.getErrorCode()
						,ErrorCode.SERVER_ERROR.getErrorMessage());						
				validationErrorsList.add(serviceError);
			}
		} catch (Exception e) {
			// There is no Service error
		}
		return validationErrorsList;
	}
	
	/**
	 * This method provide jsonvalue based on propname mention.
	 * 
	 * @param node     the jsonnode.
	 * @param propName the property name.
	 * @return the property value.
	 */
	private static String getJsonValue(JsonNode node, String propName) {
		if (node.get(propName) != null) {
			return node.get(propName).asText();
		}
		return null;
	}
	
	/**
	 * Gets the role details given a role name.
	 *
	 * @param roleName
	 *            the id generated by keycloak for that user not username or userid
	 * @return roleid as string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String getRoleId(String roleName,String realmId) throws IOException {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("realmId", realmId);		
		pathParams.put("roleName", roleName);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl + "/roles/" + roleName);
		HttpHeaders httpHeaders = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		JsonNode jsonNode = objectMapper.readTree(response);
		String roleId = jsonNode.get("id").asText();
		return roleId;
		
	}	

	/**
	 * 
	 * @return
	 */
	public String getDefaultRealmId() {
		return defaultRealmId;
	}
	
}

