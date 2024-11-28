package io.mosip.pms.device.authdevice.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@SqlResultSetMapping(
        name = "Mapping.FtmDetailSummaryEntity",
        classes = { @ConstructorResult(
                targetClass = FtmDetailSummaryEntity.class,
                columns = {
                        @ColumnResult(name = "ftmId", type = String.class),
                        @ColumnResult(name = "partnerId", type = String.class),
                        @ColumnResult(name = "orgName", type = String.class),
                        @ColumnResult(name = "make", type = String.class),
                        @ColumnResult(name = "model", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "isActive", type = Boolean.class),
                        @ColumnResult(name = "isCertificateAvailable", type = Boolean.class),
                        @ColumnResult(name = "createdDateTime", type = LocalDateTime.class)
                })
        }
)
public class FtmDetailSummaryEntity {

    public FtmDetailSummaryEntity(String ftmId, String partnerId, String orgName, String make, String model,
                                  String status, Boolean isActive, Boolean isCertificateAvailable,
                                  LocalDateTime createdDateTime) {
        this.ftmId = ftmId;
        this.partnerId = partnerId;
        this.orgName = orgName;
        this.make = make;
        this.model = model;
        this.status = status;
        this.isActive = isActive;
        this.isCertificateAvailable = isCertificateAvailable;
        this.createdDateTime = createdDateTime;
    }

    // No-argument constructor
    public FtmDetailSummaryEntity() {
        super();
    }

    @Id
    private String ftmId;

    private String partnerId;

    private String orgName;

    private String make;

    private String model;

    private String status;

    private Boolean isActive;

    private Boolean isCertificateAvailable;

    private LocalDateTime createdDateTime;
}
