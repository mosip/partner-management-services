package io.mosip.pmp.authdevice.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class DeviceDetailDto {
	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
	private String id;


	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "deviceProviderId", required = true, dataType = "java.lang.String")
	private String deviceProviderId;

	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "deviceTypeCode", required = true, dataType = "java.lang.String")
	private String deviceTypeCode;

	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "deviceSubTypeCode", required = true, dataType = "java.lang.String")
	private String deviceSubTypeCode;


	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;


	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;

	@Size(min = 0, max = 128)
	@ApiModelProperty(value = "partnerOrganizationName", required = true, dataType = "java.lang.String")
	private String partnerOrganizationName;

	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "approvalStatus", required = true, dataType = "java.lang.String")
	private String approvalStatus;

	@NotNull
	private Boolean isActive;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDeviceProviderId() {
		return deviceProviderId;
	}

	public void setDeviceProviderId(String deviceProviderId) {
		this.deviceProviderId = deviceProviderId;
	}

	public String getDeviceTypeCode() {
		return deviceTypeCode;
	}

	public void setDeviceTypeCode(String deviceTypeCode) {
		this.deviceTypeCode = deviceTypeCode;
	}

	public String getDeviceSubTypeCode() {
		return deviceSubTypeCode;
	}

	public void setDeviceSubTypeCode(String deviceSubTypeCode) {
		this.deviceSubTypeCode = deviceSubTypeCode;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getPartnerOrganizationName() {
		return partnerOrganizationName;
	}

	public void setPartnerOrganizationName(String partnerOrganizationName) {
		this.partnerOrganizationName = partnerOrganizationName;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public DeviceDetailDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
