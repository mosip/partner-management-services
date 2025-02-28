package io.mosip.pms.common.repository;

import java.util.List;

import io.mosip.pms.common.dto.PolicyCountDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import io.mosip.pms.common.entity.AuthPolicy;

@Repository
public interface AuthPolicyRepository extends JpaRepository<AuthPolicy, String>{

	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?1 and ap.name = ?2",nativeQuery = true)
	AuthPolicy findByPolicyGroupIdAndName(String policyGroupId, String name);
	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?1 and ap.id = ?2",nativeQuery = true)
	AuthPolicy findByPolicyGroupAndId(String policyGroupId, String policyId);

	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?1 and ap.id = ?2 and (ap.is_deleted is null or ap.is_deleted = false) and ap.is_active = true",nativeQuery = true)
	AuthPolicy findActivePoliciesByPolicyGroupId(String policyGroupId, String policyId);
	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=?", nativeQuery = true)
	List<AuthPolicy> findByPolicyGroupId(String policyId);	

	
	AuthPolicy findByName(String name);
	
	@Query(value = "select * from auth_policy ap where ap.id IN :policyIds and (ap.is_deleted is null or ap.is_deleted = false) and ap.is_active = true",nativeQuery = true)
	List<AuthPolicy> findByPolicyIds(@Param("policyIds") List<String> policyIds);
	
	@Query(value = "select * from auth_policy ap where ap.id IN :policyIds",nativeQuery = true)
	List<AuthPolicy> findAllByPolicyIds(@Param("policyIds") List<String> policyIds);

	
	@Query(value = "select * from auth_policy ap where ap.policy_group_id=? and (ap.is_deleted is null or ap.is_deleted = false) and ap.is_active = true",nativeQuery = true)
	List<AuthPolicy> findActivePoliciesByPolicyGroupId(String policyGroupId);

	@Query("SELECT new io.mosip.pms.common.dto.PolicyCountDto(" +
			"COUNT(CASE WHEN ap.isActive = false AND ap.schema IS NULL THEN 1 END), " +
			"COUNT(CASE WHEN ap.isActive = true AND ap.schema IS NOT NULL THEN 1 END)) " +
			"FROM AuthPolicy ap " +
			"WHERE ap.policyGroup.id = :policyGroupId AND (ap.isDeleted IS NULL OR ap.isDeleted = false)")
	PolicyCountDto findPolicyCountsByPolicyGroupId(@Param("policyGroupId") String policyGroupId);

	@Query(value = "select * from auth_policy ap where ap.name=? and (ap.is_deleted is null or ap.is_deleted = false) and ap.is_active = true",nativeQuery = true)
	AuthPolicy findByPolicyName(String policyName);

	@Query(value = "select * from auth_policy ap where ap.name=? and (ap.is_deleted is null or ap.is_deleted = false) and ap.is_active = true",nativeQuery = true)
	List <AuthPolicy> findByPolicyNameAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(String policyName);

	@Query(value="select * from auth_policy ap where lower(ap.name) like lower(concat('%', concat(?1, '%')))", nativeQuery = true)
	List<AuthPolicy> findByNameIgnoreCase(String name);
}
