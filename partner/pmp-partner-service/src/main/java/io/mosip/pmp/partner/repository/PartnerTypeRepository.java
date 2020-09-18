package io.mosip.pmp.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.PartnerType;

@Repository
public interface PartnerTypeRepository extends JpaRepository<PartnerType, String>{

}
