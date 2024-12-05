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
        name = "Mapping.DeviceDetailEntity",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceDetailEntity.class,
                        columns = {
                                @ColumnResult(name = "deviceId", type = String.class),
                                @ColumnResult(name = "partnerId", type = String.class),
                                @ColumnResult(name = "orgName", type = String.class),
                                @ColumnResult(name = "deviceType", type = String.class),
                                @ColumnResult(name = "deviceSubType", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "make", type = String.class),
                                @ColumnResult(name = "model", type = String.class),
                                @ColumnResult(name = "createdDateTime", type = LocalDateTime.class)
                        }
                )
        }
)
public class DeviceDetailEntity {

    public DeviceDetailEntity(
            String deviceId, String partnerId, String orgName, String deviceType,
            String deviceSubType, String status, String make, String model, LocalDateTime createdDateTime) {
        this.deviceId = deviceId;
        this.partnerId = partnerId;
        this.orgName = orgName;
        this.deviceType = deviceType;
        this.deviceSubType = deviceSubType;
        this.status = status;
        this.make = make;
        this.model = model;
        this.createdDateTime = createdDateTime;
    }

    // No-argument constructor
    public DeviceDetailEntity() {
        super();
    }

    @Id
    private String deviceId;

    private String partnerId;

    private String orgName;

    private String deviceType;

    private String deviceSubType;

    private String status;

    private String make;

    private String model;

    private LocalDateTime createdDateTime;
}
