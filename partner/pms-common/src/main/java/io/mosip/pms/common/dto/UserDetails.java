
package io.mosip.pms.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Component
@Entity
@Table(name = "user_details", schema = "pms")
@Data
@NoArgsConstructor
@ToString
public class UserDetails {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "consent_given")
    private String consentGiven;

    @Column(name = "consent_given_dtimes")
    private LocalDateTime consentGivenDtimes;

    @Column(name = "cr_by")
    private String crBy;

    @Column(name = "cr_dtimes")
    private LocalDateTime crDtimes;

    @Column(name = "upd_by")
    private String updBy;

    @Column(name = "upd_dtimes")
    private LocalDateTime updDtimes;
}