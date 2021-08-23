package io.mosip.pms.device.request.dto;

import io.mosip.pms.common.dto.SearchDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class DeviceSearchDto extends SearchDto {

	@ApiModelProperty(notes = "purpose is used to differentiate the device details.", required = true, example = "AUTH/REGISTRATION")
	private String purpose;
}
