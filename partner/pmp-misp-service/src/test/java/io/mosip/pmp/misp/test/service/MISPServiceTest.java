package io.mosip.pmp.misp.test.service;

import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.mosip.kernel.core.idgenerator.spi.MISPLicenseGenerator;
import io.mosip.kernel.core.idgenerator.spi.MispIdGenerator;
import io.mosip.pmp.misp.dto.MISPCreateRequestDto;
import io.mosip.pmp.misp.dto.MISPCreateResponseDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.ResponseWrapper;
import io.mosip.pmp.misp.entity.MISPEntity;
import io.mosip.pmp.misp.entity.MISPLicenseEntity;
import io.mosip.pmp.misp.entity.MISPlKeyUniqueKeyEntity;
import io.mosip.pmp.misp.exception.MISPException;
import io.mosip.pmp.misp.repository.MispLicenseKeyRepository;
import io.mosip.pmp.misp.repository.MispServiceRepository;
import io.mosip.pmp.misp.service.MISPManagementService;
import io.mosip.pmp.misp.test.MispServiceTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MispServiceTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class MISPServiceTest {

	@Autowired
	MISPManagementService service;
	
	@Mock
	private MispServiceRepository mispRepository;

	@Mock
	private MispLicenseKeyRepository misplKeyRepository;	

	@Mock
	private MispIdGenerator<String> mispIdGenerator;

	@Mock
	private MISPLicenseGenerator<String> mispLicenseKeyGenerator;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(service, "mispRepository", mispRepository);
		ReflectionTestUtils.setField(service, "misplKeyRepository", misplKeyRepository);
		ReflectionTestUtils.setField(service, "mispIdGenerator", mispIdGenerator);
		ReflectionTestUtils.setField(service, "mispLicenseKeyGenerator", mispLicenseKeyGenerator);
	}
	
	@Test
	public void createMISPTest() {
		MISPCreateRequestDto mispCreateRequest = serviceCreateRequest();
		Mockito.when(mispRepository.findByName(mispCreateRequest.getName())).thenReturn(null);
		Mockito.when(mispRepository.findByName(mispCreateRequest.getName())).thenReturn(null);
		Mockito.when(mispIdGenerator.generateId()).thenReturn("100");			
		ResponseWrapper<MISPCreateResponseDto> response = service.createMISP(mispCreateRequest);
		assertNotNull(response);
	}
	
	@Test(expected = MISPException.class)
	public void processRequestWIthExceptionTest() {
		MISPStatusUpdateRequestDto dto =serviceProcessRequestDto("approve");
		service.processRequest(dto);
	}
	
	
	@Test(expected = RuntimeException.class)
	public void processRequestRunTimeExceptionTest() {
		Optional<MISPEntity> misp = Optional.of(misp_noStatus());
		MISPStatusUpdateRequestDto dto =serviceProcessRequestDto("approved");
		Mockito.when(mispRepository.findById(dto.getMispId())).thenReturn(misp);		
		service.processRequest(dto);		
	}
	
	@Test
	public void processRequestTest() {
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",true));
		MISPStatusUpdateRequestDto dto =serviceProcessRequestDto("approved");
		Mockito.when(mispRepository.findById(dto.getMispId())).thenReturn(misp);
		Mockito.when(mispLicenseKeyGenerator.generateLicense()).thenReturn("asdfgtresdcvbhytredfg");
		service.processRequest(dto);		
	}
	
	@Test
	public void processRequest_RejectedTest() {
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",true));
		MISPStatusUpdateRequestDto dto =serviceProcessRequestDto("rejected");
		List<MISPLicenseEntity> license = mispLicenses();
		Mockito.when(misplKeyRepository.findByMispId("12345")).thenReturn(license);
		Mockito.when(mispRepository.findById(dto.getMispId())).thenReturn(misp);
		Mockito.when(mispLicenseKeyGenerator.generateLicense()).thenReturn("asdfgtresdcvbhytredfg");
		service.processRequest(dto);		
	}
	
	@Test
	public void processRequest_Rejected_Test() {
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",true));
		MISPStatusUpdateRequestDto dto =serviceProcessRequestDto("rejected");
		List<MISPLicenseEntity> license = mispLicenses();
		Mockito.when(misplKeyRepository.findByMispId("12345")).thenReturn(license);
		Mockito.when(mispRepository.findById(dto.getMispId())).thenReturn(misp);
		Mockito.when(mispLicenseKeyGenerator.generateLicense()).thenReturn("asdfgtresdcvbhytredfg");
		service.processRequest(dto);		
	}
	
	@Test(expected = RuntimeException.class)
	public void processRequestApprovedTest() {
		Optional<MISPEntity> misp = Optional.of(misp("approved",true));
		MISPStatusUpdateRequestDto dto =serviceProcessRequestDto("approved");
		Mockito.when(mispRepository.findById(dto.getMispId())).thenReturn(misp);
		Mockito.when(mispLicenseKeyGenerator.generateLicense()).thenReturn("asdfgtresdcvbhytredfg");
		service.processRequest(dto);		
	}
	
	@Test(expected = RuntimeException.class)
	public void processRequestMISPNotActiveTest() {
		Optional<MISPEntity> misp = Optional.of(misp("approved",false));
		MISPStatusUpdateRequestDto dto =serviceProcessRequestDto("approved");
		Mockito.when(mispRepository.findById(dto.getMispId())).thenReturn(misp);
		Mockito.when(mispLicenseKeyGenerator.generateLicense()).thenReturn("asdfgtresdcvbhytredfg");
		service.processRequest(dto);		
	}
	
	@Test
	public void processRequestMISPRejectedTest() {
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",true));
		MISPStatusUpdateRequestDto dto =serviceProcessRequestDto("rejected");
		Mockito.when(mispRepository.findById(dto.getMispId())).thenReturn(misp);
		Mockito.when(mispLicenseKeyGenerator.generateLicense()).thenReturn("asdfgtresdcvbhytredfg");
		service.processRequest(dto);		
	}
	
	@Test(expected = MISPException.class)
	public void processRequestNotActiveApprovedTest() {
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",false));
		MISPStatusUpdateRequestDto dto =serviceProcessRequestDto("approved");
		Mockito.when(mispRepository.findById(dto.getMispId())).thenReturn(misp);
		Mockito.when(mispLicenseKeyGenerator.generateLicense()).thenReturn("asdfgtresdcvbhytredfg");
		service.processRequest(dto);		
	}
	
	@Test(expected = MISPException.class)
	public void updateMISPStatus_InputExceptionTest() {
		MISPStatusUpdateRequestDto request = updateMISPRequest("Notactive");
		service.updateMISPStatus(request);
	}
	
	@Test(expected = MISPException.class)
	public void updateMISPStatus_MISPNotExistsTest() {
		MISPStatusUpdateRequestDto request = updateMISPRequest("de-active");
		service.updateMISPStatus(request);
	}
	
	@Test
	public void updateMISPStatus_DeActiveTest() {
		MISPStatusUpdateRequestDto request = updateMISPRequest("de-active");
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",false));
		Mockito.when(mispRepository.findById(request.getMispId())).thenReturn(misp);
		service.updateMISPStatus(request);
	}
	
	@Test
	public void updateMISPStatus_ActiveTest() {
		MISPStatusUpdateRequestDto request = updateMISPRequest("active");
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",false));
		Mockito.when(mispRepository.findById(request.getMispId())).thenReturn(misp);
		service.updateMISPStatus(request);
	}
	
	@Test
	public void updateMISPStatus_Active_Lice_After_Test() {
		MISPStatusUpdateRequestDto request = updateMISPRequest("active");
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",false));
		List<MISPLicenseEntity> license = mispLicenses();
		Mockito.when(misplKeyRepository.findByMispId("12345")).thenReturn(license);
		Mockito.when(mispRepository.findById(request.getMispId())).thenReturn(misp);
		service.updateMISPStatus(request);
	}
	
	@Test
	public void updateMISPStatus_Active_Lice_Before_Test() {
		MISPStatusUpdateRequestDto request = updateMISPRequest("active");
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",false));
		List<MISPLicenseEntity> license = mispLicenses();
		license.get(0).setValidToDate(LocalDateTime.now().plusDays(-1));
		Mockito.when(misplKeyRepository.findByMispId("12345")).thenReturn(license);
		Mockito.when(mispRepository.findById(request.getMispId())).thenReturn(misp);
		service.updateMISPStatus(request);
	}	
	
	public void updateMISP_updateTest() {
		MISPUpdateRequestDto request = updateRequest();
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",false));
		MISPEntity mispName = misp.get();
		Mockito.when(mispRepository.findById(request.getMispID())).thenReturn(misp);
		Mockito.when(mispRepository.findByName(request.getName())).thenReturn(mispName);
		service.update(request);
	}
	
	@Test(expected = MISPException.class)
	public void updateMISP_NameExistsTest_01() {
		MISPUpdateRequestDto request = updateRequest();
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",false));
		MISPEntity mispName = misp_update("Inprogress",false);
		Mockito.when(mispRepository.findById(request.getMispID())).thenReturn(misp);
		Mockito.when(mispRepository.findByName(request.getName())).thenReturn(mispName);
		service.update(request);
	}
	
	@Test
	public void updateMISPTest() {
		MISPUpdateRequestDto request = updateRequest();
		Optional<MISPEntity> misp = Optional.of(misp("Inprogress",false));
		MISPEntity mispName = misp.get();
		mispName.setName("Telecom");
		Mockito.when(mispRepository.findById(request.getMispID())).thenReturn(misp);
		Mockito.when(mispRepository.findByName(mispName.getName())).thenReturn(mispName);
		service.update(request);
	}
	
	@Test(expected = MISPException.class)
	public void UpdateMISPLkeyStatus_InputExceptionTest() {
		
		service.updateMisplkeyStatus(LkeyStatusUpdateRequest("activated","active"));
	}
	
	@Test(expected = MISPException.class)
	public void UpdateMISPLkeyStatus_KeyNotExistsTest() {
		MISPLicenseEntity license = mispLicense();
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		MISPlKeyStatusUpdateRequestDto dto = LkeyStatusUpdateRequest("active","active");
		service.updateMisplkeyStatus(dto);
	}
	
	@Test(expected = MISPException.class)
	public void UpdateMISPLkeyStatus_KeyIDCombinationNotExistsTest() {
		MISPLicenseEntity license = mispLicense();
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		MISPlKeyStatusUpdateRequestDto dto = LkeyStatusUpdateRequest("active","active");
		dto.setMispLicenseKey("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy");
		service.updateMisplkeyStatus(dto);
	}
	
	@Test(expected = MISPException.class)
	public void UpdateMISPLkeyStatus_KeyExpiredTest() {
		MISPLicenseEntity license = mispLicense();
		license.setValidToDate(LocalDateTime.now().plusDays(-10));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		MISPlKeyStatusUpdateRequestDto dto = LkeyStatusUpdateRequest("active","active");
		dto.setMispLicenseKey("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy");
		dto.setMispId("100");
		service.updateMisplkeyStatus(dto);
	}
	
	@Test
	public void UpdateMISPLkeyStatusTest() {
		MISPLicenseEntity license = mispLicense();
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		MISPlKeyStatusUpdateRequestDto dto = LkeyStatusUpdateRequest("active","active");
		dto.setMispLicenseKey("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy");
		dto.setMispId("100");
		service.updateMisplkeyStatus(dto);
	}
	
	@Test
	public void getMISPsTest() {
		List<MISPEntity> misp = misp();
		Mockito.when(mispRepository.findAll()).thenReturn(misp);
		service.getMisps();
	}
	
	@Test(expected = MISPException.class)
	public void getMISPS_NotExistsTest() {
		List<MISPEntity> misp = new ArrayList<MISPEntity>();
		Mockito.when(mispRepository.findAll()).thenReturn(misp);
		service.getMisps();
	}
	
	@Test(expected = MISPException.class)
	public void getMISP_NotExistsTest() {
		List<MISPLicenseEntity> license = mispLicenses();
		Mockito.when(misplKeyRepository.findByMispId("12345")).thenReturn(license);
		Optional<MISPEntity> misp = Optional.of( misp().get(0));
		Mockito.when(mispRepository.findById("12345")).thenReturn(misp);
		service.getMisp("100");
	}
	
	@Test
	public void getMISPTest() {
		List<MISPLicenseEntity> license = mispLicenses();
		Mockito.when(misplKeyRepository.findByMispId("12345")).thenReturn(license);
		Optional<MISPEntity> misp = Optional.of( misp().get(0));
		Mockito.when(mispRepository.findById("12345")).thenReturn(misp);
		service.getMisp("12345");
	}
	
	@Test
	public void validateLicenseKeyTest() {
		MISPlKeyUniqueKeyEntity request = mispLUniqueKey();
		MISPLicenseEntity license = mispLicense();
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		service.validateLicenseKey(request);
	}
	
	@Test(expected = MISPException.class)
	public void validateLicenseKey_ID_L_ExceptionTest() {
		MISPlKeyUniqueKeyEntity request = mispLUniqueKey();
		request.setMisp_id("98766");
		MISPLicenseEntity license = mispLicense();
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		service.validateLicenseKey(request);
	}
	
	@Test(expected = MISPException.class)
	public void validateLicenseKey_L_ExceptionTest() {
		MISPlKeyUniqueKeyEntity request = mispLUniqueKey();
		request.setMisp_id("98766");
		MISPLicenseEntity license = mispLicense();
		license.setValidToDate(LocalDateTime.now().plusDays(-10));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		service.validateLicenseKey(request);
	}
	
	@Test
	public void getMISPByOrgTest() {
		List<MISPEntity> misps = misp();
		Mockito.when(mispRepository.findByStartsWithName("hello")).thenReturn(misps);
		service.getMispsByOrg("hello");
	}
	
	@Test(expected = MISPException.class)
	public void getMISPByOrg_EmptyExceptionTest() {
		List<MISPEntity> misps = new ArrayList<MISPEntity>();
		Mockito.when(mispRepository.findByStartsWithName("hello")).thenReturn(misps);
		service.getMispsByOrg("hello");
	}
	
	@Test(expected = MISPException.class)
	public void retriveLicense_LicenseNotExistTest() {
		Optional<MISPEntity> misp = Optional.of(misp_noStatus());
		misp.get().setStatus_code("approved");
		misp.get().setIsActive(true);
		Mockito.when(mispRepository.findById("100")).thenReturn(misp);
		service.retriveLicense("100");
	}
	
	@Test(expected = MISPException.class)
	public void retriveLicense_NotActiveTest() {
		Optional<MISPEntity> misp = Optional.of(misp_noStatus());
		misp.get().setStatus_code("approved");
		misp.get().setIsActive(false);
		Mockito.when(mispRepository.findById("100")).thenReturn(misp);
		service.retriveLicense("100");
	}
	
	@Test(expected = MISPException.class)
	public void retriveLicense_NotApprovedTest() {
		Optional<MISPEntity> misp = Optional.of(misp_noStatus());
		misp.get().setStatus_code("Inprogress");
		Mockito.when(mispRepository.findById("100")).thenReturn(misp);
		service.retriveLicense("100");
	}
	
	@Test
	public void retriveLicenseTest() {
		Optional<MISPEntity> misp = Optional.of(misp_noStatus());
		misp.get().setStatus_code("approved");
		misp.get().setIsActive(true);
		List<MISPLicenseEntity> license = mispLicenses();
		license.get(0).setIsActive(true);
		Mockito.when(misplKeyRepository.findByMispId("100")).thenReturn(license);
		Mockito.when(mispRepository.findById("100")).thenReturn(misp);
		service.retriveLicense("100");
	}
	
	@Test(expected = MISPException.class)
	public void retriveLicense__LNotActiveTest() {
		Optional<MISPEntity> misp = Optional.of(misp_noStatus());
		misp.get().setStatus_code("approved");
		misp.get().setIsActive(true);
		List<MISPLicenseEntity> license = mispLicenses();
		license.get(0).setIsActive(false);
		Mockito.when(misplKeyRepository.findByMispId("100")).thenReturn(license);
		Mockito.when(mispRepository.findById("100")).thenReturn(misp);
		service.retriveLicense("100");
	}
	
	@Test
	public void retriveLicense_license_ExpiredTest() {
		Optional<MISPEntity> misp = Optional.of(misp_noStatus());
		misp.get().setStatus_code("approved");
		misp.get().setIsActive(true);
		List<MISPLicenseEntity> license = mispLicenses();
		license.get(0).setIsActive(true);
		license.get(0).setValidToDate(LocalDateTime.now().plusDays(-10));
		Mockito.when(misplKeyRepository.findByMispId("100")).thenReturn(license);
		Mockito.when(mispRepository.findById("100")).thenReturn(misp);
		service.retriveLicense("100");
	}
	@Test
	public void retriveLicense_license_Test() {
		Optional<MISPEntity> misp = Optional.of(misp_noStatus());
		misp.get().setStatus_code("approved");
		misp.get().setIsActive(true);
		List<MISPLicenseEntity> license = mispLicenses();
		license.get(0).setIsActive(true);
		//license.get(0).setValidToDate(LocalDateTime.now().plusDays(-10));
		Mockito.when(misplKeyRepository.findByMispId("100")).thenReturn(license);
		Mockito.when(mispRepository.findById("100")).thenReturn(misp);
		service.retriveLicense("100");
	}
	private MISPlKeyStatusUpdateRequestDto LkeyStatusUpdateRequest(String lStatus, String mispSatus) {
		MISPlKeyStatusUpdateRequestDto dto = new MISPlKeyStatusUpdateRequestDto();
		dto.setMispId("12345");
		dto.setMispLicenseKey("asdfghjkkiuytrewqHF");
		dto.setMispLicenseKeyStatus(lStatus);
		dto.setMispStatus(mispSatus);
		return dto;
	}
	private MISPUpdateRequestDto updateRequest() {
		MISPUpdateRequestDto dto = new MISPUpdateRequestDto();
		dto.setAddress("Banaglore");
		dto.setContactNumber("9902344554");
		dto.setEmailId("India@gmail.com");
		dto.setMispID("12345");
		dto.setName("Huwai");		
		
		return dto;
	}
	
	private MISPStatusUpdateRequestDto updateMISPRequest(String status) {
		MISPStatusUpdateRequestDto request = new MISPStatusUpdateRequestDto();
		request.setMispId("12345");
		request.setMispStatus(status);
		return request;
	}
	private MISPlKeyUniqueKeyEntity mispLUniqueKey() {
		MISPlKeyUniqueKeyEntity unique = new MISPlKeyUniqueKeyEntity();
		unique.setMisp_id("100");
		unique.setLicense_key("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy");
		return unique;
	}
	
	private MISPLicenseEntity mispLicense() {
		MISPLicenseEntity mispLices = new MISPLicenseEntity();
		mispLices.setMispLicenseUniqueKey(mispLUniqueKey());
		mispLices.setValidFromDate(LocalDateTime.now());
		mispLices.setValidToDate(LocalDateTime.now().plusDays(90));
		mispLices.setIsActive(true);
		return mispLices;
	}
	
	private List<MISPLicenseEntity> mispLicenses() {
		List<MISPLicenseEntity> licenses = new ArrayList<MISPLicenseEntity>();
		MISPLicenseEntity mispLices = new MISPLicenseEntity();
		mispLices.setMispLicenseUniqueKey(mispLUniqueKey());
		mispLices.setValidFromDate(LocalDateTime.now());
		mispLices.setValidToDate(LocalDateTime.now().plusDays(90));
		licenses.add(mispLices);
		
		return licenses;
	}
	
	private MISPStatusUpdateRequestDto serviceProcessRequestDto(String status) {
		MISPStatusUpdateRequestDto dto = new MISPStatusUpdateRequestDto();
		dto.setMispId("12345");
		dto.setMispStatus(status);
		
		return dto;
	}
	
	private MISPCreateRequestDto serviceCreateRequest() {
		MISPCreateRequestDto dto = new MISPCreateRequestDto();
		dto.setAddress("Bangalore");
		dto.setContactNumber("1234567890");
		dto.setEmailId("airtel@gmail.com");
		dto.setName("Airtel");
		dto.setOrganizationName("Airtel");
		
		return dto;
	}
	
	private MISPEntity misp_noStatus() {
		MISPEntity dto = new MISPEntity();
		dto.setAddress("Bangalore");
		dto.setContactNumber("1234567890");
		dto.setEmailId("airtel@gmail.com");
		dto.setName("Airtel");
		dto.setID("100");;
		
		return dto;
	}
	
	private MISPEntity misp(String status_code, boolean isActive) {
		MISPEntity dto = new MISPEntity();
		dto.setAddress("Bangalore");
		dto.setContactNumber("1234567890");
		dto.setEmailId("airtel@gmail.com");
		dto.setName("Airtel");
		dto.setID("100");;
		dto.setStatus_code(status_code);
		dto.setIsActive(isActive);
		return dto;
	}
	
	
	private MISPEntity misp_update(String status_code, boolean isActive) {
		MISPEntity dto = new MISPEntity();
		dto.setAddress("Bangalore");
		dto.setContactNumber("1234567890");
		dto.setEmailId("airtel@gmail.com");
		dto.setName("Airtel");
		dto.setID("101");;
		dto.setStatus_code(status_code);
		dto.setIsActive(isActive);
		return dto;
	}
	
	private List<MISPEntity> misp() {
		List<MISPEntity> misps = new ArrayList<MISPEntity>();
		MISPEntity dto = new MISPEntity();
		dto.setAddress("Bangalore");
		dto.setContactNumber("1234567890");
		dto.setEmailId("airtel@gmail.com");
		dto.setName("Airtel");
		dto.setID("100");;
		dto.setStatus_code("approved");
		dto.setIsActive(true);
		misps.add(dto);
		return misps;
	}
}
