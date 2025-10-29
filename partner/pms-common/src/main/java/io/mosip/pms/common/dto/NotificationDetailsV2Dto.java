package io.mosip.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class NotificationDetailsV2Dto extends NotificationDetailsDto {

    List<MISPLicenseKeyDetailsDto> mispLicenseKeyDetails;
}
