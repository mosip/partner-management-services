package io.mosip.pmp.authdevice.dto;

<<<<<<< HEAD:partner/pmp-partner-service/src/main/java/io/mosip/pmp/authdevice/dto/FilterResponseCodeDto.java
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
=======
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
>>>>>>> upstream/1.1.4:partner/pmp-partner-service/src/main/java/io/mosip/pmp/authdevice/dto/SBISearchDto.java
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
<<<<<<< HEAD:partner/pmp-partner-service/src/main/java/io/mosip/pmp/authdevice/dto/FilterResponseCodeDto.java
public class FilterResponseCodeDto {

	private List<ColumnCodeValue> filters;
=======
@EqualsAndHashCode(callSuper=true)
public class SBISearchDto extends DeviceSearchDto {
	
	private String deviceDetailId;
>>>>>>> upstream/1.1.4:partner/pmp-partner-service/src/main/java/io/mosip/pmp/authdevice/dto/SBISearchDto.java
}
