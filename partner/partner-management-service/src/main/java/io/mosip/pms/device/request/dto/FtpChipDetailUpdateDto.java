package io.mosip.pms.device.request.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class FtpChipDetailUpdateDto {
	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "ftpChipDetailId", required = true, dataType = "java.lang.String")
	private String ftpChipDetailId;

	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "ftpProviderId", required = true, dataType = "java.lang.String")
	private String ftpProviderId;

	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;
	
	@NotNull
	private Boolean isItForRegistrationDevice;


	public Boolean getIsItForRegistrationDevice() {
		return isItForRegistrationDevice;
	}

	public void setIsItForRegistrationDevice(Boolean isItForRegistrationDevice) {
		this.isItForRegistrationDevice = isItForRegistrationDevice;
	}

	public String getFtpChipDetailId() {
		return ftpChipDetailId;
	}

	public void setFtpChipDetailId(String ftpChipDetailId) {
		this.ftpChipDetailId = ftpChipDetailId;
	}

	public String getFtpProviderId() {
		return ftpProviderId;
	}

	public void setFtpProviderId(String ftpProviderId) {
		this.ftpProviderId = ftpProviderId;
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

	public FtpChipDetailUpdateDto() {
		super();
	}
}
