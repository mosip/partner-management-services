package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.PartnerSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("PartnerSummaryRepository")
public interface PartnerSummaryRepository extends BaseRepository<PartnerSummaryEntity, String> {

    @Query("SELECT new PartnerSummaryEntity("
            + "p.id, p.partnerTypeCode, p.name, p.policyGroup.id, pg.name, "
            + "p.emailId, CASE WHEN p.certificateAlias IS NULL THEN 'not_uploaded' ELSE 'uploaded' END, "
            + "p.approvalStatus, p.isActive, p.crDtimes) "
            + "FROM PartnerV3 p "
            + "LEFT JOIN p.policyGroup pg "
            + "WHERE (:partnerId is null or p.id = :partnerId)"
    )
    Page<PartnerSummaryEntity> getSummaryOfAllPartners(@Param("partnerId") String partnerId, Pageable pageable);
}