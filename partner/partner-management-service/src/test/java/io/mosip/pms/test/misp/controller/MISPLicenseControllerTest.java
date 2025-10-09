package io.mosip.pms.test.misp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.util.RequestValidator;
import io.mosip.pms.partner.misp.controller.MISPLicenseController;
import io.mosip.pms.partner.misp.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.misp.exception.MISPServiceException;
import io.mosip.pms.partner.misp.service.InfraServiceProviderService;
import io.mosip.pms.partner.misp.service.impl.InfraProviderServiceImpl;
import io.mosip.pms.common.validator.InputValidator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class MISPLicenseControllerTest {

	@Autowired
    private MockMvc mockMvc;

	@MockBean
	private InfraProviderServiceImpl infraProvidertService;
	
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	RequestValidator requestValidator;

	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void generateLicenseTest () throws MISPServiceException , Exception{
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		Mockito.when(infraProvidertService.approveInfraProvider(Mockito.any())).thenReturn(response);
		RequestWrapper<MISPLicenseRequestDto> request = createRequest();
		mockMvc.perform(post("/misps").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void updateLicenseDetailsTest () throws MISPServiceException , Exception{
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		Mockito.when(infraProvidertService.updateInfraProvider(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);
		RequestWrapper<MISPLicenseUpdateRequestDto> request = createUpdateRequest();
		mockMvc.perform(put("/misps").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}


	@Test
	@WithMockUser(roles = {"MISP_PARTNER"})
	public void getLicenseDetailsTest () throws MISPServiceException , Exception{
		mockMvc.perform(MockMvcRequestBuilders.get("/misps")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void regenarteLicenseKeyTest () throws Exception{
		Mockito.when(infraProvidertService.regenerateKey("12345")).thenReturn(new MISPLicenseResponseDto() );
		mockMvc.perform(MockMvcRequestBuilders.get("/misps/12345/licenseKey")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void filterValues () throws Exception{
		FilterResponseCodeDto response = new FilterResponseCodeDto();
		Mockito.when(infraProvidertService.filterValues(createFilterRequest().getRequest())).thenReturn(response);
		mockMvc.perform(post("/misps/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createFilterRequest()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void search () throws Exception{
		PageResponseDto<MISPLicenseEntity> response = new PageResponseDto<MISPLicenseEntity>();
		Mockito.when(infraProvidertService.search(searchRequest().getRequest())).thenReturn(response);
		mockMvc.perform(post("/misps/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(searchRequest()))).andExpect(status().isOk());
	}

    private RequestWrapper<FilterValueDto> createFilterRequest(){
    	RequestWrapper<FilterValueDto> request = new RequestWrapper<FilterValueDto>();
    	request.setId("mosip.partnermanagement.sbi.filtervalues");
    	request.setMetadata("{}");
    	request.setRequest(filterValuesRequest());
    	request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    	request.setVersion("1.0");
    	return request;
    }

    private FilterValueDto filterValuesRequest() {
    	FilterValueDto sbiFilterValueDto = new FilterValueDto();
    	FilterDto filterDto = new FilterDto();
    	SearchFilter searchFilter = new SearchFilter();
    	searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("all");
		searchFilter.setValue("m");
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
    	searchDtos.add(searchFilter);
    	filterDto.setColumnName("name");
    	filterDto.setText("");
    	filterDto.setType("all");
    	List<FilterDto> filterDtos = new ArrayList<FilterDto>();
    	filterDtos.add(filterDto);
    	sbiFilterValueDto.setFilters(filterDtos);
    	return sbiFilterValueDto;
    }
	private RequestWrapper<MISPLicenseRequestDto> createRequest() {
		RequestWrapper<MISPLicenseRequestDto> request = new RequestWrapper<MISPLicenseRequestDto>();
		request.setId("mosip.partnerservice.MispLicense.create");
		request.setMetadata("{}");
		request.setRequest(mispLicenseRequest());
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setVersion("1.0");
		return request;
	}

	private MISPLicenseRequestDto mispLicenseRequest() {
		MISPLicenseRequestDto requestDto = new MISPLicenseRequestDto();
		requestDto.setProviderId("12345");
		return requestDto;
	}

	private RequestWrapper<MISPLicenseUpdateRequestDto> createUpdateRequest() {
		RequestWrapper<MISPLicenseUpdateRequestDto> request = new RequestWrapper<MISPLicenseUpdateRequestDto>();
		request.setId("mosip.partnerservice.MispLicense.update");
		request.setMetadata("{}");
		request.setRequest(mispLicenseUpdateRequest());
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setVersion("1.0");
		return request;
	}

	private MISPLicenseUpdateRequestDto mispLicenseUpdateRequest() {
		MISPLicenseUpdateRequestDto requestDto = new MISPLicenseUpdateRequestDto();
		requestDto.setLicenseKey("12345");
		requestDto.setLicenseKeyStatus("active");
		requestDto.setProviderId("1234");
		return requestDto;
	}

    private SearchDto searchMISPRequest () {
    	SearchDto dto = new SearchDto();
    	Pagination pagination = new Pagination();
    	SearchSort searchSort = new SearchSort();
    	SearchFilter searchFilter = new SearchFilter();
    	searchSort.setSortField("model");
    	searchSort.setSortType("asc");
    	searchFilter.setColumnName("model");
    	searchFilter.setFromValue("");
    	searchFilter.setToValue("");
    	searchFilter.setType("STARTSWITH");
    	searchFilter.setValue("b");
    	List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
    	searchDtos1.add(searchSort);
    	List<SearchFilter> searchfilterDtos = new ArrayList<SearchFilter>();
    	searchfilterDtos.add(searchFilter);
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
    	dto.setFilters(searchfilterDtos);
    	dto.setPagination(pagination);
    	dto.setSort(searchDtos1);
    	return dto;
    }

    private RequestWrapper<SearchDto> searchRequest() {
    	RequestWrapper<SearchDto> request = new RequestWrapper<SearchDto>();
        request.setRequest(searchMISPRequest());
        request.setId("mosip.partnermanagement.sbi.update");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllMispLicensesTest() throws Exception {
		String sortFieldName = "field";
		String sortType = "asc";
		Integer pageNo = 1;
		Integer pageSize = 10;
		String partnerId = "PARTNER1";
		String orgName = "ORG";
		String policyGroupName = "PG";
		String policyName = "PN";
		String mispLicenseKeyName = "KEY";
		String status = "activated";
		MISPFilterDto filterDto = new MISPFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<MISPLicenseSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		when(infraProvidertService.getAllMISPLicenses(sortFieldName, sortType, pageNo, pageSize, filterDto))
				.thenReturn(responseWrapper);

		mockMvc.perform(MockMvcRequestBuilders.get("/misp-licenses")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize))
						.param("partnerId", partnerId)
						.param("orgName", orgName)
						.param("policyGroupName", policyGroupName)
						.param("policyName", policyName)
						.param("status", status)
						.param("mispLicenseKeyName", mispLicenseKeyName))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllMispLicensesTest_WithoutFilter() throws Exception {
		String sortFieldName = "partnerId";
		String sortType = "asc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		MISPFilterDto filterDto = new MISPFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<MISPLicenseSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		when(infraProvidertService.getAllMISPLicenses(sortFieldName, sortType, pageNo, pageSize, filterDto))
				.thenReturn(responseWrapper);

		mockMvc.perform(MockMvcRequestBuilders.get("/misp-licenses")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize)))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	private MISPLicenseRequestDtoV2 generateMISPRequest() {
		MISPLicenseRequestDtoV2 request = new MISPLicenseRequestDtoV2();
		request.setPartnerId("PARTNER1");
		request.setLicenseKeyName("KEY1");
		request.setExpiryDate(LocalDate.now().plusDays(10));
		return request;
	}

	private RequestWrapperV2<MISPLicenseRequestDtoV2> generateMISPRequestWrapper() {
		RequestWrapperV2<MISPLicenseRequestDtoV2> requestWrapper = new RequestWrapperV2<>();
		requestWrapper.setId("mosip.pms.misp.generate.license.post");
		requestWrapper.setVersion("1.0");
		requestWrapper.setRequest(generateMISPRequest());
		return requestWrapper;
	}

	private ResponseWrapperV2<MISPLicenseResponseDtoV2> generateMISPResponseWrapper() {
		ResponseWrapperV2<MISPLicenseResponseDtoV2> responseWrapper = new ResponseWrapperV2<>();
		responseWrapper.setId("mosip.pms.misp.generate.license.post");
		responseWrapper.setVersion("1.0");
		MISPLicenseResponseDtoV2 response = new MISPLicenseResponseDtoV2();
		responseWrapper.setResponse(response);
		return responseWrapper;
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void generateMispLicenseTest() throws Exception {
		ResponseWrapperV2<Object> errorResponseWrapper = new ResponseWrapperV2<>();
		when(requestValidator.validate(any(), any())).thenReturn(Optional.of(errorResponseWrapper));
		Mockito.when(infraProvidertService.generateMISPLicense(generateMISPRequest())).thenReturn(generateMISPResponseWrapper());
		mockMvc.perform(MockMvcRequestBuilders.post("/misp-licenses").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(generateMISPRequestWrapper()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void generateMispLicenseTest_InvalidRequest() throws Exception {
		when(requestValidator.validate(any(), any())).thenReturn(Optional.empty());
		Mockito.when(infraProvidertService.generateMISPLicense(generateMISPRequest())).thenReturn(generateMISPResponseWrapper());
		mockMvc.perform(MockMvcRequestBuilders.post("/misp-licenses").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(generateMISPRequestWrapper()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getMISPLicenseDetailsTest() throws Exception {
		String partnerId = "PARTNER1";
		String licenseKeyName = "KEY1";
		String policyId = "POLICY1";
		ResponseWrapperV2<MISPLicenseDetailsDto> responseWrapper = new ResponseWrapperV2<>();
		MISPLicenseDetailsDto detailsDto = new MISPLicenseDetailsDto();
		responseWrapper.setResponse(detailsDto);
		when(infraProvidertService.getMISPLicenseDetails(partnerId, licenseKeyName, policyId))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/misp-licenses/PARTNER1")
						.param("licenseKeyName", licenseKeyName)
						.param("policyId", policyId))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	private MISPDeactivateRequestDto deactivateMISPRequest() {
		MISPDeactivateRequestDto request = new MISPDeactivateRequestDto();
		request.setPolicyId("policy1");
		request.setLicenseKeyName("newKey");
		request.setStatus("De-Activate");
		return request;
	}

	private RequestWrapperV2<MISPDeactivateRequestDto> deactivateMISPRequestWrapper() {
		RequestWrapperV2<MISPDeactivateRequestDto> requestWrapper = new RequestWrapperV2<>();
		requestWrapper.setId("mosip.pms.deactivate.misp.license.patch");
		requestWrapper.setVersion("1.0");
		requestWrapper.setRequest(deactivateMISPRequest());
		return requestWrapper;
	}

	private ResponseWrapperV2<MISPDeactivateResponseDto> deactivateMISPResponseWrapper() {
		ResponseWrapperV2<MISPDeactivateResponseDto> responseWrapper = new ResponseWrapperV2<>();
		responseWrapper.setId("mosip.pms.deactivate.misp.license.patch");
		responseWrapper.setVersion("1.0");
		MISPDeactivateResponseDto response = new MISPDeactivateResponseDto();
		responseWrapper.setResponse(response);
		return responseWrapper;
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void deactivateMISPLicenseTest() throws Exception {
		ResponseWrapperV2<Object> errorResponseWrapper = new ResponseWrapperV2<>();
		when(requestValidator.validate(any(), any())).thenReturn(Optional.of(errorResponseWrapper));
		Mockito.when(infraProvidertService.deactivateMISPLicense("partner1", deactivateMISPRequest())).thenReturn(deactivateMISPResponseWrapper());

		mockMvc.perform(MockMvcRequestBuilders.patch("/misp-licenses/partner1").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(deactivateMISPRequestWrapper()))).andExpect(status().isOk());

	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void deactivateMISPLicenseTest_InvalidRequest() throws Exception {
		when(requestValidator.validate(any(), any())).thenReturn(Optional.empty());
		Mockito.when(infraProvidertService.deactivateMISPLicense("partner1", deactivateMISPRequest())).thenReturn(deactivateMISPResponseWrapper());

		mockMvc.perform(MockMvcRequestBuilders.patch("/misp-licenses/partner1").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(deactivateMISPRequestWrapper()))).andExpect(status().isOk());

	}

	private MISPRegenerateRequestDto regenerateMISPRequest() {
		MISPRegenerateRequestDto request = new MISPRegenerateRequestDto();
		request.setPolicyId("policy1");
		request.setLicenseKeyName("newKey");
		request.setExpiryDate(LocalDate.now());
		return request;
	}

	private RequestWrapperV2<MISPRegenerateRequestDto> regenerateMISPRequestWrapper() {
		RequestWrapperV2<MISPRegenerateRequestDto> requestWrapper = new RequestWrapperV2<>();
		requestWrapper.setId("mosip.pms.regenerate.misp.license.put");
		requestWrapper.setVersion("1.0");
		requestWrapper.setRequest(regenerateMISPRequest());
		return requestWrapper;
	}

	private ResponseWrapperV2<MISPLicenseResponseDtoV2> regenerateMISPResponseWrapper() {
		ResponseWrapperV2<MISPLicenseResponseDtoV2> responseWrapper = new ResponseWrapperV2<>();
		responseWrapper.setId("mosip.pms.regenerate.misp.license.put");
		responseWrapper.setVersion("1.0");
		MISPLicenseResponseDtoV2 response = new MISPLicenseResponseDtoV2();
		responseWrapper.setResponse(response);
		return responseWrapper;
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void regenerateMISPLicenseTest() throws Exception {
		ResponseWrapperV2<Object> errorResponseWrapper = new ResponseWrapperV2<>();
		when(requestValidator.validate(any(), any())).thenReturn(Optional.of(errorResponseWrapper));
		Mockito.when(infraProvidertService.regenerateMISPLicense("partner1", regenerateMISPRequest())).thenReturn(regenerateMISPResponseWrapper());

		mockMvc.perform(MockMvcRequestBuilders.put("/misp-licenses/partner1").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(regenerateMISPRequestWrapper()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void regenerateMISPLicenseTest_InvalidRequest() throws Exception {
		when(requestValidator.validate(any(), any())).thenReturn(Optional.empty());
		Mockito.when(infraProvidertService.regenerateMISPLicense("partner1", regenerateMISPRequest())).thenReturn(regenerateMISPResponseWrapper());

		mockMvc.perform(MockMvcRequestBuilders.put("/misp-licenses/partner1").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(regenerateMISPRequestWrapper()))).andExpect(status().isOk());
	}
}
