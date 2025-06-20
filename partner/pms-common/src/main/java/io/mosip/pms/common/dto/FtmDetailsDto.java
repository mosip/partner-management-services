package io.mosip.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class FtmDetailsDto extends CertificateDetailsDto {
    private String ftmId;
    private String make;
    private String model;
}
