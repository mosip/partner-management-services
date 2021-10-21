package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PartnerH;

@Repository
public interface PartnerHRepository extends JpaRepository<PartnerH, String>{

}
