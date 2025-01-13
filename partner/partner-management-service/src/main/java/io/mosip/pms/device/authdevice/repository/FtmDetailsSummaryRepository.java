package io.mosip.pms.device.authdevice.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.device.authdevice.entity.FtmDetailSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("FtmDetailsSummaryRepository")
public interface FtmDetailsSummaryRepository extends BaseRepository<FtmDetailSummaryEntity, String> {

    String FTM_DETAILS_SUMMARY_QUERY = "SELECT new FtmDetailSummaryEntity(" +
            "f.ftpChipDetailId, f.ftpProviderId, f.partnerOrganizationName, f.make, f.model, " +
            "CASE " +
            "WHEN f.approvalStatus = 'approved' AND f.isActive = true THEN 'approved' " +
            "WHEN f.approvalStatus = 'approved' AND f.isActive = false THEN 'deactivated' " +
            "WHEN f.approvalStatus = 'rejected' THEN 'rejected' " +
            "WHEN f.approvalStatus = 'pending_approval' THEN 'pending_approval' " +
            "WHEN f.approvalStatus = 'pending_cert_upload' THEN 'pending_cert_upload' " +
            "END AS status, " +
            "f.isActive, CASE WHEN f.certificateAlias IS NULL THEN false ELSE true END, f.crDtimes) " +
            "FROM FTPChipDetail f " +
            "WHERE (:partnerId IS NULL OR lower(f.ftpProviderId) LIKE %:partnerId%) " +
            "AND (:orgName IS NULL OR lower(f.partnerOrganizationName) LIKE %:orgName%) " +
            "AND (:ftmId IS NULL OR lower(f.ftpChipDetailId) LIKE %:ftmId%) " +
            "AND (:make IS NULL OR lower(f.make) LIKE %:make%) " +
            "AND (:model IS NULL OR lower(f.model) LIKE %:model%) " +
            "AND (:status IS NULL OR " +
            "(:status = 'deactivated' AND f.approvalStatus = 'approved' AND f.isActive = false) " +
            "OR (:status = 'approved' AND f.approvalStatus = 'approved' AND f.isActive = true) " +
            "OR (:status = 'rejected' AND f.approvalStatus = 'rejected') " +
            "OR (:status = 'pending_approval' AND f.approvalStatus = 'pending_approval') " +
            "OR (:status = 'pending_cert_upload' AND f.approvalStatus = 'pending_cert_upload'))";

    @Query(FTM_DETAILS_SUMMARY_QUERY)
    Page<FtmDetailSummaryEntity> getSummaryOfPartnersFtmDetails(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("ftmId") String ftmId,
            @Param("make") String make,
            @Param("model") String model,
            @Param("status") String status,
            Pageable pageable
    );

    @Query(FTM_DETAILS_SUMMARY_QUERY + " ORDER BY status ASC")
    Page<FtmDetailSummaryEntity> getSummaryOfPartnersFtmDetailsByStatusAsc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("ftmId") String ftmId,
            @Param("make") String make,
            @Param("model") String model,
            @Param("status") String status,
            Pageable pageable
    );

    @Query(FTM_DETAILS_SUMMARY_QUERY + " ORDER BY status DESC")
    Page<FtmDetailSummaryEntity> getSummaryOfPartnersFtmDetailsByStatusDesc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("ftmId") String ftmId,
            @Param("make") String make,
            @Param("model") String model,
            @Param("status") String status,
            Pageable pageable
    );
}
