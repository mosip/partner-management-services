package io.mosip.pmp.misp.repository;

import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pmp.misp.entity.MISPLicenseReadEntity;
import io.mosip.pmp.misp.entity.MISPlKeyUniqueKeyEntity;

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
