package io.mosip.pms.device.authdevice.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.device.authdevice.entity.SbiSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("SbiSummaryRepository")
public interface SbiSummaryRepository extends BaseRepository<SbiSummaryEntity, String> {

    String SBI_DETAILS_SUMMARY_QUERY = "SELECT new SbiSummaryEntity(" +
            "s.providerId, s.partnerOrgName, s.id, s.swVersion, " +
            "CASE " +
            "WHEN s.approvalStatus = 'approved' AND s.isActive = true THEN 'approved' " +
            "WHEN s.approvalStatus = 'approved' AND s.isActive = false THEN 'deactivated' " +
            "WHEN s.approvalStatus = 'rejected' THEN 'rejected' " +
            "WHEN s.approvalStatus = 'pending_approval' THEN 'pending_approval' " +
            "END AS status, " +
            "s.isActive, s.swCreateDateTime, s.swExpiryDateTime, s.crDtimes, COUNT(dd.id.deviceDetailId) AS countOfAssociatedDevices, " +
            "SUM(CASE WHEN d.approvalStatus = 'approved' AND d.isActive = true THEN 1 ELSE 0 END) AS countOfApprovedDevices, " +
            "SUM(CASE WHEN d.approvalStatus = 'pending_approval' THEN 1 ELSE 0 END) AS countOfPendingDevices, " +
            "CASE " +
            "WHEN s.swExpiryDateTime < CURRENT_DATE THEN 'expired' " +
            "ELSE 'valid' " +
            "END AS sbiExpiryStatus ) " +
            "FROM SecureBiometricInterface s " +
            "LEFT JOIN DeviceDetailSBI dd ON dd.id.sbiId = s.id " +
            "LEFT JOIN DeviceDetail d ON d.id = dd.id.deviceDetailId " +
            "WHERE (:partnerId IS NULL OR lower(s.providerId) LIKE %:partnerId%) " +
            "AND (:orgName IS NULL OR lower(s.partnerOrgName) LIKE %:orgName%) " +
            "AND (:sbiId IS NULL OR lower(s.id) LIKE %:sbiId%) " +
            "AND (:sbiVersion IS NULL OR lower(s.swVersion) LIKE %:sbiVersion%) " +
            "AND (:status IS NULL OR " +
            "(:status = 'deactivated' AND s.approvalStatus = 'approved' AND s.isActive = false) " +
            "OR (:status = 'approved' AND s.approvalStatus = 'approved' AND s.isActive = true) " +
            "OR (:status = 'rejected' AND s.approvalStatus = 'rejected') " +
            "OR (:status = 'pending_approval' AND s.approvalStatus = 'pending_approval')) " +
            "AND (:sbiExpiryStatus IS NULL OR " +
            "(:sbiExpiryStatus = 'expired' AND s.swExpiryDateTime < CURRENT_DATE) " +
            "OR (:sbiExpiryStatus = 'valid' AND s.swExpiryDateTime >= CURRENT_DATE)) " +
            "AND (:isPartnerAdmin = true OR (s.providerId IN :partnerIdList)) " +
            "GROUP BY s.providerId, s.partnerOrgName, s.id, s.swVersion, s.approvalStatus, " +
            "s.isActive, s.swCreateDateTime, s.swExpiryDateTime, s.crDtimes";

    @Query(SBI_DETAILS_SUMMARY_QUERY)
    Page<SbiSummaryEntity> getSummaryOfSbiDetails(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("status") String status,
            @Param("sbiExpiryStatus") String sbiExpiryStatus,
            @Param("partnerIdList") List<String> partnerIdList,
            @Param("isPartnerAdmin") boolean isPartnerAdmin,
            Pageable pageable
    );

    @Query(SBI_DETAILS_SUMMARY_QUERY + " ORDER BY status ASC")
    Page<SbiSummaryEntity> getSummaryOfSbiDetailsByStatusAsc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("status") String status,
            @Param("sbiExpiryStatus") String sbiExpiryStatus,
            @Param("partnerIdList") List<String> partnerIdList,
            @Param("isPartnerAdmin") boolean isPartnerAdmin,
            Pageable pageable
    );

    @Query(SBI_DETAILS_SUMMARY_QUERY + " ORDER BY status DESC")
    Page<SbiSummaryEntity> getSummaryOfSbiDetailsByStatusDesc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("status") String status,
            @Param("sbiExpiryStatus") String sbiExpiryStatus,
            @Param("partnerIdList") List<String> partnerIdList,
            @Param("isPartnerAdmin") boolean isPartnerAdmin,
            Pageable pageable
    );

    @Query(SBI_DETAILS_SUMMARY_QUERY + " ORDER BY sbiExpiryStatus ASC")
    Page<SbiSummaryEntity> getSummaryOfSbiDetailsByExpiryStatusAsc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("status") String status,
            @Param("sbiExpiryStatus") String sbiExpiryStatus,
            @Param("partnerIdList") List<String> partnerIdList,
            @Param("isPartnerAdmin") boolean isPartnerAdmin,
            Pageable pageable
    );

    @Query(SBI_DETAILS_SUMMARY_QUERY + " ORDER BY sbiExpiryStatus DESC")
    Page<SbiSummaryEntity> getSummaryOfSbiDetailsByExpiryStatusDesc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("status") String status,
            @Param("sbiExpiryStatus") String sbiExpiryStatus,
            @Param("partnerIdList") List<String> partnerIdList,
            @Param("isPartnerAdmin") boolean isPartnerAdmin,
            Pageable pageable
    );

    @Query(SBI_DETAILS_SUMMARY_QUERY + " ORDER BY countOfAssociatedDevices ASC")
    Page<SbiSummaryEntity> getSummaryOfSbiDetailsByDevicesCountAsc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("status") String status,
            @Param("sbiExpiryStatus") String sbiExpiryStatus,
            @Param("partnerIdList") List<String> partnerIdList,
            @Param("isPartnerAdmin") boolean isPartnerAdmin,
            Pageable pageable
    );

    @Query(SBI_DETAILS_SUMMARY_QUERY + " ORDER BY countOfAssociatedDevices DESC")
    Page<SbiSummaryEntity> getSummaryOfSbiDetailsByDevicesCountDesc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("status") String status,
            @Param("sbiExpiryStatus") String sbiExpiryStatus,
            @Param("partnerIdList") List<String> partnerIdList,
            @Param("isPartnerAdmin") boolean isPartnerAdmin,
            Pageable pageable
    );
}
