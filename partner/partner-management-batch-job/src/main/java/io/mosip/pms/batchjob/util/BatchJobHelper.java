package io.mosip.pms.batchjob.util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.dto.OriginalCertDownloadResponseDto;
import io.mosip.pms.batchjob.dto.TrustCertTypeListRequestDto;
import io.mosip.pms.batchjob.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.batchjob.entity.Partner;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.batchjob.repository.PartnerServiceRepository;
import io.mosip.pms.common.request.dto.RequestWrapper;

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

}