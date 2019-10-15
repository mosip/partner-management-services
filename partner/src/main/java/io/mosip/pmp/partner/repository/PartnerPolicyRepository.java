package io.mosip.pmp.partner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.PartnerPolicy;

/**
 * @author sanjeev.shrivastava
 *
 */
@Repository
public interface PartnerPolicyRepository extends JpaRepository<PartnerPolicy, String> {
	
	//public List<PartnerPolicy> findBypart_id(String partnerId);//
	
	//public PartnerPolicy findByPartId(String part_id);
 
}
