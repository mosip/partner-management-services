package io.mosip.pms.device.util;

import java.util.ArrayList;
import java.util.List;

import io.mosip.pms.device.regdevice.entity.RegSecureBiometricInterface;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;

public class MapperUtil {

	public static List<SbiSearchResponseDto> mapSbiResponse(List<RegSecureBiometricInterface> sbiDetails){
		List<SbiSearchResponseDto> response = new ArrayList<>();
		sbiDetails.forEach(sbi->{
			SbiSearchResponseDto dto = new SbiSearchResponseDto();
			dto.setCrBy(sbi.getCrBy());
			dto.setCrDtimes(sbi.getCrDtimes());
			dto.setDelDtimes(sbi.getDelDtimes());
			dto.setUpdBy(sbi.getUpdBy());
			dto.setUpdDtimes(sbi.getUpdDtimes());
			dto.setIsActive(sbi.isActive());
			dto.setDeleted(sbi.isDeleted());
			dto.setApprovalStatus(sbi.getApprovalStatus());
			dto.setDeviceDetailId(sbi.getDeviceDetailId());	
			dto.setDeviceProviderId(sbi.getDeviceDetail().getDeviceProviderId());
			dto.setPartnerOrganizationName(sbi.getDeviceDetail().getPartnerOrganizationName());
			dto.setId(sbi.getId());
			dto.setSwBinaryHash(sbi.getSwBinaryHash());
			dto.setSwCreateDateTime(sbi.getSwCreateDateTime());
			dto.setSwExpiryDateTime(sbi.getSwExpiryDateTime());
			dto.setSwVersion(sbi.getSwVersion());
			response.add(dto);
		});
		return response;
	}
}
