package io.mosip.pmp.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.PartnerH;

@Repository
public interface PartnerHRepository extends JpaRepository<PartnerH, String>{

}
