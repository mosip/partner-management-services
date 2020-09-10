package io.mosip.pmp.authdevice.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FTPChipDetailStatusDto {
	
	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "ftpChipDetailId", required = true, dataType = "java.lang.String")
	private String ftpChipDetailId;
	
	@NotNull		
	private Boolean approvalStatus;
		
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

	public Boolean getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(Boolean approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public FTPChipDetailStatusDto() {
		super();
	}
}
