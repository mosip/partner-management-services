package io.mosip.pms.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class MISPLicenseEntityPK {

    @Column(name = "misp_id")
    private String mispId;

    @NotNull
    @Column(name = "license_key")
    private String licenseKey;
}
