package io.mosip.pmp.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.regdevice.entity.RegRegisteredDeviceHistory;

@Repository
public interface RegRegisteredDeviceHistoryRepository extends JpaRepository<RegRegisteredDeviceHistory, String> {

}
