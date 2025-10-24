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

    String PARTNER_DETAILS_SUMMARY_QUERY = "SELECT new PartnerSummaryEntity(" +
            "p.id, p.partnerTypeCode, p.name, p.policyGroup.id, pg.name, " +
            "p.emailId, CASE WHEN p.certificateAlias IS NULL THEN 'not_uploaded' ELSE 'uploaded' END, " +
            "CASE " +
            "WHEN p.approvalStatus = 'approved' AND p.isActive = true THEN 'active' " +
            "WHEN p.approvalStatus = 'approved' AND p.isActive = false THEN 'deactivated' " +
            "WHEN p.approvalStatus = 'InProgress' AND p.isActive = false THEN 'inactive' " +
            "END AS status, " +
            "p.isActive, p.crDtimes) " +
            "FROM PartnerV3 p " +
            "LEFT JOIN p.policyGroup pg " +
            "WHERE (:partnerId IS NULL OR lower(p.id) LIKE %:partnerId%) " +
            "AND (:partnerTypeCode IS NULL OR lower(p.partnerTypeCode) LIKE %:partnerTypeCode%) " +
            "AND (:organizationName IS NULL OR lower(p.name) LIKE %:organizationName%) " +
            "AND (:policyGroupName IS NULL OR lower(pg.name) LIKE %:policyGroupName%) " +
            "AND (:status IS NULL OR " +
            "(:status = 'deactivated' AND p.approvalStatus = 'approved' AND p.isActive = false) " +
            "OR (:status = 'active' AND p.approvalStatus = 'approved' AND p.isActive = true) " +
            "OR (:status = 'inactive' AND p.approvalStatus = 'InProgress')) " +
            "AND (:certificateUploadStatus IS NULL OR " +
            "(:certificateUploadStatus = 'not_uploaded' AND p.certificateAlias IS NULL) " +
            "OR (:certificateUploadStatus = 'uploaded' AND p.certificateAlias IS NOT NULL)) " +
            "AND (:emailAddress IS NULL OR (lower(p.emailId) = :emailAddress OR p.emailIdHash = :emailAddressHash)) " +
            "AND (:isActive IS NULL OR p.isActive = :isActive)";

    String PARTNER_DETAILS_SUMMARY_COUNT_QUERY =
            "SELECT COUNT(p.id) " +
                    "FROM PartnerV3 p " +
                    "LEFT JOIN p.policyGroup pg " +
                    "WHERE (:partnerId IS NULL OR lower(p.id) LIKE %:partnerId%) " +
                    "AND (:partnerTypeCode IS NULL OR lower(p.partnerTypeCode) LIKE %:partnerTypeCode%) " +
                    "AND (:organizationName IS NULL OR lower(p.name) LIKE %:organizationName%) " +
                    "AND (:policyGroupName IS NULL OR lower(pg.name) LIKE %:policyGroupName%) " +
                    "AND (:status IS NULL OR " +
                    "(:status = 'deactivated' AND p.approvalStatus = 'approved' AND p.isActive = false) " +
                    "OR (:status = 'active' AND p.approvalStatus = 'approved' AND p.isActive = true) " +
                    "OR (:status = 'inactive' AND p.approvalStatus = 'InProgress')) " +
                    "AND (:certificateUploadStatus IS NULL OR " +
                    "(:certificateUploadStatus = 'not_uploaded' AND p.certificateAlias IS NULL) " +
                    "OR (:certificateUploadStatus = 'uploaded' AND p.certificateAlias IS NOT NULL)) " +
                    "AND (:emailAddress IS NULL OR (lower(p.emailId) = :emailAddress OR p.emailIdHash = :emailAddressHash))";

    @Query(
            value = PARTNER_DETAILS_SUMMARY_QUERY,
            countQuery = PARTNER_DETAILS_SUMMARY_COUNT_QUERY
    )
    Page<PartnerSummaryEntity> getSummaryOfAllPartners(
            @Param("partnerId") String partnerId,
            @Param("partnerTypeCode") String partnerTypeCode,
            @Param("organizationName") String organizationName,
            @Param("policyGroupName") String policyGroupName,
            @Param("certificateUploadStatus") String certificateUploadStatus,
            @Param("emailAddress") String emailAddress,
            @Param("emailAddressHash") String emailAddressHash,
            @Param("isActive") Boolean isActive,
            @Param("status") String status,
            Pageable pageable
    );

    @Query(
            value = PARTNER_DETAILS_SUMMARY_QUERY + " ORDER BY status ASC",
            countQuery = PARTNER_DETAILS_SUMMARY_COUNT_QUERY
    )
    Page<PartnerSummaryEntity> getSummaryOfAllPartnersByStatusAsc(
            @Param("partnerId") String partnerId,
            @Param("partnerTypeCode") String partnerTypeCode,
            @Param("organizationName") String organizationName,
            @Param("policyGroupName") String policyGroupName,
            @Param("certificateUploadStatus") String certificateUploadStatus,
            @Param("emailAddress") String emailAddress,
            @Param("emailAddressHash") String emailAddressHash,
            @Param("isActive") Boolean isActive,
            @Param("status") String status,
            Pageable pageable
    );

    @Query(
            value = PARTNER_DETAILS_SUMMARY_QUERY + " ORDER BY status DESC",
            countQuery = PARTNER_DETAILS_SUMMARY_COUNT_QUERY
    )
    Page<PartnerSummaryEntity> getSummaryOfAllPartnersByStatusDesc(
            @Param("partnerId") String partnerId,
            @Param("partnerTypeCode") String partnerTypeCode,
            @Param("organizationName") String organizationName,
            @Param("policyGroupName") String policyGroupName,
            @Param("certificateUploadStatus") String certificateUploadStatus,
            @Param("emailAddress") String emailAddress,
            @Param("emailAddressHash") String emailAddressHash,
            @Param("isActive") Boolean isActive,
            @Param("status") String status,
            Pageable pageable
    );

}