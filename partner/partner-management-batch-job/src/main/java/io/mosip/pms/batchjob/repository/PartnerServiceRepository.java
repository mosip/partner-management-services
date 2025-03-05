package io.mosip.pms.batchjob.repository;

import io.mosip.pms.batchjob.entity.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerServiceRepository extends JpaRepository<PartnerEntity, String> {

}
