package io.mosip.pms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CryptoRequestDto {
    private String data;
    private String applicationId;
    private String referenceId;
    private LocalDateTime timeStamp;

}