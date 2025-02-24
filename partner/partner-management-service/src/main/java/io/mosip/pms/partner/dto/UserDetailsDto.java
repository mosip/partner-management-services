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

    private boolean consentGiven;

    private LocalDateTime consentGivenDtimes;

}