package io.mosip.pms.batchjob.dto;

import lombok.Data;

@Data
public class CertificateDetailsDto {
    private String certificateId;
    private String issuedBy;
    private String issuedTo;
    private String partnerId;
    private String partnerDomain;
    private String expiryDateTime;
    private String expiryPeriod;
    private String certificateType;
}
