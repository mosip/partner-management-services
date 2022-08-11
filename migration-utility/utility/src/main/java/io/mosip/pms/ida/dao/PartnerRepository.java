package io.mosip.pms.ida.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


/**
 * Repository class for create partner id.
 * @author sanjeev.shrivastava
 *
 */

@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {

	/**
	 * Method to fetch last updated partner id
	 * @param name partner name
	 * @return list of partner
	 */
	
	public List<Partner> findByName(String name);
	
	/**
	 * 
	 * @param partnerType
	 * @return
	 */
	@Query(value = "select * from partner ppr where ppr.partner_type_code=?", nativeQuery = true)
	public List<Partner> findByPartnerType(String partnerType);
}
