package io.mosip.pms.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.pms.common.constant.SearchErrorCode;
import io.mosip.pms.common.dto.AuthenticationFactor;
import io.mosip.pms.common.dto.Claims;
import io.mosip.pms.common.exception.RequestException;

@Component
public class AuthenticationContextRefUtil {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationContextRefUtil.class);
	private static final String AMR_KEY = "amr";
	private static final String ACR_AMR = "acr_amr";
	private static final String IDENTITY = "identity";
	private static final String CLAIMS_SUPPORTED = "claims_supported";
	@Autowired
	RestUtil restUtil;
	
	@Autowired
	ObjectMapper objectMapper;
	 
	 @Value("${mosip.pms.esignet.claims-mapping-file-url}")
	 private String claimsMappingFileUrl;
	 
	 @Value("${mosip.pms.esignet.amr-acr-mapping-file-url}")
	 private String acrMappingFileUrl;
	 
	 @Autowired
	 RestTemplate restTemplate;
	 
	 private String claimsMappingJson;
	 
	 private String acrMappingJson;
	 
	private Map<String, List<AuthenticationFactor>> getAllAMRs() {
		try {
			ObjectNode objectNode = objectMapper.readValue(getAcrMappingJson(), new TypeReference<ObjectNode>() {
			});
			return objectMapper.convertValue(objectNode.get(AMR_KEY),
					new TypeReference<Map<String, List<AuthenticationFactor>>>() {
					});
		} catch (IOException e) {
			logger.error("Failed to load / parse amr mappings", e);
			return null;
		}
	}

	private String getAcrMappingJson() {
        if(StringUtils.isEmpty(acrMappingJson)) {
            logger.info("Fetching Claims mapping json from : {}", claimsMappingFileUrl);
            acrMappingJson = restTemplate.getForObject(acrMappingFileUrl, String.class);
        }
        return acrMappingJson;
    }
	
	@SuppressWarnings("unchecked")
	@Cacheable
	private List<String> getSupportedClaims(){
		Map<String, Object> idpClientResponse = restUtil.getApi("mosip.pms.esignet.config-url", null, "", "", Map.class);
		return (List<String>) idpClientResponse.get(CLAIMS_SUPPORTED);
	}
	
	private String getClaimsMappingJson() {
        if(StringUtils.isEmpty(claimsMappingJson)) {
            logger.info("Fetching Claims mapping json from : {}", claimsMappingFileUrl);
            claimsMappingJson = restTemplate.getForObject(claimsMappingFileUrl, String.class);
        }
        return claimsMappingJson;
    }
	
	private Map<List<String>, String> getAllClaims() {
		try {
			ObjectNode objectNode = objectMapper.readValue(getClaimsMappingJson(), new TypeReference<ObjectNode>() {
			});
			Map<String, Claims> map = objectMapper.convertValue(objectNode.get(IDENTITY),
					new TypeReference<Map<String,Claims >>() {
					});
			Map<List<String>, String> claimsMap = new HashMap<>();
			List<String> allowedClaims = getSupportedClaims();
			for(String claim:allowedClaims) {
				if(map.get(claim).getValue()!=null) {
					claimsMap.put(convertStringToList(map.get(claim).getValue()),claim);
				}
			}
			return claimsMap;
		} catch (IOException e) {
			logger.error("Failed to load / parse claims mappings", e);
			throw new RequestException(SearchErrorCode.FAILED_TO_FETCH_CLAIMS.getErrorCode(),
					SearchErrorCode.FAILED_TO_FETCH_CLAIMS.getErrorMessage());
		}
	}
	private Map<String, List<String>> getAllACR_AMR_Mapping() {
		try {
			ObjectNode objectNode = objectMapper.readValue(getAcrMappingJson(), new TypeReference<ObjectNode>() {
			});
			return objectMapper.convertValue(objectNode.get(ACR_AMR), new TypeReference<Map<String, List<String>>>() {
			});

		} catch (IOException e) {
			logger.error("Failed to load / parse acr_amr mappings", e);
			throw new RequestException(SearchErrorCode.FAILED_TO_FETCH_ACRVALUES.getErrorCode(),
					SearchErrorCode.FAILED_TO_FETCH_ACRVALUES.getErrorMessage());

		}
	}

	public Set<String> getSupportedACRValues() {
		return getAllACR_AMR_Mapping().keySet();
	}
	

	public Set<String> getAuthFactors(Set<String> policyACRs) {
		Set<String> matchedAMRs = getAllAMRs().entrySet().stream()
		        .filter( entry -> entry.getValue().stream().allMatch( factor -> policyACRs.contains(factor.getType().toLowerCase())))
		        .map(Map.Entry::getKey)
		        .collect(Collectors.toSet());
		Set<String> matchedACRs = getAllACR_AMR_Mapping().entrySet().stream()
		        .filter( entry -> entry.getValue().stream().allMatch(amr -> matchedAMRs.contains(amr)))
		        .map(Map.Entry::getKey)
		        .collect(Collectors.toSet());
		return matchedACRs;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set<String> getPolicySupportedClaims(Set<String> claimsFromPolicy) {		
		Map<List<String>, String> map = getAllClaims();
		Set<String> filteredClaims = new HashSet<String>();
		for(String claim:claimsFromPolicy) {
			for(Map.Entry<List<String>,String> mapElement : map.entrySet()) {
				if(mapElement.getKey().stream().anyMatch(claim::equalsIgnoreCase)) {
					filteredClaims.add(mapElement.getValue());
				}
			}
		}
		return filteredClaims;
	}
	
	/**
	 * 
	 * @param commaSeparatedString
	 * @return
	 */
	private List<String> convertStringToList(String commaSeparatedString){
		return Arrays.asList(commaSeparatedString.split(","));
	}
}