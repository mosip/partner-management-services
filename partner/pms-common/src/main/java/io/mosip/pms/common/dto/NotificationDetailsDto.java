package io.mosip.pms.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationDetailsDto {
    private List<CertificateDetailsDto> certificateDetails;
    private List<FtmDetailsDto> ftmDetails;
    private List<SbiDetailsDto> sbiDetails;
    private List<ApiKeyDetailsDto> apiKeyDetails;
}
