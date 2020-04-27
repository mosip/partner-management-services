package io.mosip.pmp.misp.dto;

import java.util.List;

import io.mosip.pmp.misp.entity.MISPEntity;
import io.mosip.pmp.misp.entity.MISPLicenseEntity;
import lombok.Data;

@Data
public class MISPDetailsDto {
	
	private MISPEntity misp;	
	
	private List<MISPLicenseEntity> misp_licenses;

}
