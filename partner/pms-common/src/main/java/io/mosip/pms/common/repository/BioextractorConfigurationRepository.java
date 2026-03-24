package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.BioextractorConfiguration;

@Repository
public interface BioextractorConfigurationRepository extends JpaRepository<BioextractorConfiguration, String> {

	boolean existsByConfigName(String configName);
}
