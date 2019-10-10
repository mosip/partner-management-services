package io.mosip.pmp.partnermanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partnermanagement.dto.Partner;


/**
 * Repository class for create partner id.
 * @author sanjeev.shrivastava
 *
 */

@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {

	/**
	 * Method to fetch last updated partner id.
	 * 
	 * @return the entity.
	 */

	public List<Partner> findByName(String name);
}
