package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.PartnerSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository("PartnerSummaryRepository")
public interface PartnerSummaryRepository extends BaseRepository<PartnerSummaryEntity, String> {

    @Query(value = "SELECT p.id AS partnerId, p.partner_type_code AS partnerType, p.name AS orgName, p.policy_group_id AS policyGroupId, "
            + "pg.name AS policyGroupName, p.email_id AS emailAddress, "
            + "CASE WHEN p.certificate_alias IS NULL THEN 'not_uploaded' ELSE 'uploaded' END AS certificateUploadStatus, "
            + "p.approval_status AS status, p.is_active AS isActive, p.cr_dtimes AS createdDateTime "
            + "FROM pms.partner p LEFT JOIN pms.policy_group pg ON p.policy_group_id = pg.id",
            nativeQuery = true)
    Page<PartnerSummaryEntity> getSummaryOfAllPartners(Pageable pageable);
}
