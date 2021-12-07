package io.mosip.pms.partner.misp.service;

import java.util.List;

import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;

public interface InfraServiceProviderService {

	/**
	 * 
	 * @return
	 */
	public MISPLicenseResponseDto approveInfraProvider(String mispId);
	
	/**
	 * 
	 * @return
	 */
	public MISPLicenseResponseDto updateInfraProvider(String id, String licenseKey, String status);
	
	/**
	 * 
	 * @return
	 */
	public List<MISPLicenseEntity> getInfraProvider();
	
	/**
	 * 
	 * @return
	 */
	public MISPLicenseResponseDto regenerateKey(String mispId);
	
	/**
	 * 
	 * @param filterValueDto
	 * @return
	 */
	public FilterResponseCodeDto filterValues(FilterValueDto filterValueDto);
	
	/**
	 * 
	 * @param dto
	 * @return
	 */
	public PageResponseDto<MISPLicenseEntity> search(SearchDto dto);
}
