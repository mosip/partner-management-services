package io.mosip.pmp.partner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.Partner;

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
	/*@Query(value = "select t.curr_seq_no,t.cr_by,t.cr_dtimes,t.upd_by,t.upd_dtimes FROM master.tspid_seq t where t.curr_seq_no=(select max(t.curr_seq_no) FROM master.tspid_seq t)", nativeQuery = true)
	Partner findLastTspId();*/
 
	public List<Partner> findByName(String name);
}
