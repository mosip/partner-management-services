package io.mosip.pms.partner.misp.service;

import java.util.List;

import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.partner.misp.dto.MISPFilterDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseSummaryDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseRequestDtoV2;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDtoV2;
import io.mosip.pms.partner.misp.dto.MISPLicenseDetailsDto;
import io.mosip.pms.partner.misp.dto.MISPDeactivateRequestDto;
import io.mosip.pms.partner.misp.dto.MISPDeactivateResponseDto;
import io.mosip.pms.partner.misp.dto.MISPRegenerateRequestDto;

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

	public ResponseWrapperV2<PageResponseV2Dto<MISPLicenseSummaryDto>> getAllMISPLicenses(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, MISPFilterDto filterDto);

	public ResponseWrapperV2<MISPLicenseResponseDtoV2> generateMISPLicense(MISPLicenseRequestDtoV2 request);

	public ResponseWrapperV2<MISPLicenseDetailsDto> getMISPLicenseDetails(String partnerId, String policyId, String mispLicenseKeyName);

	public ResponseWrapperV2<MISPDeactivateResponseDto> deactivateMISPLicense(String partnerId, MISPDeactivateRequestDto request);

	public ResponseWrapperV2<MISPLicenseResponseDtoV2> regenerateMISPLicense(String partnerId, MISPRegenerateRequestDto request);
}
