package io.mosip.pmp.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.regdevice.entity.RegSecureBiometricInterfaceHistory;

@Repository
public interface RegSecureBiometricInterfaceHistoryRepository extends JpaRepository<RegSecureBiometricInterfaceHistory, String>{

}
