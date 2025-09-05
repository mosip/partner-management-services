package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.MISPLicenseSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("MISPLicenseSummaryRepository")
public interface MISPLicenseSummaryRepository extends BaseRepository<MISPLicenseSummaryEntity, String> {

    @Query(value = "SELECT new MISPLicenseSummaryEntity(" +
            "m.mispId, pg.id, pg.name, pg.desc, ap.id, ap.name, " +
            "ap.descr, m.licenseKey, " +
            "CASE " +
            "WHEN (m.isActive = true) THEN 'activated' " +
            "WHEN (m.isActive = false) THEN 'deactivated' " +
            "END as status, " +
            "m.createdDateTime, m.validToDate) " +
            "FROM MISPLicenseEntityV2 m " +
            "LEFT JOIN m.policy ap " +
            "LEFT JOIN m.partner p " +
            "LEFT JOIN p.policyGroup pg " +
            "WHERE (:partnerId IS NULL OR lower(m.mispId) LIKE %:partnerId%) " +
            "AND (:policyGroupName IS NULL OR lower(pg.name) LIKE %:policyGroupName%) " +
            "AND (:policyName IS NULL OR lower(ap.name) LIKE %:policyName%) " +
            "AND (:status IS NULL OR " +
            "(" +
            "  (:status = 'activated' AND m.isActive = true) " +
            "  OR (:status = 'deactivated' AND m.isActive = false) " +
            ")) "
    )
    Page<MISPLicenseSummaryEntity> getSummaryOfAllMispLicenseDetails(
            @Param("partnerId") String partnerId,
            @Param("policyGroupName") String policyGroupName,
            @Param("policyName") String policyName,
            @Param("status") String status,
            Pageable pageable
    );
}
