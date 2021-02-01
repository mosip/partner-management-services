package io.mosip.pms.device.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.authdevice.entity.SecureBiometricInterfaceHistory;

@Repository
public interface SecureBiometricInterfaceHistoryRepository extends JpaRepository<SecureBiometricInterfaceHistory, String>{

}
