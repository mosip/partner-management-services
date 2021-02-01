package io.mosip.pms.device.authdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import io.mosip.pms.device.authdevice.entity.RegisteredDeviceHistory;

@Component
public interface RegisteredDeviceHistoryRepository extends JpaRepository<RegisteredDeviceHistory, String> {

}
