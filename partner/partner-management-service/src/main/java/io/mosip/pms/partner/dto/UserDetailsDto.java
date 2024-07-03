package io.mosip.pms.partner.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDetailsDto {

    private String userId;

    private String consentGiven;

    private LocalDateTime consentGivenDtimes;

    private String crBy;

    private LocalDateTime crDtimes;

    private String updBy;

    private LocalDateTime updDtimes;

}