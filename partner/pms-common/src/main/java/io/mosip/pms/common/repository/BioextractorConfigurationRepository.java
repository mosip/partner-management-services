package io.mosip.pms.common.repository;

import io.mosip.pms.common.entity.BioextractorConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BioextractorConfigurationRepository extends JpaRepository<BioextractorConfiguration, String> {
	boolean existsByConfigName(String configName);
}

