package io.mosip.pmp.misp.repository;

import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pmp.misp.entity.MISPLicenseReadEntity;
import io.mosip.pmp.misp.entity.MISPlKeyUniqueKeyEntity;

@Repository
public interface MispLicenseKeyReadRepository extends BaseRepository<MISPLicenseReadEntity, MISPlKeyUniqueKeyEntity> {

}
