package io.mosip.pms.common.dto;

import io.mosip.pms.common.constant.Purpose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class DeviceFilterValueDto extends FilterValueDto{	
	
	private Purpose purpose;
}
