package io.mosip.pms.common.repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.pms.common.entity.ClientSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("ClientSummaryRepository")
public interface ClientSummaryRepository extends BaseRepository<ClientSummaryEntity, String> {

    @Query(value = "SELECT new ClientSummaryEntity(" +
            "c.rpId, p.name, pg.name, pg.desc, ap.name, " +
            "ap.descr, c.id, c.name, c.status, c.createdDateTime) " +
            "FROM ClientDetailV2 c " +
            "LEFT JOIN c.policy ap " +
            "LEFT JOIN c.partner p " +
            "LEFT JOIN p.policyGroup pg " +
            "WHERE (:partnerId IS NULL OR lower(c.rpId) LIKE %:partnerId%) " +
            "AND (:orgName IS NULL OR lower(p.name) LIKE %:orgName%) " +
            "AND (:policyGroupName IS NULL OR lower(pg.name) LIKE %:policyGroupName%) " +
            "AND (:policyName IS NULL OR lower(ap.name) LIKE %:policyName%) " +
            "AND (:oidcClientName IS NULL OR lower(c.name) LIKE %:oidcClientName%) " +
            "AND (:status IS NULL OR c.status = :status) "
    )
    Page<ClientSummaryEntity> getSummaryOfAllPartnerClients(
            @Param("partnerId") String partnerId,
            @Param("orgName") String orgName,
            @Param("policyGroupName") String policyGroupName,
            @Param("policyName") String policyName,
            @Param("oidcClientName") String oidcClientName,
            @Param("status") String status,
            Pageable pageable
    );
}
