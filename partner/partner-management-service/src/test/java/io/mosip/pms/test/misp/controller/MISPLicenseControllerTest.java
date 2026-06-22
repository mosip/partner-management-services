package io.mosip.pms.test.misp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void searchMISPWithValidData() throws Exception{
		PageResponseDto<MISPLicenseEntity> pageResponse = new PageResponseDto<>();
		List<MISPLicenseEntity> data = new ArrayList<>();
		MISPLicenseEntity entity = new MISPLicenseEntity();
		data.add(entity);
		pageResponse.setData(data);
		Mockito.when(infraProvidertService.search(any(SearchDto.class))).thenReturn(pageResponse);
		mockMvc.perform(post("/misps/search")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(searchRequest())))
				.andExpect(status().isOk());
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
		String status = "ACTIVE";
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

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getMispLicensesWithVeryLargePageNoReturnsValidationError() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/misp-licenses")
						.param("pageNo", "4567890908909")
						.param("pageSize", "8")
						.contentType(MediaType.APPLICATION_JSON_VALUE))

				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].errorCode").value("PMS_PRT_360"))
				.andExpect(jsonPath("$.errors[0].message").value("Invalid Page No"));

		verify(infraProvidertService, never())
				.getAllMISPLicenses(
						any(),
						any(),
						any(),
						any(),
						any(MISPFilterDto.class));
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllMispLicensesTestWithExpiryPeriod() throws Exception {
		Integer expiryPeriod = 15;
		ResponseWrapperV2<PageResponseV2Dto<MISPLicenseSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		when(infraProvidertService.getAllMISPLicenses(any(), any(), any(), any(), any(MISPFilterDto.class)))
				.thenReturn(responseWrapper);

		mockMvc.perform(MockMvcRequestBuilders.get("/misp-licenses")
						.param("expiryPeriod", String.valueOf(expiryPeriod)))
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
		String mispLicenseId = "550e8400-e29b-41d4-a716-446655440000";
		ResponseWrapperV2<MISPLicenseDetailsDto> responseWrapper = new ResponseWrapperV2<>();
		MISPLicenseDetailsDto detailsDto = new MISPLicenseDetailsDto();
		responseWrapper.setResponse(detailsDto);
		when(infraProvidertService.getMISPLicenseDetails(mispLicenseId)).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/misp-licenses/" + mispLicenseId))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	private MISPLicensePatchRequestDto updateMISPRequest() {
		MISPLicensePatchRequestDto request = new MISPLicensePatchRequestDto();
		request.setStatus("INACTIVE");
		return request;
	}

	private RequestWrapperV2<MISPLicensePatchRequestDto> updateMISPRequestWrapper() {
		RequestWrapperV2<MISPLicensePatchRequestDto> requestWrapper = new RequestWrapperV2<>();
		requestWrapper.setId("mosip.pms.update.misp.license.patch");
		requestWrapper.setVersion("1.0");
		requestWrapper.setRequest(updateMISPRequest());
		return requestWrapper;
	}

	private ResponseWrapperV2<MISPLicenseResponseDtoV2> updateMISPResponseWrapper() {
		ResponseWrapperV2<MISPLicenseResponseDtoV2> responseWrapper = new ResponseWrapperV2<>();
		responseWrapper.setId("mosip.pms.update.misp.license.patch");
		responseWrapper.setVersion("1.0");
		MISPLicenseResponseDtoV2 response = new MISPLicenseResponseDtoV2();
		responseWrapper.setResponse(response);
		return responseWrapper;
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void updateMISPLicenseTest() throws Exception {
		String mispLicenseId = "550e8400-e29b-41d4-a716-446655440000";
		when(requestValidator.validate(any(), any())).thenReturn(Optional.empty());
		Mockito.when(infraProvidertService.updateMISPLicense(eq(mispLicenseId), any(MISPLicensePatchRequestDto.class)))
				.thenReturn(updateMISPResponseWrapper());

		mockMvc.perform(MockMvcRequestBuilders.patch("/misp-licenses/" + mispLicenseId)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(updateMISPRequestWrapper())))
				.andExpect(status().isOk());

		verify(infraProvidertService).updateMISPLicense(eq(mispLicenseId), any(MISPLicensePatchRequestDto.class));
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void updateMISPLicenseTest_InvalidRequest() throws Exception {
		String mispLicenseId = "550e8400-e29b-41d4-a716-446655440000";
		// Validation fails -> controller returns the validator error and must NOT call the service.
		ResponseWrapperV2<Object> errorResponseWrapper = new ResponseWrapperV2<>();
		when(requestValidator.validate(any(), any())).thenReturn(Optional.of(errorResponseWrapper));

		mockMvc.perform(MockMvcRequestBuilders.patch("/misp-licenses/" + mispLicenseId)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(updateMISPRequestWrapper())))
				.andExpect(status().isOk());

		verify(infraProvidertService, never()).updateMISPLicense(anyString(), any(MISPLicensePatchRequestDto.class));
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void updateMISPLicenseTest_WithExpiryDate() throws Exception {
		String mispLicenseId = "550e8400-e29b-41d4-a716-446655440000";
		when(requestValidator.validate(any(), any())).thenReturn(Optional.empty());
		MISPLicensePatchRequestDto patchRequest = new MISPLicensePatchRequestDto();
		patchRequest.setExpiryDate(LocalDate.now().plusDays(30));
		RequestWrapperV2<MISPLicensePatchRequestDto> requestWrapper = new RequestWrapperV2<>();
		requestWrapper.setId("mosip.pms.update.misp.license.patch");
		requestWrapper.setVersion("1.0");
		requestWrapper.setRequest(patchRequest);
		Mockito.when(infraProvidertService.updateMISPLicense(anyString(), any())).thenReturn(updateMISPResponseWrapper());

		mockMvc.perform(MockMvcRequestBuilders.patch("/misp-licenses/" + mispLicenseId)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper)))
				.andExpect(status().isOk());
	}
}
