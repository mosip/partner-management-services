package io.mosip.pms.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.pms.common.dto.AuthenticationFactor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class AuthenticationContextRefUtil {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationContextRefUtil.class);
	private static final String AMR_KEY = "amr";
	private static final String ACR_AMR = "acr_amr";
	
	@Value("${mosip.pms.idp.acr-amr-mappings}")
	String acr_amr_values;

	@Value("${mosip.pms.idp.supported-claims}")
	String supportedClaims;

	@Autowired
	ObjectMapper objectMapper;

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
		JSONParser parser = new JSONParser();
		JSONObject json = null;
		Set<String> filteredClaims = new HashSet<String>();
		try {
			json = (JSONObject) parser.parse(supportedClaims);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set entrySet = json.entrySet();
		for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			Map.Entry<String, Object> map = (Map.Entry<String, Object>) object;
			Object val = map.getValue();
			if (val instanceof String && !((String) val).isBlank()) {
				if (claimsFromPolicy.contains(val)) {
					filteredClaims.add(map.getKey());
				}
			}
			if (val instanceof JSONObject) {
				JSONObject cascadedObj = (JSONObject) val;
				Set cascadedObjEntrySet = cascadedObj.entrySet();
				for (Iterator cascadedObjIterator = cascadedObjEntrySet.iterator(); cascadedObjIterator.hasNext();) {
					Object object1 = (Object) cascadedObjIterator.next();
					Map.Entry<String, Object> map1 = (Map.Entry<String, Object>) object1;
					Object val1 = map1.getValue();
					if (val1 instanceof String && !((String) val1).isBlank()) {
						if (claimsFromPolicy.contains(val1)) {
							filteredClaims.add(map.getKey());
						}
					}
					if (val1 instanceof JSONArray) {
						JSONArray arrayOfValues = (JSONArray) val1;
						for (int i = 0; i < arrayOfValues.size(); i++) {
							if (claimsFromPolicy.contains(arrayOfValues.get(i))) {
								filteredClaims.add(map.getKey());
							}
						}
					}
				}

			}
		}
		return filteredClaims;
	}
}