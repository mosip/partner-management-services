package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.MISPLicenseReadEntity;
import io.mosip.pms.common.entity.MISPlKeyUniqueKeyEntity;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 *
 * Defines an object to provide misp license data base related operations. 
 */
@Repository
public interface MispLicenseKeyReadRepository extends JpaRepository<MISPLicenseReadEntity, MISPlKeyUniqueKeyEntity> {

}
