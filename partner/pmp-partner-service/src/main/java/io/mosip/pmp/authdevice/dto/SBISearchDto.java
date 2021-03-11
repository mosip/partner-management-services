package io.mosip.pmp.authdevice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class SBISearchDto extends DeviceSearchDto {
	
	private String deviceDetailId;
}
