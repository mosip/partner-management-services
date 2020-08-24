package io.mosip.pmp.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pmp.registration.entity.DeviceMasterH;

@Repository
public interface DeviceMasterHRepository extends JpaRepository<DeviceMasterH, String> {

}
