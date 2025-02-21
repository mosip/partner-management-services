package io.mosip.pms.partner.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class CertificateDto {
    private String certificateName;
    private Date certificateUploadDate;
    private Date certificateExpiryDate;
    private String partnerType;
    private String partnerId;
    private Boolean isCertificateAvailable;
}
