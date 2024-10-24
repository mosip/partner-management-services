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

    @Query("SELECT new PartnerSummaryEntity(" +
            "p.id, p.partnerTypeCode, p.name, p.policyGroup.id, pg.name, " +
            "p.emailId, CASE WHEN p.certificateAlias IS NULL THEN 'not_uploaded' ELSE 'uploaded' END, " +
            "p.approvalStatus, p.isActive, p.crDtimes) " +
            "FROM PartnerV3 p " +
            "LEFT JOIN p.policyGroup pg " +
            "WHERE (:partnerId = '' OR p.id LIKE CONCAT('%', :partnerId, '%')) " +
            "AND (:partnerTypeCode = '' OR p.partnerTypeCode LIKE CONCAT('%', :partnerTypeCode, '%')) " +
            "AND (:organizationName = '' OR p.name LIKE CONCAT('%', :organizationName, '%')) " +
            "AND (:policyGroupName = '' OR pg.name LIKE CONCAT('%', :policyGroupName, '%')) " +
            "AND (:certificateUploadStatus = '' OR " +
            "(:certificateUploadStatus = 'not_uploaded' AND p.certificateAlias IS NULL) " +
            "OR (:certificateUploadStatus = 'uploaded' AND p.certificateAlias IS NOT NULL)) " +
            "AND (:emailAddress = '' OR p.emailId LIKE CONCAT('%', :emailAddress, '%')) " +
            "AND (:isActive IS NULL OR p.isActive = :isActive)"
    )
    Page<PartnerSummaryEntity> getSummaryOfAllPartners(
            @Param("partnerId") String partnerId,
            @Param("partnerTypeCode") String partnerTypeCode,
            @Param("organizationName") String organizationName,
            @Param("policyGroupName") String policyGroupName,
            @Param("certificateUploadStatus") String certificateUploadStatus,
            @Param("emailAddress") String emailAddress,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

}