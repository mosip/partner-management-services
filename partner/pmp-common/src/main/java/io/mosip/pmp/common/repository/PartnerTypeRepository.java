package io.mosip.pmp.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.common.entity.PartnerType;

@Repository
public interface PartnerTypeRepository extends JpaRepository<PartnerType, String>{

}
