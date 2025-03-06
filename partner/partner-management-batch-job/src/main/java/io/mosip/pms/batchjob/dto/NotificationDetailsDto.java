package io.mosip.pms.batchjob.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationDetailsDto {
    private List<CertificateDetailsDto> certificateDetails;
}
