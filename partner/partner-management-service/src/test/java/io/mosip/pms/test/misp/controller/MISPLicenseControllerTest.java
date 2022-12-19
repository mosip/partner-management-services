package io.mosip.pms.test.misp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.misp.dto.MISPLicenseRequestDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseUpdateRequestDto;
import io.mosip.pms.partner.misp.exception.MISPServiceException;
import io.mosip.pms.partner.misp.service.impl.InfraProviderServiceImpl;

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
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
	
	@MockBean
	private AuditUtil auditUtil;
	
	@Autowired
	private ObjectMapper objectMapper;	
	
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

	
}
