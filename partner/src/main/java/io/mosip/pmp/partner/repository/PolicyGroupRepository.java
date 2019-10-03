package io.mosip.pmp.partner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.partner.entity.PolicyGroup;

/**
 * Repository class for create partner id.
 * 
 * @author Sanjeev
 *
 */
// TODO: once the db is finalize changes required according to schema given
@Repository
public interface PolicyGroupRepository extends JpaRepository<PolicyGroup, String> {

	/**
	 * Method to fetch last updated partner id.
	 * 
	 * @return the entity.
	 */
	
	
	public PolicyGroup findByName(String str);
	
	/*@Query(value = "select t.curr_seq_no,t.cr_by,t.cr_dtimes,t.upd_by,t.upd_dtimes FROM master.tspid_seq t where t.curr_seq_no=(select max(t.curr_seq_no) FROM master.tspid_seq t)", nativeQuery = true)
	Partner findLastTspId();*/
 
}
