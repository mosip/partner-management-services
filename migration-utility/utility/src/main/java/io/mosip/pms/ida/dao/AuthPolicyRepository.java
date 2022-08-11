package io.mosip.pms.ida.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface AuthPolicyRepository extends JpaRepository<AuthPolicy, String>{

	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?1 and ap.name = ?2",nativeQuery = true)
	AuthPolicy findByPolicyGroupAndName(String policyGroupId, String name);
	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?1 and ap.id = ?2",nativeQuery = true)
	AuthPolicy findByPolicyGroupAndId(String policyGroupId, String policyId);
	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?", nativeQuery = true)
	List<AuthPolicy> findByPolicyGroupId(String policyId);	

	
	AuthPolicy findByName(String name);
	
	@Query(value = "select * from auth_policy ap where ap.id IN :policyIds and (ap.is_deleted is null or ap.is_deleted = false) and ap.is_active = true",nativeQuery = true)
	List<AuthPolicy> findByPolicyIds(@Param("policyIds") List<String> policyIds);
	
	@Query(value = "select * from auth_policy ap where ap.id IN :policyIds",nativeQuery = true)
	List<AuthPolicy> findAllByPolicyIds(@Param("policyIds") List<String> policyIds);

	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=? and (ap.is_deleted is null or ap.is_deleted = false) and ap.is_active = true",nativeQuery = true)
	List<AuthPolicy> findActivePoliciesByPolicyGroupId(String policyGroupId);
	
	@Query(value = "select * from auth_policy ap where ap.name=? and (ap.is_deleted is null or ap.is_deleted = false) and ap.is_active = true",nativeQuery = true)
	AuthPolicy findByPolicyName(String policyName);
	
	@Query(value="select * from auth_policy ap where lower(ap.name) like lower(concat('%', concat(?1, '%')))", nativeQuery = true)
	List<AuthPolicy> findByNameIgnoreCase(String name);
}
