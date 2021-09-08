package io.mosip.pms.device.request.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class FtpChipDetailUpdateDto {
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "ftpChipDetailId", required = true, dataType = "java.lang.String")
	private String ftpChipDetailId;

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "ftpProviderId", required = true, dataType = "java.lang.String")
	private String ftpProviderId;

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;
	
	@NotBlank(message="value is empty or null")
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
