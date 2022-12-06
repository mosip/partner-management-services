package io.mosip.pms.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import io.mosip.pms.common.dto.AuthenticationFactor;
import io.mosip.pms.common.dto.Claims;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Component
public class AuthenticationContextRefUtil {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationContextRefUtil.class);
	private static final String AMR_KEY = "amr";
	private static final String ACR_AMR = "acr_amr";
	private static final String CLAIMSMAPPING = "idp-claims-mapping";
	
	@Value("${mosip.pms.idp.acr-amr-mappings}")
	String acr_amr_values;

	@Value("${mosip.pms.idp.supported-claims}")
	String supportedClaims;

	@Value("${mosip.pms.idp.claims-mappings}")
	String supportedClaimsMapping;
	
	@Autowired
	ObjectMapper objectMapper;
	 
	 @Value("${mosip.idp.claims-mapping-file-url:}")
	 private String claimsMappingFileUrl;
	 
	 @Autowired
	 RestTemplate restTemplate;
	 
	 private String claimsMappingJson;
	 
	private Map<String, List<AuthenticationFactor>> getAllAMRs() {
		try {
			ObjectNode objectNode = objectMapper.readValue(acr_amr_values, new TypeReference<ObjectNode>() {
			});
			return objectMapper.convertValue(objectNode.get(AMR_KEY),
					new TypeReference<Map<String, List<AuthenticationFactor>>>() {
					});
		} catch (IOException e) {
			logger.error("Failed to load / parse amr mappings", e);
			return null;
		}
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
			Map<String, Claims> map = objectMapper.convertValue(objectNode.get(CLAIMSMAPPING),
					new TypeReference<Map<String,Claims >>() {
					});
			Map<List<String>, String> claimsMap = new HashMap<>();
			for(Map.Entry<String,Claims> mapElement : map.entrySet()) {
				claimsMap.put(convertStringToList(mapElement.getValue().getAttributeName()),mapElement.getKey());
			}
			return claimsMap;
		} catch (IOException e) {
			logger.error("Failed to load / parse claims mappings", e);
			return null;
		}
	}
	private Map<String, List<String>> getAllACR_AMR_Mapping() {
		try {
			ObjectNode objectNode = objectMapper.readValue(acr_amr_values, new TypeReference<ObjectNode>() {
			});
			return objectMapper.convertValue(objectNode.get(ACR_AMR), new TypeReference<Map<String, List<String>>>() {
			});

		} catch (IOException e) {
			logger.error("Failed to load / parse acr_amr mappings", e);
			return null;
		}
	}

	public Set<String> getSupportedACRValues() {
		return getAllACR_AMR_Mapping().keySet();
	}
	

	public Set<String> getAuthFactors(List<String> policyACRs) {
		Map<String, List<AuthenticationFactor>> amr_mappings = getAllAMRs();
		Map<String, List<String>> acr_amr_mappings = getAllACR_AMR_Mapping();
		Set<String> result = new HashSet<>();
		for (String acrFromPolicy : policyACRs) {
			List<AuthenticationFactor> authFactors = amr_mappings.getOrDefault(acrFromPolicy, Collections.emptyList());
			for (String supportedACRValue : getSupportedACRValues()) {
				List<String> authFactorNames = acr_amr_mappings.getOrDefault(supportedACRValue,
						Collections.emptyList());
				for (AuthenticationFactor authFactor : authFactors) {
					if (authFactorNames.contains(authFactor.getType())) {
						result.add(supportedACRValue);
					}
				}
			}

		}
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Set<String> getPolicySupportedClaims(List<String> claimsFromPolicy) {		
		Map<List<String>, String> map = getAllClaims();
		Set<String> filteredClaims = new HashSet<String>();
		for(String claim:claimsFromPolicy) {
			for(Map.Entry<List<String>,String> mapElement : map.entrySet()) {
				if(mapElement.getKey().contains(claim)) {
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