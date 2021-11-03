package io.mosip.pms.device.regdevice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pms.device.regdevice.entity.RegRegistrationDeviceType;

@Repository
public interface RegRegistrationDeviceTypeRepository extends JpaRepository<RegRegistrationDeviceType, String>{

}
