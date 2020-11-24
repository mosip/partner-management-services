package io.mosip.pmp.common.repository;

import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pmp.common.entity.MISPLicenseReadEntity;
import io.mosip.pmp.common.entity.MISPlKeyUniqueKeyEntity;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 *
 * Defines an object to provide misp license data base related operations. 
 */
@Repository
public interface MispLicenseKeyReadRepository extends BaseRepository<MISPLicenseReadEntity, MISPlKeyUniqueKeyEntity> {

}
