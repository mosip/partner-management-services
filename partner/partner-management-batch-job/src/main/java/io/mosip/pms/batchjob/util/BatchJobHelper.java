package io.mosip.pms.batchjob.util;

import java.util.Optional;

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

}