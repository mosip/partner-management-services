package io.mosip.pms.device.authdevice.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.device.authdevice.entity.DeviceDetailEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceDetailSummaryRepository extends BaseRepository<DeviceDetailEntity, String> {

    String DEVICE_DETAILS_SUMMARY_QUERY = "SELECT new DeviceDetailEntity(" +
            "d.id, d.deviceProviderId, d.partnerOrganizationName, d.deviceTypeCode, d.deviceSubTypeCode, " +
            "CASE " +
            "WHEN (d.approvalStatus = 'approved' AND d.isActive = true) THEN 'approved' " +
            "WHEN (d.approvalStatus = 'approved' AND d.isActive = false) THEN 'deactivated' " +
            "WHEN (d.approvalStatus = 'pending_approval') THEN 'pending_approval' " +
            "WHEN (d.approvalStatus = 'rejected') THEN 'rejected' " +
            "END as status, " +
            "d.make, d.model, d.crDtimes, s.id, s.swVersion) " +
            "FROM DeviceDetail d " +
            "LEFT JOIN DeviceDetailSBI dds ON dds.id.deviceDetailId = d.id " +
            "LEFT JOIN SecureBiometricInterface s ON dds.id.sbiId = s.id " +
            "WHERE (:partnerId IS NULL OR lower(d.deviceProviderId) LIKE %:partnerId%) " +
            "AND (:orgName IS NULL OR lower(d.partnerOrganizationName) LIKE %:orgName%) " +
            "AND (:deviceType IS NULL OR lower(d.deviceTypeCode) LIKE %:deviceType%) " +
            "AND (:deviceSubType IS NULL OR lower(d.deviceSubTypeCode) LIKE %:deviceSubType%) " +
            "AND (:status IS NULL OR " +
            "(" +
            "  (:status = 'approved' AND d.approvalStatus = 'approved' AND d.isActive = true) " +
            "  OR (:status = 'deactivated' AND d.approvalStatus = 'approved' AND d.isActive = false) " +
            "  OR (:status = 'pending_approval' AND d.approvalStatus = 'pending_approval') " +
            "  OR (:status = 'rejected' AND d.approvalStatus = 'rejected')" +
            ")) " +
            "AND (:make IS NULL OR lower(d.make) LIKE %:make%) " +
            "AND (:model IS NULL OR lower(d.model) LIKE %:model%)"+
            "AND (:deviceId IS NULL OR lower(d.id) LIKE %:deviceId%)"+
            "AND (:sbiId IS NULL OR lower(s.id) LIKE %:sbiId%) " +
            "AND (:sbiVersion IS NULL OR lower(s.swVersion) LIKE %:sbiVersion%)";

    @Query(DEVICE_DETAILS_SUMMARY_QUERY)
    Page<DeviceDetailEntity> getSummaryOfAllDeviceDetails(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("deviceType") String deviceType,
            @Param("deviceSubType") String deviceSubType,
            @Param("status") String status,
            @Param("make") String make,
            @Param("model") String model,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("deviceId") String deviceId,
            Pageable pageable
    );

    @Query(DEVICE_DETAILS_SUMMARY_QUERY + " ORDER BY status ASC")
    Page<DeviceDetailEntity> getSummaryOfAllDeviceDetailsByStatusAsc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("deviceType") String deviceType,
            @Param("deviceSubType") String deviceSubType,
            @Param("status") String status,
            @Param("make") String make,
            @Param("model") String model,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("deviceId") String deviceId,
            Pageable pageable
    );

    @Query(DEVICE_DETAILS_SUMMARY_QUERY + " ORDER BY status DESC")
    Page<DeviceDetailEntity> getSummaryOfAllDeviceDetailsByStatusDesc(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("deviceType") String deviceType,
            @Param("deviceSubType") String deviceSubType,
            @Param("status") String status,
            @Param("make") String make,
            @Param("model") String model,
            @Param("sbiId") String sbiId,
            @Param("sbiVersion") String sbiVersion,
            @Param("deviceId") String deviceId,
            Pageable pageable
    );
}
