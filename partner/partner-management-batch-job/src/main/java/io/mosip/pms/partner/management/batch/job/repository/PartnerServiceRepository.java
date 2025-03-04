package io.mosip.pms.partner.management.batch.job.repository;

import io.mosip.pms.partner.management.batch.job.entity.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerServiceRepository extends JpaRepository<PartnerEntity, String> {

}
