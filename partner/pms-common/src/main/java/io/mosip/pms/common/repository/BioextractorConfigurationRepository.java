package io.mosip.pms.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.BioextractorConfiguration;

@Repository
public interface BioextractorConfigurationRepository extends JpaRepository<BioextractorConfiguration, String> {
	
	boolean existsByConfigNameIgnoreCase(String configName);

	@Query("SELECT b FROM BioextractorConfiguration b " +
			"WHERE (:configName IS NULL OR lower(b.configName) LIKE %:configName%) " +
			"AND (:bioextractorProviderName IS NULL OR lower(b.bioextractorProviderName) LIKE %:bioextractorProviderName%) " +
			"AND (:bioextractorProviderVersion IS NULL OR lower(b.bioextractorProviderVersion) LIKE %:bioextractorProviderVersion%) " +
			"AND (:bioModality IS NULL OR lower(b.bioModality) LIKE %:bioModality%)")
	Page<BioextractorConfiguration> getAllBioextractorConfigurations(
			@Param("configName") String configName,
			@Param("bioextractorProviderName") String bioextractorProviderName,
			@Param("bioextractorProviderVersion") String bioextractorProviderVersion,
			@Param("bioModality") String bioModality,
			Pageable pageable
	);
}
