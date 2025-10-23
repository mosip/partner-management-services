package io.mosip.pms.common.repository;

import io.mosip.pms.common.entity.MISPLicenseEntityV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MispLicenseV2Repository extends JpaRepository<MISPLicenseEntityV2, String> {

    @Query(value = "select * from misp_license ml where ml.misp_id=?1 and ((?2 IS NULL AND ml.policy_id IS NULL) OR ml.policy_id = ?2) and ((?3 IS NULL AND ml.license_key_name IS NULL) OR ml.license_key_name = ?3)", nativeQuery = true)
    List<MISPLicenseEntityV2> findByPartnerIdAndPolicyIdAndLicenseKeyName(String partnerId, String policyId, String licenseKeyName);

    @Query(value = "select * from misp_license ml where (ml.is_deleted is null or ml.is_deleted = false) and ml.is_active = true and ml.valid_to_date < ?1", nativeQuery = true)
    List<MISPLicenseEntityV2> findAllExpiredActiveMISPLicenses(LocalDateTime currentDateTime);

    @Query(value = "select * from misp_license ml where ml.misp_id=?1 and ((?2 IS NULL AND ml.policy_id IS NULL) OR ml.policy_id = ?2) and ml.is_active = true", nativeQuery = true)
    List<MISPLicenseEntityV2> findActiveLicenseKeyByPartnerIdAndPolicyId(String partnerId, String policyId);

    @Query(value = "select * from misp_license ml where ml.misp_id=?1 and ml.is_active = true", nativeQuery = true)
    List<MISPLicenseEntityV2> findActiveLicenseKeyByPartnerId(String partnerId);

    @Query(value = "select * from misp_license ml where ml.misp_id=?1 and ml.license_key = ?2", nativeQuery = true)
    MISPLicenseEntityV2 findByPartnerIdAndLicenseKey(String partnerId, String licenseKey);
}
