package io.mosip.pms.device.regdevice.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.regdevice.entity.RegSecureBiometricInterfaceHistory;

@Repository
public interface RegSecureBiometricInterfaceHistoryRepository extends JpaRepository<RegSecureBiometricInterfaceHistory, String>{

	/**
	 * Find by id and is active is true.
	 *
	 * @param swVersion   the sw version
	 * @param effiveTimes the effive times
	 * @return {@link MOSIPDeviceServiceHistory}
	 */
	@Query(value = "(select * from secure_biometric_interface_h dsh where sw_version = ?1 and eff_dtimes<= ?2 and (is_deleted is null or is_deleted =false) and is_active=true ORDER BY eff_dtimes DESC) LIMIT 1", nativeQuery = true)
	List<RegSecureBiometricInterfaceHistory> findByIdAndIsActiveIsTrueAndByEffectiveTimes(String swVersion,
			LocalDateTime effiveTimes);

	/**
	 * Find by id and D provider id.
	 *
	 * @param id               the id
	 * @param deviceProviderId the device provider id
	 * @param effTimes         the eff times
	 * @return {@link MOSIPDeviceServiceHistory}
	 */
	@Query(value = "(select * from secure_biometric_interface_h dsh where id = ?1 and dprovider_id=?2 and eff_dtimes<= ?3 and (is_deleted is null or is_deleted =false) ORDER BY eff_dtimes DESC) LIMIT 1", nativeQuery = true)
	RegSecureBiometricInterfaceHistory findByIdAndDProviderId(String id, String deviceProviderId, LocalDateTime effTimes);

}
