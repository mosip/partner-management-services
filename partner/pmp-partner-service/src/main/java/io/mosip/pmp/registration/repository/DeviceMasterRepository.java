package io.mosip.pmp.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.registration.entity.DeviceMaster;

@Repository
public interface DeviceMasterRepository extends JpaRepository<DeviceMaster, String>{

}
