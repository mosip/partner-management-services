package io.mosip.pms.common.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import io.mosip.pms.common.entity.MISPEntity;

 /**
  * 
  * @author Nagarjuna Kuchi
  * @version 1.0
  * 
  * Defines an object to provide misp data base related operations.
  *
  */
@Repository
public interface MispServiceRepository extends JpaRepository<MISPEntity, String> {

	
	@Query(value = "select * from misp m where m.name=?",nativeQuery = true)
	MISPEntity findByName(String name);
	
	@Query(value = "select * from misp m where m.name like ?%",nativeQuery = true)
	List<MISPEntity> findByStartsWithName(String name);
}
