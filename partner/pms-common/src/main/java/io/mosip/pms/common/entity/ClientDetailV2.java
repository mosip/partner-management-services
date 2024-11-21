package io.mosip.pms.common.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name="oidc_client")
@Data
public class ClientDetailV2 {

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "rp_id")
    private String rpId;

    @Column(name = "policy_id")
    private String policyId;

    @Column(name = "logo_uri")
    private String logoUri;

    @Column(name = "redirect_uris")
    private String redirectUris;

    @Column(name = "public_key", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "claims")
    private String claims;

    @Column(name = "acr_values")
    private String acrValues;

    @Column(name = "status")
    private String status;

    @Column(name = "grant_types")
    private String grantTypes;

    @Column(name = "auth_methods")
    private String clientAuthMethods;

    @Column(name = "cr_by", nullable = false, length = 256)
    public String createdBy;

    @Column(name = "cr_dtimes", nullable = false)
    public Date createdDateTime;

    @Column(name = "upd_by", length = 256)
    public String updatedBy;

    @Column(name = "upd_dtimes")
    public Date updatedDateTime;

    @Column(name = "is_deleted")
    public Boolean isDeleted;

    @ManyToOne
    @JoinColumn(name="rp_id", insertable = false, updatable = false)
    private PartnerV3 partner;

    @ManyToOne
    @JoinColumn(name = "policy_id", insertable = false, updatable = false)
    private AuthPolicy policy;
}
