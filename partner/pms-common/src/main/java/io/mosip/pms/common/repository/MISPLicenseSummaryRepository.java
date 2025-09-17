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
            "m.id.mispId, p.name, pg.id, pg.name, pg.desc, ap.id, ap.name, " +
            "ap.descr, m.licenseKeyName, " +
            "CONCAT(FUNCTION('repeat', '*', LENGTH(m.id.licenseKey) - 4), SUBSTRING(m.id.licenseKey, LENGTH(m.id.licenseKey) - 3, 4)) as mispLicenseKey, " +
            "CASE " +
            "WHEN (m.isActive = true) THEN 'activated' " +
            "WHEN (m.isActive = false) THEN 'deactivated' " +
            "END as status, " +
            "m.createdDateTime, m.validToDate) " +
            "FROM MISPLicenseEntityV2 m " +
            "LEFT JOIN m.policy ap " +
            "LEFT JOIN m.partner p " +
            "LEFT JOIN p.policyGroup pg " +
            "WHERE (:partnerId IS NULL OR lower(m.id.mispId) LIKE %:partnerId%) " +
            "AND (:orgName IS NULL OR lower(p.name) LIKE %:orgName%) " +
            "AND (:policyGroupName IS NULL OR lower(pg.name) LIKE %:policyGroupName%) " +
            "AND (:policyName IS NULL OR lower(ap.name) LIKE %:policyName%) " +
            "AND (:mispLicenseKeyName IS NULL OR lower(m.licenseKeyName) LIKE %:mispLicenseKeyName%) " +
            "AND (:status IS NULL OR " +
            "(" +
            "  (:status = 'activated' AND m.isActive = true) " +
            "  OR (:status = 'deactivated' AND m.isActive = false) " +
            ")) "
    )
    Page<MISPLicenseSummaryEntity> getSummaryOfAllMispLicenseDetails(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("policyGroupName") String policyGroupName,
            @Param("policyName") String policyName,
            @Param("mispLicenseKeyName") String mispLicenseKeyName,
            @Param("status") String status,
            Pageable pageable
    );
}
