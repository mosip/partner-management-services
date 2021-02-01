package io.mosip.pms.device.request.dto;

import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.SearchDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class DeviceSearchDto extends SearchDto {

	private Purpose purpose;
}
