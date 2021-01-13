package io.mosip.pmp.common.dto;

import io.mosip.pmp.common.constant.Purpose;
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
