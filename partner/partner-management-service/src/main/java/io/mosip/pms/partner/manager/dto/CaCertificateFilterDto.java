package io.mosip.pms.partner.manager.dto;

import lombok.Data;

@Data
public class CaCertificateFilterDto {

    private String caCertificateType;
    private String certificateId;
    private String partnerDomain;
    private String issuedTo;
    private String issuedBy;
}
