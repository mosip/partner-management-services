package io.mosip.pms.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.PartnerContact;

import java.util.List;

@Repository
public interface PartnerContactRepository extends JpaRepository<PartnerContact, String> {

    @Query(value = "select * from partner_contact pc where pc.partner_id=?1 and pc.email_id_hash = ?2", nativeQuery = true)
    public PartnerContact findByPartnerAndEmailIdHash(String partnerId, String emailIdHash);

    @Query(value = "select * from partner_contact pc where pc.partner_id=?1 and pc.email_id = ?2", nativeQuery = true)
    public PartnerContact findByPartnerAndEmailId(String partnerId, String emailId);

    @Query(value = "SELECT * FROM partner_contact pc WHERE pc.email_id_hash IS NULL", nativeQuery = true)
    List<PartnerContact> findPartnerContactWithNullEmailHash();
} 
