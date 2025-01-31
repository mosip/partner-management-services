package io.mosip.pms.partner.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserDetailsDto {

    @Schema(description = "Unique identifier for the user", example = "user123")
    private String userId;

    @Schema(description = "Indicates whether consent has been given (true if consent is given, false otherwise)", example = "true")
    private boolean consentGiven;

    @Schema(description = "Date and time when consent was given", example = "2024-08-08T10:00:00Z")
    private LocalDateTime consentGivenDateTime;

}