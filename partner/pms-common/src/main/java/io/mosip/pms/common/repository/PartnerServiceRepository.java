package io.mosip.pms.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.entity.Partner;

/**
 * Repository class for create partner id.
 *
 * @author sanjeev.shrivastava
 */

@Repository
public interface PartnerServiceRepository extends JpaRepository<Partner, String> {

    @Query(value = "select * from partner ppr where ppr.name=?", nativeQuery = true)
    public Partner findByName(String name);

    @Query(value = "select * from partner ppr where ppr.id=? and (ppr.is_deleted is null or ppr.is_deleted = false) and ppr.is_active = true", nativeQuery = true)
    public Partner findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(String deviceProviderId);

    @Query(value = "select * from partner ppr where ppr.email_id=?", nativeQuery = true)
    public Partner findByEmailId(String emailId);

    Partner findByIdAndIsActiveIsTrue(String id);

    @Query(value = "select * from partner ppr where ppr.id IN :partnerIds and (ppr.is_deleted is null or ppr.is_deleted = false) and ppr.is_active = true", nativeQuery = true)
    List<Partner> findByPartnerIds(@Param("partnerIds") List<String> partnerIds);

    @Query(value = "select p.id from partner p where lower(p.name) like lower(concat('%', concat(?1, '%')))", nativeQuery = true)
    List<String> findByNameIgnoreCase(String name);

    @Query(value = "select * from partner ppr where ppr.user_id=?", nativeQuery = true)
    public List<Partner> findByUserId(String userId);
}
