package io.mosip.pmp.authdevice.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class FTPChipDetailDto {
//	@NotNull
//	@Size(min = 1, max = 36)
//	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
//	private String id;


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

//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}

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
	
	public FTPChipDetailDto() {
		super();
		// TODO Auto-generated constructor stub
	}
}
