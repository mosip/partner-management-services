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
import io.mosip.pms.partner.misp.dto.MISPLicensePatchRequestDto;
import io.mosip.pms.partner.misp.dto.MISPDeactivateResponseDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseSummaryDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseRequestDtoV2;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDtoV2;
import io.mosip.pms.partner.misp.dto.MISPLicenseDetailsDto;

public interface InfraServiceProviderService {

	public MISPLicenseResponseDto approveInfraProvider(String mispId);

	public MISPLicenseResponseDto updateInfraProvider(String id, String licenseKey, String status);

	public List<MISPLicenseEntity> getInfraProvider();

	public MISPLicenseResponseDto regenerateKey(String mispId);

	public FilterResponseCodeDto filterValues(FilterValueDto filterValueDto);

	public PageResponseDto<MISPLicenseEntity> search(SearchDto dto);

	public ResponseWrapperV2<PageResponseV2Dto<MISPLicenseSummaryDto>> getAllMISPLicenses(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, MISPFilterDto filterDto);

	public ResponseWrapperV2<MISPLicenseResponseDtoV2> generateMISPLicense(MISPLicenseRequestDtoV2 request);

	public ResponseWrapperV2<MISPLicenseDetailsDto> getMISPLicenseDetails(String mispLicenseId);

	public ResponseWrapperV2<MISPDeactivateResponseDto> updateMISPLicense(String mispLicenseId, MISPLicensePatchRequestDto request);
}
