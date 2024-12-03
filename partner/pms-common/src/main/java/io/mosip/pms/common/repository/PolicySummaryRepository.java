package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.PolicySummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("PolicySummaryRepository")
public interface PolicySummaryRepository extends BaseRepository<PolicySummaryEntity, String> {

    String POLICY_SUMMARY_QUERY = "SELECT new PolicySummaryEntity(" +
            "p.id, p.name, p.descr, p.policyGroup.id, pg.name, " +
            "CASE " +
            "WHEN (p.schema IS NULL AND p.isActive = false) THEN 'draft' " +
            "WHEN (p.schema IS NOT NULL AND p.isActive = true) THEN 'activated' " +
            "WHEN (p.schema IS NOT NULL AND p.isActive = false) THEN 'deactivated' " +
            "END as status, " +
            "p.crDtimes) " +
            "FROM AuthPolicy p " +
            "LEFT JOIN p.policyGroup pg " +
            "WHERE (:policyId IS NULL OR lower(p.id) LIKE %:policyId%) " +
            "AND (:policyType IS NULL OR lower(p.policyType) LIKE %:policyType%) " +
            "AND (:policyName IS NULL OR lower(p.name) LIKE %:policyName%) " +
            "AND (:policyDescription IS NULL OR lower(p.descr) LIKE %:policyDescription%) " +
            "AND (:policyGroupName IS NULL OR lower(pg.name) LIKE %:policyGroupName%) " +
            "AND (:status IS NULL OR " +
            "(:status = 'draft' AND p.schema IS NULL AND p.isActive = false) " +
            "OR (:status = 'deactivated' AND p.schema IS NOT NULL AND p.isActive = false) " +
            "OR (:status = 'activated' AND p.schema IS NOT NULL AND p.isActive = true))";

    @Query(POLICY_SUMMARY_QUERY)
    Page<PolicySummaryEntity> getSummaryOfAllPolicies(
            @Param("policyId") String policyId,
            @Param("policyType") String policyType,
            @Param("policyName") String policyName,
            @Param("policyDescription") String policyDescription,
            @Param("policyGroupName") String policyGroupName,
            @Param("status") String status,
            Pageable pageable
    );

    @Query(POLICY_SUMMARY_QUERY + " ORDER BY status ASC")
    Page<PolicySummaryEntity> getSummaryOfAllPoliciesByStatusAsc(
            @Param("policyId") String policyId,
            @Param("policyType") String policyType,
            @Param("policyName") String policyName,
            @Param("policyDescription") String policyDescription,
            @Param("policyGroupName") String policyGroupName,
            @Param("status") String status,
            Pageable pageable
    );

    @Query(POLICY_SUMMARY_QUERY + " ORDER BY status DESC")
    Page<PolicySummaryEntity> getSummaryOfAllPoliciesByStatusDesc(
            @Param("policyId") String policyId,
            @Param("policyType") String policyType,
            @Param("policyName") String policyName,
            @Param("policyDescription") String policyDescription,
            @Param("policyGroupName") String policyGroupName,
            @Param("status") String status,
            Pageable pageable
    );

}
