package io.mosip.pmp.misp.repository;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pmp.misp.entity.MISPEntity;

 /**
  * 
  * @author Nagarjuna Kuchi
  * @version 1.0
  * 
  * Defines an object to provide misp data base related operations.
  *
  */
@Repository
public interface MispServiceRepository extends BaseRepository<MISPEntity, String> {

	
	@Query(value = "select * from pmp.misp m where m.name=?",nativeQuery = true)
	MISPEntity findByName(String name);
	
	@Query(value = "select * from pmp.misp m where m.name like ?%",nativeQuery = true)
	List<MISPEntity> findByStartsWithName(String name);
}
