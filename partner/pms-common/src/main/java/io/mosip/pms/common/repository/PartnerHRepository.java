package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PartnerH;

import java.util.List;

@Repository
public interface PartnerHRepository extends JpaRepository<PartnerH, String> {
    @Query(value = "SELECT * FROM partner_h p WHERE p.email_id_hash IS NULL", nativeQuery = true)
    List<PartnerH> findPartnerHWithNullEmailHash();
}
