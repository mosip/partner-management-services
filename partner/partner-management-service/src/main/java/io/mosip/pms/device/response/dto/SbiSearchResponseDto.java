package io.mosip.pms.device.response.dto;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SbiSearchResponseDto {	
	
	private String id;
	
	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "softBinaryHash", required = true, dataType = "java.lang.String")
	private byte[] swBinaryHash;
	
	@NotNull
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "softwareVersion", required = true, dataType = "java.lang.String")
	private String swVersion;
	
	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "deviceDetailId", required = true, dataType = "java.lang.String")
	private String deviceDetailId;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swCreateDateTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swExpiryDateTime;

	private Boolean isActive;	

	@Column(name="approval_status",length=36,nullable=false)
	private String approvalStatus;
	
	@Column(name="dprovider_id",length=36,nullable=false)
	private String providerId;
	
	@Column(name="partner_org_name",length=128)
	private String partnerOrganizationName;
	
	@Column(name="is_deleted")
	private boolean isDeleted;
	
	@Column(name="cr_by",length=256,nullable=false)
	private String crBy;

	@Column(name="cr_dtimes",nullable=false)
	private LocalDateTime crDtimes;

	@Column(name="del_dtimes")
	private LocalDateTime delDtimes;
	
	@Column(name="upd_by",length=256)
	private String updBy;

	@Column(name="upd_dtimes")
	private LocalDateTime updDtimes;	
}
