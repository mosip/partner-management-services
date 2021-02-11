package io.mosip.pmp.authdevice.dto;

import io.mosip.pmp.authdevice.constants.Purpose;
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
