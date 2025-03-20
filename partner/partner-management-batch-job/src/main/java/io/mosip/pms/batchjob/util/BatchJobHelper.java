package io.mosip.pms.batchjob.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.pms.batchjob.config.LoggerConfiguration;

@Component
public class BatchJobHelper {

	private static final Logger LOGGER = LoggerConfiguration.logConfig(BatchJobHelper.class);

	@Autowired
	PartnerServiceRepository partnerRepository;

	public boolean validateActivePartnerId(Optional<Partner> partnerById) {
		if (partnerById.isEmpty()) {
			return false;
		} else {
			if (!partnerById.get().getIsActive()) {
				return false;
			} else {
				return true;
			}
		}
	}

	public Optional<Partner> getPartnerById(String partnerId) {
		Optional<Partner> partnerById = partnerRepository.findById(partnerId);
		return partnerById;
	}

	public void validateApiResponse(Map<String, Object> response, String apiUrl) {
		if (response == null) {
			LOGGER.error("Received null response from API: {}", apiUrl);
			throw new BatchJobServiceException(
					ErrorCodes.API_NULL_RESPONSE.getCode(),
					ErrorCodes.API_NULL_RESPONSE.getMessage()
			);
		}

		if (response.containsKey(PartnerConstants.ERRORS)) {
			List<Map<String, Object>> errorList = (List<Map<String, Object>>) response.get(PartnerConstants.ERRORS);
			if (errorList != null && !errorList.isEmpty()) {
				LOGGER.error("Error occurred while fetching data: {}", errorList);
				throw new BatchJobServiceException(
						String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORCODE)),
						String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORMESSAGE))
				);
			}
		}

		if (!response.containsKey(PartnerConstants.RESPONSE) || response.get(PartnerConstants.RESPONSE) == null) {
			LOGGER.error("Missing response data in API call: {}", apiUrl);
			throw new BatchJobServiceException(
					ErrorCodes.API_NULL_RESPONSE.getCode(),
					ErrorCodes.API_NULL_RESPONSE.getMessage()
			);
		}
	}

}