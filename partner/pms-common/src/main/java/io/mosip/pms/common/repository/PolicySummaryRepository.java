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

    @Query(value = "SELECT new PolicySummaryEntity(" +
            "p.id, p.name, p.descr, p.policyGroup.id, pg.name, " +
            "p.isActive, p.crDtimes) " +
            "FROM AuthPolicy p " +
            "LEFT JOIN p.policyGroup pg " +
            "WHERE (:policyId IS NULL OR lower(p.id) LIKE %:policyId%) " +
            "AND (:policyType IS NULL OR lower(p.policyType) LIKE %:policyType%) " +
            "AND (:policyName IS NULL OR lower(p.name) LIKE %:policyName%) " +
            "AND (:policyDescription IS NULL OR lower(p.descr) LIKE %:policyDescription%) " +
            "AND (:policyGroupName IS NULL OR lower(pg.name) LIKE %:policyGroupName%) " +
            "AND (:isActive IS NULL OR p.isActive = :isActive)"
    )
    Page<PolicySummaryEntity> getSummaryOfAllPolicies(
            @Param("policyId") String policyId,
            @Param("policyType") String policyType,
            @Param("policyName") String policyName,
            @Param("policyDescription") String policyDescription,
            @Param("policyGroupName") String policyGroupName,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

}
