package io.mosip.pms.common.dto;

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
    private String ftmId;
    private String make;
    private String model;
}
