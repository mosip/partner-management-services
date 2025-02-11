package io.mosip.pms.device.authdevice.service.impl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;
import java.util.Map;
import java.util.stream.Collectors;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.SbiSummaryEntity;
import io.mosip.pms.device.authdevice.repository.SbiSummaryRepository;
import io.mosip.pms.device.dto.SbiFilterDto;
import io.mosip.pms.device.util.DeviceUtil;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.dto.DeviceDto;
import io.mosip.pms.device.dto.SbiDetailsDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.device.response.dto.SbiDetailsResponseDto;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.partner.util.PartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pms.common.constant.CommonConstant;
import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.entity.DeviceDetailSBI;
import io.mosip.pms.common.entity.DeviceDetailSBIPK;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterfaceHistory;
import io.mosip.pms.device.authdevice.entity.RegistrationDeviceSubType;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceHistoryRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.device.authdevice.repository.RegistrationDeviceSubTypeRepository;
import io.mosip.pms.device.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.constant.DeviceDetailExceptionsConstant;
import io.mosip.pms.device.constant.SecureBiometricInterfaceConstant;
import io.mosip.pms.device.request.dto.DeviceDetailSBIMappingDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.request.dto.DeactivateSbiRequestDto;
import io.mosip.pms.device.response.dto.ColumnCodeValue;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.MappedDeviceDetailsReponse;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;
import io.mosip.pms.device.response.dto.SbiSummaryDto;
import io.mosip.pms.device.util.AuditUtil;

@Component
@Transactional
public class SecureBiometricInterfaceServiceImpl implements SecureBiometricInterfaceService {

	private static final Logger LOGGER = PMSLogger.getLogger(SecureBiometricInterfaceServiceImpl.class);
	public static final String BLANK_STRING = "";
	public static final String VERSION = "1.0";
	public static final String DEVICE_PROVIDER = "Device_Provider";
	public static final String APPROVED = "approved";
	public static final String REJECTED = "rejected";
	public static final String PENDING_APPROVAL = "pending_approval";

	@Value("${mosip.pms.api.id.sbi.devices.get}")
	private  String getSbiDevicesId;

	@Value("${mosip.pms.api.id.deactivate.sbi.patch}")
	private  String patchDeactivateSbi;

	@Value("${mosip.pms.api.id.all.sbi.details.get}")
	private  String getAllSbiDetails;

	@Value("${mosip.pms.api.id.sbi.details.get}")
	private  String getSbiDetailsId;

	@Value("${mosip.pms.api.id.add.device.to.sbi.id.post}")
	private  String postAddDeviceToSbi;

	@Autowired
	DeviceDetailRepository deviceDetailRepository;

	@Autowired
	RegistrationDeviceSubTypeRepository registrationDeviceSubTypeRepository;

	@Autowired
	AuditUtil auditUtil;

	@Autowired
	SecureBiometricInterfaceRepository sbiRepository;

	@Autowired
	SecureBiometricInterfaceHistoryRepository sbiHistoryRepository;

	@Autowired
	SearchHelper searchHelper;

	@Autowired
	private PageUtils pageUtils;
	
	@Autowired
	PartnerServiceRepository partnerRepository;
	
	@Autowired
	DeviceDetailSbiRepository deviceDetailSbiRepository;

	@Autowired
	SbiSummaryRepository sbiSummaryRepository;

	@Autowired
	FilterColumnValidator filterColumnValidator;

	@Autowired
	FilterHelper filterHelper;

	@Autowired
	PartnerHelper partnerHelper;

	@Value("${mosip.pms.expiry.date.max.year}")
	private int maxAllowedExpiryYear;

	@Value("${mosip.pms.created.date.max.year}")
	private int maxAllowedCreatedYear;

	
	@Override
	public IdDto createSecureBiometricInterface(SecureBiometricInterfaceCreateDto sbiDto) {
		SecureBiometricInterface sbi = null;
		SecureBiometricInterface entity = new SecureBiometricInterface();
		IdDto dto = new IdDto();
		validateDates(sbiDto.getSwCreateDateTime(),sbiDto.getSwExpiryDateTime());
		Partner partner = partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(
				sbiDto.getProviderId());
		if (partner == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorCode(),
							DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorMessage()),
					"AUT-003", sbiDto.getProviderId(), "partnerId");
			throw new RequestException(DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorCode(),
					DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorMessage());
		}		
		List<SecureBiometricInterface> existsRecordsFromDb = sbiRepository.findByProviderIdAndSwVersion(sbiDto.getProviderId(), PartnerUtil.trimAndReplace(sbiDto.getSwVersion()));
		if(existsRecordsFromDb.size() > 0) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorCode(),
							SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorMessage()),
					"AUT-005", sbiDto.getProviderId(), "partnerId");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorCode(),
					SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorMessage());
		}
		entity.setProviderId(partner.getId());
		entity.setPartnerOrgName(partner.getName());
		String id = UUID.randomUUID().toString();
		entity.setId(id);
		byte[] swNinaryHashArr = sbiDto.getSwBinaryHash().getBytes();
		entity.setSwBinaryHash(swNinaryHashArr);
		entity.setActive(false);
		entity.setDeleted(false);
		entity = getCreateMapping(entity, sbiDto);
		sbi = sbiRepository.save(entity);
		dto.setId(sbi.getId());
		SecureBiometricInterfaceHistory history = new SecureBiometricInterfaceHistory();
		history = getCreateHistoryMapping(history, sbi);
		sbiHistoryRepository.save(history);
		return dto;
	}

	private SecureBiometricInterface getCreateMapping(SecureBiometricInterface entity,
			SecureBiometricInterfaceCreateDto dto) {
		entity.setActive(false);
		entity.setDeleted(false);
		entity.setApprovalStatus(CommonConstant.PENDING_APPROVAL);
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			entity.setCrBy(authN.getName());
		}
		entity.setCrDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		entity.setSwVersion(dto.getSwVersion());
		entity.setSwCreateDateTime(dto.getSwCreateDateTime());
		entity.setSwExpiryDateTime(dto.getSwExpiryDateTime());

		return entity;

	}

	private SecureBiometricInterfaceHistory getCreateHistoryMapping(SecureBiometricInterfaceHistory historyEntity,
			SecureBiometricInterface entity) {
		historyEntity.setId(entity.getId());
		historyEntity.setActive(entity.isActive());
		historyEntity.setDeleted(entity.isDeleted() == null ? false: entity.isDeleted());
		historyEntity.setApprovalStatus(entity.getApprovalStatus());
		historyEntity.setCrBy(entity.getCrBy());
		historyEntity.setEffectDateTime(entity.getCrDtimes());
		historyEntity.setCrDtimes(entity.getCrDtimes());
		historyEntity.setSwVersion(entity.getSwVersion());
		historyEntity.setSwCreateDateTime(entity.getSwCreateDateTime());
		historyEntity.setSwExpiryDateTime(entity.getSwExpiryDateTime());
		historyEntity.setSwBinaryHAsh(entity.getSwBinaryHash());
		historyEntity.setProviderId(entity.getProviderId());
		historyEntity.setPartnerOrgName(entity.getPartnerOrgName());
		return historyEntity;

	}

	@Override
	public IdDto updateSecureBiometricInterface(SecureBiometricInterfaceUpdateDto sbiupdateDto) {
		SecureBiometricInterface sbi = null;
		SecureBiometricInterface entity = new SecureBiometricInterface();
		IdDto dto = new IdDto();
		validateDates(sbiupdateDto.getSwCreateDateTime(), sbiupdateDto.getSwExpiryDateTime());
		entity = sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(sbiupdateDto.getId());
		if (entity == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(), String.format(
									SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), dto.getId())),
					"AUT-016", sbiupdateDto.getId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), dto.getId()));
		}		
		List<SecureBiometricInterface> existsRecordsFromDb = sbiRepository.findByProviderIdAndSwVersion(entity.getProviderId(), sbiupdateDto.getSwVersion());
		if(!existsRecordsFromDb.isEmpty() && !existsRecordsFromDb.stream().anyMatch(s->s.getId().equals(sbiupdateDto.getId()))) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorCode(),
							SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorMessage()),
					"AUT-005", sbiupdateDto.getId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorCode(),
					SecureBiometricInterfaceConstant.SBI_RECORDS_EXISTS.getErrorMessage());
		}
		entity.setId(sbiupdateDto.getId());
		byte[] swNinaryHashArr = sbiupdateDto.getSwBinaryHash().getBytes();
		entity.setSwBinaryHash(swNinaryHashArr);		
		entity = getUpdateMapping(entity, sbiupdateDto);
		sbi = sbiRepository.save(entity);
		dto.setId(sbi.getId());
		SecureBiometricInterfaceHistory history = new SecureBiometricInterfaceHistory();
		history = getUpdateHistoryMapping(history, sbi);
		sbiHistoryRepository.save(history);
		return dto;
	}

	private SecureBiometricInterface getUpdateMapping(SecureBiometricInterface entity,
			SecureBiometricInterfaceUpdateDto dto) {
		entity.setActive(dto.getIsActive());
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			entity.setUpdBy(authN.getName());
		}
		entity.setDeleted(false);
		entity.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		entity.setSwVersion(dto.getSwVersion());
		entity.setSwCreateDateTime(dto.getSwCreateDateTime());
		entity.setSwExpiryDateTime(dto.getSwExpiryDateTime());
		return entity;
	}

	private SecureBiometricInterfaceHistory getUpdateHistoryMapping(SecureBiometricInterfaceHistory historyEntity,
			SecureBiometricInterface entity) {
		historyEntity.setId(entity.getId());
		historyEntity.setActive(entity.isActive());
		historyEntity.setApprovalStatus(entity.getApprovalStatus());
		historyEntity.setCrBy(entity.getUpdBy());
		historyEntity.setEffectDateTime(entity.getUpdDtimes());
		historyEntity.setCrDtimes(entity.getUpdDtimes());
		historyEntity.setSwVersion(entity.getSwVersion());
		historyEntity.setSwCreateDateTime(entity.getSwCreateDateTime());
		historyEntity.setSwExpiryDateTime(entity.getSwExpiryDateTime());
		historyEntity.setSwBinaryHAsh(entity.getSwBinaryHash());
		historyEntity.setProviderId(entity.getProviderId());
		historyEntity.setPartnerOrgName(entity.getPartnerOrgName());
		return historyEntity;
	}

	@Override
	public String updateSecureBiometricInterfaceStatus(
			SecureBiometricInterfaceStatusUpdateDto secureBiometricInterfaceDto) {
		SecureBiometricInterface entity = sbiRepository
				.findByIdAndIsDeletedFalseOrIsDeletedIsNull(secureBiometricInterfaceDto.getId());
		if (entity == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(),
							String.format(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(),
									secureBiometricInterfaceDto.getId())),
					"AUT-016", secureBiometricInterfaceDto.getId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(),
							secureBiometricInterfaceDto.getId()));
		}
		if (entity.getApprovalStatus().equals(DeviceConstant.APPROVED) && entity.isActive()) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_ALREADY_APPROVED.getErrorCode(),
							String.format(SecureBiometricInterfaceConstant.SBI_ALREADY_APPROVED.getErrorMessage(),
									secureBiometricInterfaceDto.getId())),
					"AUT-016", secureBiometricInterfaceDto.getId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_ALREADY_APPROVED.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_ALREADY_APPROVED.getErrorMessage(),
							secureBiometricInterfaceDto.getId()));
		}
		if (entity.getApprovalStatus().equals(DeviceConstant.REJECTED)) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_ALREADY_REJECTED.getErrorCode(),
							String.format(SecureBiometricInterfaceConstant.SBI_ALREADY_REJECTED.getErrorMessage(),
									secureBiometricInterfaceDto.getId())),
					"AUT-016", secureBiometricInterfaceDto.getId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_ALREADY_REJECTED.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_ALREADY_REJECTED.getErrorMessage(),
							secureBiometricInterfaceDto.getId()));
		}

		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			entity.setUpdBy(authN.getName());
			entity.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		}

		if (secureBiometricInterfaceDto.getApprovalStatus().equals(DeviceConstant.APPROVE)) {
			entity.setApprovalStatus(CommonConstant.APPROVED);
			entity.setActive(true);
			SecureBiometricInterfaceHistory history = new SecureBiometricInterfaceHistory();
			history = getUpdateHistoryMapping(history, entity);
			sbiHistoryRepository.save(history);
			sbiRepository.save(entity);
			return "Secure biometric details approved successfully.";
		}
		if (secureBiometricInterfaceDto.getApprovalStatus().equals(DeviceConstant.REJECT)) {
			entity.setApprovalStatus(CommonConstant.REJECTED);
			entity.setActive(false);
			SecureBiometricInterfaceHistory history = new SecureBiometricInterfaceHistory();
			history = getUpdateHistoryMapping(history, entity);
			sbiHistoryRepository.save(history);
			sbiRepository.save(entity);
			return "Secure biometric details rejected successfully.";
		}

		auditUtil.auditRequest(
				String.format(DeviceConstant.STATUS_UPDATE_FAILURE, DeviceDetail.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.FAILURE_DESC,
						SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorCode(),
						SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorMessage()),
				"AUT-008", secureBiometricInterfaceDto.getId(), "sbiId");
		throw new RequestException(SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorCode(),
				String.format(SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorMessage(),
						secureBiometricInterfaceDto.getId()));
	}
	
	@Override
	public <E> PageResponseDto<SbiSearchResponseDto> searchSecureBiometricInterface(Class<E> entity,
			DeviceSearchDto dto) {
		List<SbiSearchResponseDto> sbis = new ArrayList<>();
		PageResponseDto<SbiSearchResponseDto> pageDto = new PageResponseDto<>();		
		Page<SecureBiometricInterface> page = searchHelper.search(SecureBiometricInterface.class, dto, "providerId");
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			 sbis=mapSbiResponse(page.getContent());
			 pageDto = pageUtils.sortPage(sbis, dto.getSort(), dto.getPagination(),page.getTotalElements());
		}
		return pageDto;
	}	

	/**
	 * 
	 * @param sbiDetails
	 * @return
	 */
	private List<SbiSearchResponseDto> mapSbiResponse(List<SecureBiometricInterface> sbiDetails){
		List<SbiSearchResponseDto> response = new ArrayList<>();
		sbiDetails.forEach(sbi->{
			SbiSearchResponseDto dto = new SbiSearchResponseDto();				
			dto.setCrBy(sbi.getCrBy());
			dto.setCrDtimes(sbi.getCrDtimes());
			dto.setDelDtimes(sbi.getDelDtimes());
			dto.setUpdBy(sbi.getUpdBy());
			dto.setUpdDtimes(sbi.getUpdDtimes());
			dto.setIsActive(sbi.isActive());
			dto.setDeleted(sbi.isDeleted() == null ? false : sbi.isDeleted());
			dto.setApprovalStatus(sbi.getApprovalStatus());
			dto.setProviderId(sbi.getProviderId());
			dto.setPartnerOrganizationName(sbi.getPartnerOrgName());
			dto.setId(sbi.getId());
			dto.setSwBinaryHash(sbi.getSwBinaryHash());
			dto.setSwCreateDateTime(sbi.getSwCreateDateTime());
			dto.setSwExpiryDateTime(sbi.getSwExpiryDateTime());
			dto.setSwVersion(sbi.getSwVersion());
			response.add(dto);
		});
		return response;
	}	
	
	/**
	 * Validates the 2 dates
	 * @param fromDate
	 * @param toDate
	 */
	private void validateDates(LocalDateTime fromDate, LocalDateTime toDate) {
		// Check if fromDate is in the future
		if (fromDate.toLocalDate().isAfter(LocalDate.now())) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_PAST_OR_TODAY.getErrorCode(),
							SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_PAST_OR_TODAY
									.getErrorMessage()),
					"AUT-015");
			throw new RequestException(
					SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_PAST_OR_TODAY.getErrorCode(),
					SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_PAST_OR_TODAY.getErrorMessage());
		}
		// Check if toDate is before or on today's date
		if (toDate.toLocalDate().isBefore(LocalDate.now()) || toDate.toLocalDate().isEqual(LocalDate.now())) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_BE_GREATERTHAN_TODAYSDATE.getErrorCode(),
							SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_BE_GREATERTHAN_TODAYSDATE
									.getErrorMessage()),
					"AUT-015");
			throw new RequestException(
					SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_BE_GREATERTHAN_TODAYSDATE.getErrorCode(),
					SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_BE_GREATERTHAN_TODAYSDATE.getErrorMessage());
		}
		// Check if fromDate is less than 10 years from today
		LocalDate maxCreatedYear = LocalDate.now().minusYears(maxAllowedCreatedYear);
		if (fromDate.toLocalDate().isBefore(maxCreatedYear)) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.CREATEDDATE_SHOULD_NOT_BE_LESS_THAN_TEN_YEARS.getErrorCode(),
							SecureBiometricInterfaceConstant.CREATEDDATE_SHOULD_NOT_BE_LESS_THAN_TEN_YEARS.getErrorMessage(), maxAllowedCreatedYear),
					"AUT-015");
			throw new RequestException(
					SecureBiometricInterfaceConstant.CREATEDDATE_SHOULD_NOT_BE_LESS_THAN_TEN_YEARS.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.CREATEDDATE_SHOULD_NOT_BE_LESS_THAN_TEN_YEARS.getErrorMessage(), maxAllowedCreatedYear));
		}
		// Check if toDate is more than 10 years from today
		LocalDate maxExpiryYear = LocalDate.now().plusYears(maxAllowedExpiryYear);
		if (toDate.toLocalDate().isAfter(maxExpiryYear)) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorCode(),
							SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorMessage(), maxAllowedExpiryYear),
					"AUT-015");
			throw new RequestException(
					SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorMessage(), maxAllowedExpiryYear));
		}
	}

	@Override
	public String mapDeviceDetailAndSbi(DeviceDetailSBIMappingDto input) {
		DeviceDetailSBI deviceDetailFromDb = deviceDetailSbiRepository.findByDeviceDetailAndSbi(input.getDeviceDetailId(), input.getSbiId());
		if(deviceDetailFromDb != null) {
			return "Mapping already exists";			
		}
		DeviceDetail validDeviceDetail = deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(input.getDeviceDetailId());
		if (validDeviceDetail == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, DeviceDetail.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorCode(),
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorMessage()),
					"AUT-008", input.getSbiId(), "sbiId");
			throw new RequestException(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorCode(), String
					.format(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorMessage(), input.getDeviceDetailId()));
		}
		if(!validDeviceDetail.getIsActive() && validDeviceDetail.getApprovalStatus().equalsIgnoreCase(CommonConstant.REJECTED)) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, DeviceDetail.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_REJECTED.getErrorCode(),
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_REJECTED.getErrorMessage()),
					"AUT-008", input.getSbiId(), "sbiId");
			throw new RequestException(DeviceDetailExceptionsConstant.DEVICE_DETAIL_REJECTED.getErrorCode(), String
					.format(DeviceDetailExceptionsConstant.DEVICE_DETAIL_REJECTED.getErrorMessage(), input.getDeviceDetailId()));
		}
		if(!validDeviceDetail.getIsActive()) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, DeviceDetail.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_APPROVED.getErrorCode(),
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_APPROVED.getErrorMessage()),
					"AUT-008", input.getSbiId(), "sbiId");
			throw new RequestException(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_APPROVED.getErrorCode(), String
					.format(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_APPROVED.getErrorMessage(), input.getDeviceDetailId()));
		}
		SecureBiometricInterface validSbi = sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(input.getSbiId());
		if (validSbi == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(), String.format(
									SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), input.getSbiId())),
					"AUT-016", input.getSbiId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), input.getSbiId()));
		}		
		if (!validSbi.isActive()) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(), String.format(
									SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), input.getSbiId())),
					"AUT-016", input.getSbiId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_NOT_APPROVED.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_NOT_APPROVED.getErrorMessage(), input.getSbiId()));
		}
		if (validSbi.getSwExpiryDateTime().toLocalDate().isBefore(LocalDate.now())) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(), String.format(
									SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), input.getSbiId())),
					"AUT-016", input.getSbiId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_EXPIRED.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_EXPIRED.getErrorMessage(), input.getSbiId()));
		}
		if(!validDeviceDetail.getDeviceProviderId().equals(validSbi.getProviderId())) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.DD_SBI_PROVIDER_NOT_MATCHING.getErrorCode(), String.format(
									SecureBiometricInterfaceConstant.DD_SBI_PROVIDER_NOT_MATCHING.getErrorMessage(), input.getSbiId())),
					"AUT-016", input.getSbiId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.DD_SBI_PROVIDER_NOT_MATCHING.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.DD_SBI_PROVIDER_NOT_MATCHING.getErrorMessage(), input.getSbiId()));			
		}
		
		DeviceDetailSBI deviceDetailSbiMapping = new DeviceDetailSBI();
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if(authN !=null) {
			deviceDetailSbiMapping.setCrBy(authN.getName());
		}
		deviceDetailSbiMapping.setProviderId(validSbi.getProviderId());
		deviceDetailSbiMapping.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		DeviceDetailSBIPK deviceDetailSBIKey = new DeviceDetailSBIPK();
		deviceDetailSBIKey.setDeviceDetailId(input.getDeviceDetailId());
		deviceDetailSBIKey.setSbiId(validSbi.getId());
		deviceDetailSbiMapping.setId(deviceDetailSBIKey);
		deviceDetailSbiMapping.setIsActive(true);
		deviceDetailSbiMapping.setIsDeleted(false);
		deviceDetailSbiMapping.setPartnerName(validSbi.getPartnerOrgName());
		deviceDetailSbiRepository.save(deviceDetailSbiMapping);
		return "Success";
	}

	@Override
	public String deleteDeviceDetailAndSbiMapping(DeviceDetailSBIMappingDto input) {
		DeviceDetailSBI deviceDetailFromDb = deviceDetailSbiRepository.findByDeviceDetailAndSbi(input.getDeviceDetailId(), input.getSbiId());
		if(deviceDetailFromDb == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.DD_SBI_MAPPING_NOT_EXISTS.getErrorCode(), String.format(
									SecureBiometricInterfaceConstant.DD_SBI_MAPPING_NOT_EXISTS.getErrorMessage(), input.getSbiId())),
					"AUT-016", input.getSbiId(), "sbiId");
			throw new RequestException(SecureBiometricInterfaceConstant.DD_SBI_MAPPING_NOT_EXISTS.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.DD_SBI_MAPPING_NOT_EXISTS.getErrorMessage(), input.getSbiId()));			
			
		}
		deviceDetailSbiRepository.delete(deviceDetailFromDb);
		return "Success";
	}
	
	@Override
	public <E> PageResponseDto<MappedDeviceDetailsReponse> searchMappedDeviceDetails(Class<E> entity, DeviceSearchDto dto) {		
		PageResponseDto<MappedDeviceDetailsReponse> pageDto = new PageResponseDto<>();	
		List<MappedDeviceDetailsReponse> mappedRecords = new ArrayList<>();
		Optional<SearchFilter> deviceDetailSearchFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("deviceDetailId")).findFirst();		
		dto.getFilters().removeIf(f->f.getColumnName().equalsIgnoreCase("deviceDetailId"));		
		Page<DeviceDetailSBI> page = searchHelper.search(DeviceDetailSBI.class, dto, "providerId");
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			if(deviceDetailSearchFilter.isPresent()) {
				String idValue = deviceDetailSearchFilter.get().getValue();
				mappedRecords = mapMappedDeviceDetailsResponse(page.getContent().stream().filter(
						f -> f.getId().getDeviceDetailId().equals(idValue))
						.collect(Collectors.toList()));

			}else {
				mappedRecords=mapMappedDeviceDetailsResponse(page.getContent());
			}
			 pageDto = pageUtils.sortPage(mappedRecords, dto.getSort(), dto.getPagination(),page.getTotalElements());
		}
		return pageDto;
	}
	
	private List<MappedDeviceDetailsReponse> mapMappedDeviceDetailsResponse(List<DeviceDetailSBI> mappedDeviceDetails) {
		List<MappedDeviceDetailsReponse> response = new ArrayList<>();
		List<DeviceDetail> allDeviceDetails = deviceDetailRepository.findAll();
		List<SecureBiometricInterface> allSBIs = sbiRepository.findAll();
		mappedDeviceDetails.forEach(sbi->{
			MappedDeviceDetailsReponse output = new MappedDeviceDetailsReponse();
			DeviceDetail deviceDetail = allDeviceDetails.stream().filter(f->f.getId().equals(sbi.getId().getDeviceDetailId())).findFirst().get();
			SecureBiometricInterface secureBioInterface = allSBIs.stream().filter(f->f.getId().equals(sbi.getId().getSbiId())).findFirst().get();
			output.setCrBy(sbi.getCrBy());
			output.setCrDtimes(sbi.getCrDtimes());			
			output.setDeviceDetailId(sbi.getId().getDeviceDetailId());
			output.setDeviceSubTypeCode(deviceDetail.getDeviceSubTypeCode());
			output.setDeviceTypeCode(deviceDetail.getDeviceTypeCode());
			output.setMake(deviceDetail.getMake());
			output.setModel(deviceDetail.getModel());
			output.setProviderId(sbi.getProviderId());
			output.setProviderName(sbi.getPartnerName());
			output.setSbiId(sbi.getId().getSbiId());
			output.setSwBinaryHash(secureBioInterface.getSwBinaryHash());
			output.setSwVersion(secureBioInterface.getSwVersion());			
			response.add(output);
		});
		return response;
	}

	@Override
	public FilterResponseCodeDto filterValues(FilterValueDto filterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), SecureBiometricInterface.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(SecureBiometricInterface.class,
						filterDto, filterValueDto, "id");
				filterValues.forEach(filterValue -> {
					ColumnCodeValue columnValue = new ColumnCodeValue();
					columnValue.setFieldCode(filterValue.getFieldCode());
					columnValue.setFieldID(filterDto.getColumnName().split(",")[0]);
					columnValue.setFieldValue(filterValue.getFieldValue());
					columnValueList.add(columnValue);
				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		return filterResponseDto;
	}

	@Override
	public ResponseWrapperV2<IdDto> addDeviceToSbi(DeviceDetailDto deviceDetailDto, String sbiId) {
		ResponseWrapperV2<IdDto> responseWrapper = new ResponseWrapperV2<>();
		String deviceId = null;
		try {
			String partnerId = deviceDetailDto.getDeviceProviderId();
			String userId = getUserId();
			List<Partner> partnerList = partnerRepository.findByUserId(userId);
			if (partnerList.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
			// check if partnerId is associated with user
			boolean partnerIdExists = false;
			String partnerOrgname = BLANK_STRING;
			for (Partner partner : partnerList) {
				if (partner.getId().equals(partnerId)) {
					partnerHelper.validatePartnerId(partner, userId);
					partnerIdExists = true;
					partnerOrgname = partner.getName();
					break;
				}
			}
			if (!partnerIdExists) {
				LOGGER.info("sessionId", "idType", "id", "Partner id is not associated with user.");
				throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
						ErrorCode.PARTNER_ID_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
			}
			IdDto dto = createDeviceDetails(deviceDetailDto);
			deviceId = dto.getId();
			addInactiveMappingDeviceToSbi(sbiId, deviceId, partnerId, partnerOrgname, userId);
			responseWrapper.setResponse(dto);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In addDeviceToSbi method of SecureBiometricInterfaceServiceImpl - " + ex.getMessage());
			if (Objects.nonNull(deviceId) && !deviceId.equals(BLANK_STRING)) {
				deleteDeviceDetail(deviceId);
			}
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In addDeviceToSbi method of SecureBiometricInterfaceServiceImpl - " + ex.getMessage());
			if (Objects.nonNull(deviceId) && !deviceId.equals(BLANK_STRING)) {
				deleteDeviceDetail(deviceId);
			}
			String errorCode = ErrorCode.CREATE_DEVICE_ERROR.getErrorCode();
			String errorMessage = ErrorCode.CREATE_DEVICE_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(postAddDeviceToSbi);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	private IdDto createDeviceDetails(DeviceDetailDto deviceDetailDto) {
		DeviceDetail entity = new DeviceDetail();
		DeviceDetail deviceDetail = null;
		IdDto dto = new IdDto();
		RegistrationDeviceSubType registrationDeviceSubType = registrationDeviceSubTypeRepository
				.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(
						deviceDetailDto.getDeviceSubTypeCode(), deviceDetailDto.getDeviceTypeCode());
		if (registrationDeviceSubType == null) {
			throw new PartnerServiceException(DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorCode(),
					DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorMessage());
		} else {
			entity.setDeviceSubTypeCode(registrationDeviceSubType.getCode());
			entity.setDeviceTypeCode(registrationDeviceSubType.getDeviceTypeCode());
		}
		Partner partner = partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(
				deviceDetailDto.getDeviceProviderId());
		if (partner == null) {
			throw new PartnerServiceException(DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorCode(),
					DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorMessage());
		}
		entity.setPartnerOrganizationName(partner.getName());
		if (deviceDetailRepository.findUniqueDeviceDetail(PartnerUtil.trimAndReplace(deviceDetailDto.getMake()), PartnerUtil.trimAndReplace(deviceDetailDto.getModel()),
				deviceDetailDto.getDeviceProviderId(), deviceDetailDto.getDeviceSubTypeCode(),
				deviceDetailDto.getDeviceTypeCode()) != null) {
			throw new PartnerServiceException(DeviceDetailExceptionsConstant.DEVICE_DETAIL_EXIST.getErrorCode(),
					DeviceDetailExceptionsConstant.DEVICE_DETAIL_EXIST.getErrorMessage());
		}
		entity = getCreateMapping(entity, deviceDetailDto);
		deviceDetail = deviceDetailRepository.save(entity);
		dto.setId(deviceDetail.getId());
		return dto;
	}

	public DeviceDetail getCreateMapping(DeviceDetail deviceDetail, DeviceDetailDto deviceDetailDto) {
		deviceDetail.setId(deviceDetailDto.getId() == null ? DeviceUtil.generateId(): deviceDetailDto.getId());
		deviceDetail.setIsActive(false);
		deviceDetail.setIsDeleted(false);
		deviceDetail.setApprovalStatus(CommonConstant.PENDING_APPROVAL);
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			deviceDetail.setCrBy(authN.getName());
		}
		deviceDetail.setCrDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		deviceDetail.setDeviceProviderId(deviceDetailDto.getDeviceProviderId());
		deviceDetail.setMake(deviceDetailDto.getMake());
		deviceDetail.setModel(deviceDetailDto.getModel());
		return deviceDetail;

	}

	private void addInactiveMappingDeviceToSbi(String sbiId, String deviceId, String partnerId, String orgName, String userId) {
		DeviceDetailSBI deviceDetailSBI = deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(partnerId, sbiId, deviceId);
		if (Objects.nonNull(deviceDetailSBI)){
			LOGGER.info("sessionId", "idType", "id", "SBI and Device mapping already exists in DB.");
			throw new PartnerServiceException(ErrorCode.SBI_DEVICE_MAPPING_ALREADY_EXIST.getErrorCode(),
					ErrorCode.SBI_DEVICE_MAPPING_ALREADY_EXIST.getErrorMessage());
		}

		// validate sbi and device mapping
		partnerHelper.validateSbiDeviceMapping(partnerId, sbiId, deviceId);

		DeviceDetailSBI entity = new DeviceDetailSBI();

		DeviceDetailSBIPK pk = new DeviceDetailSBIPK();
		pk.setSbiId(sbiId);
		pk.setDeviceDetailId(deviceId);

		entity.setId(pk);
		entity.setProviderId(partnerId);
		entity.setPartnerName(orgName);
		entity.setIsActive(false);
		entity.setIsDeleted(false);
		entity.setCrBy(userId);
		entity.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));

		DeviceDetailSBI savedEntity = deviceDetailSbiRepository.save(entity);
		LOGGER.info("sessionId", "idType", "id", "saved inactive device mapping to sbi successfully in Db.");
	}

	private void deleteDeviceDetail(String deviceDetailId) {
		try {
			Optional<DeviceDetail> deviceDetail = deviceDetailRepository.findById(deviceDetailId);
			if (deviceDetail.isPresent()) {
				List<DeviceDetailSBI> deviceDetailSBIList = deviceDetailSbiRepository.findByDeviceDetailId(deviceDetailId);
				if (deviceDetailSBIList.isEmpty()) {
					deviceDetailRepository.deleteById(deviceDetailId);
					LOGGER.info("sessionId", "idType", "id", "Device detail with id " + deviceDetailId + " deleted successfully.");
				}
			}
		} catch (Exception e) {
			LOGGER.error("sessionId", "idType", "id", "Error while deleting device detail with id " + deviceDetailId + ": " + e.getMessage());
		}
	}

	@Override
	public ResponseWrapperV2<List<DeviceDto>> getAllDevicesForSbi(String sbiId) {
		ResponseWrapperV2<List<DeviceDto>> responseWrapper = new ResponseWrapperV2<>();
		try {
			String userId = getUserId();
			List<Partner> partnerList = partnerRepository.findByUserId(userId);

			if (partnerList.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}

			Optional<SecureBiometricInterface> secureBiometricInterface = sbiRepository.findById(sbiId);

			if (secureBiometricInterface.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "Sbi is not associated with partner Id.");
				throw new PartnerServiceException(ErrorCode.SBI_NOT_EXISTS.getErrorCode(),
						ErrorCode.SBI_NOT_EXISTS.getErrorMessage());
			}

			SecureBiometricInterface sbi = secureBiometricInterface.get();
			// check if partnerId is associated with user
			boolean partnerIdExists = false;
			for (Partner partner : partnerList) {
				if (partner.getId().equals(sbi.getProviderId())) {
					partnerHelper.validatePartnerId(partner, userId);
					validateDevicePartnerType(partner, userId);
					partnerIdExists = true;
					break;
				}
			}
			if (!partnerIdExists) {
				LOGGER.info("sessionId", "idType", "id", "Partner id is not associated with user.");
				throw new PartnerServiceException(ErrorCode.SBI_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
						ErrorCode.SBI_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
			}
			// fetch devices list
			List<DeviceDetailSBI> deviceDetailSBIList = deviceDetailSbiRepository.findByDeviceProviderIdAndSbiId(sbi.getProviderId(), sbiId);
			if (!deviceDetailSBIList.isEmpty()) {
				List<DeviceDto> deviceDtoList = new ArrayList<>();
				for (DeviceDetailSBI deviceDetailSBI : deviceDetailSBIList) {
					Optional<DeviceDetail> optionalDeviceDetail = deviceDetailRepository.
							findByIdAndDeviceProviderId(deviceDetailSBI.getId().getDeviceDetailId(), deviceDetailSBI.getProviderId());
					if (optionalDeviceDetail.isPresent()) {
						DeviceDetail deviceDetail = optionalDeviceDetail.get();
						DeviceDto deviceDto = new DeviceDto();
						deviceDto.setDeviceId(deviceDetail.getId());
						deviceDto.setDeviceTypeCode(deviceDetail.getDeviceTypeCode());
						deviceDto.setDeviceSubTypeCode(deviceDetail.getDeviceSubTypeCode());
						deviceDto.setDeviceProviderId(deviceDetail.getDeviceProviderId());
						deviceDto.setMake(deviceDetail.getMake());
						deviceDto.setModel(deviceDetail.getModel());
						deviceDto.setStatus(deviceDetail.getApprovalStatus());
						deviceDto.setActive(deviceDetail.getIsActive());
						deviceDto.setCreatedDateTime(deviceDetail.getCrDtimes());

						deviceDtoList.add(deviceDto);
					}
				}
				responseWrapper.setResponse(deviceDtoList);
			}
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getAllDevicesForSbi method of SecureBiometricInterfaceServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getAllDevicesForSbi method of SecureBiometricInterfaceServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.DEVICES_LIST_FOR_SBI_FETCH_ERROR.getErrorCode();
			String errorMessage = ErrorCode.DEVICES_LIST_FOR_SBI_FETCH_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getSbiDevicesId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	private void validateDevicePartnerType(Partner partner, String userId) {
		if (!partner.getPartnerTypeCode().equals(DEVICE_PROVIDER)) {
			LOGGER.info("Invalid Partner type for partner id : " + partner.getId());
			throw new PartnerServiceException(ErrorCode.INVALID_DEVICE_PARTNER_TYPE.getErrorCode(),
					ErrorCode.INVALID_DEVICE_PARTNER_TYPE.getErrorMessage());
		}
	}

	@Override
	public ResponseWrapperV2<SbiDetailsResponseDto> deactivateSbi(String sbiId, DeactivateSbiRequestDto requestDto) {
		ResponseWrapperV2<SbiDetailsResponseDto> responseWrapper = new ResponseWrapperV2<>();
		try {
			String status = requestDto.getStatus();
			if (Objects.isNull(status) || status.equals(BLANK_STRING) || !status.equals(PartnerConstants.DEACTIVATE)) {
				LOGGER.info(status + " : is Invalid Input Parameter, it should be (De-Activate)");
				throw new PartnerServiceException(ErrorCode.DEACTIVATE_STATUS_CODE.getErrorCode(),
						ErrorCode.DEACTIVATE_STATUS_CODE.getErrorMessage());
			}
			String userId = getUserId();
			List<Partner> partnerList = partnerRepository.findByUserId(userId);
			if (partnerList.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}

			if (Objects.isNull(sbiId)) {
				LOGGER.info("sessionId", "idType", "id", "SBI id is null.");
				throw new PartnerServiceException(ErrorCode.INVALID_SBI_ID.getErrorCode(),
						ErrorCode.INVALID_SBI_ID.getErrorMessage());
			}
			Optional<SecureBiometricInterface> secureBiometricInterface = sbiRepository.findById(sbiId);
			if (!secureBiometricInterface.isPresent()) {
				LOGGER.error("SBI not exists with id {}", sbiId);
				throw new PartnerServiceException(ErrorCode.SBI_NOT_EXISTS.getErrorCode(),
						ErrorCode.SBI_NOT_EXISTS.getErrorMessage());
			}
			SecureBiometricInterface sbi = secureBiometricInterface.get();

			boolean isAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
			if (!isAdmin) {
				Partner partnerDetails = getAssociatedPartner(partnerList, sbi.getProviderId(), userId);
				partnerHelper.checkIfPartnerIsNotActive(partnerDetails);
			}
			if (!sbi.getApprovalStatus().equals(APPROVED)) {
				LOGGER.error("Unable to deactivate sbi with id {}", sbi.getId());
				throw new PartnerServiceException(ErrorCode.SBI_NOT_APPROVED.getErrorCode(),
						ErrorCode.SBI_NOT_APPROVED.getErrorMessage());
			}
			if (sbi.getApprovalStatus().equals(APPROVED) && !sbi.isActive()) {
				LOGGER.error("Unable to deactivate sbi with id {}", sbi.getId());
				throw new PartnerServiceException(ErrorCode.SBI_ALREADY_DEACTIVATED.getErrorCode(),
						ErrorCode.SBI_ALREADY_DEACTIVATED.getErrorMessage());
			}
			// Deactivate approved devices
			List<DeviceDetail> approvedDevices = deviceDetailRepository.findApprovedDevicesBySbiId(sbiId);
			if (!approvedDevices.isEmpty()) {
				for (DeviceDetail deviceDetail : approvedDevices) {
					deviceDetail.setIsActive(false);
					deviceDetail.setUpdDtimes(LocalDateTime.now());
					deviceDetail.setUpdBy(getUserId());
					deviceDetailRepository.save(deviceDetail);
				}
			}
			// Reject pending_approval devices
			List<DeviceDetail> pendingApprovalDevices = deviceDetailRepository.findPendingApprovalDevicesBySbiId(sbiId);
			if (!pendingApprovalDevices.isEmpty()) {
				for (DeviceDetail deviceDetail : pendingApprovalDevices) {
					deviceDetail.setApprovalStatus(REJECTED);
					deviceDetail.setUpdDtimes(LocalDateTime.now());
					deviceDetail.setUpdBy(getUserId());
					deviceDetailRepository.save(deviceDetail);
				}
			}
			sbi.setActive(false);
			sbi.setUpdDtimes(LocalDateTime.now());
			sbi.setUpdBy(getUserId());
			SecureBiometricInterface updatedSbi = sbiRepository.save(sbi);
			SbiDetailsResponseDto sbiDetailsResponseDto = new SbiDetailsResponseDto();

			sbiDetailsResponseDto.setSbiId(updatedSbi.getId());
			sbiDetailsResponseDto.setSbiVersion(updatedSbi.getSwVersion());
			sbiDetailsResponseDto.setStatus(updatedSbi.getApprovalStatus());
			sbiDetailsResponseDto.setActive(updatedSbi.isActive());

			responseWrapper.setResponse(sbiDetailsResponseDto);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In deactivateSbi method of SecureBiometricInterfaceServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In deactivateSbi method of SecureBiometricInterfaceServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.DEACTIVATE_SBI_ERROR.getErrorCode();
			String errorMessage = ErrorCode.DEACTIVATE_SBI_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(patchDeactivateSbi);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	public Partner getAssociatedPartner (List<Partner> partnerList, String sbiProviderId, String userId) {
		boolean sbiProviderExist = false;
		Partner partnerDetails = null;
		for (Partner partner : partnerList) {
			if (partner.getId().equals(sbiProviderId)) {
				partnerHelper.validatePartnerId(partner, userId);
				sbiProviderExist = true;
				partnerDetails = partner;
				break;
			}
		}
		if (!sbiProviderExist) {
			LOGGER.info("sessionId", "idType", "id", "SBI is not associated with user.");
			throw new PartnerServiceException(ErrorCode.SBI_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
					ErrorCode.SBI_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
		}
		return partnerDetails;
	}

	@Override
	public ResponseWrapperV2<PageResponseV2Dto<SbiSummaryDto>> getAllSbiDetails(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, SbiFilterDto filterDto) {
		ResponseWrapperV2<PageResponseV2Dto<SbiSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		try {
			PageResponseV2Dto<SbiSummaryDto> pageResponseV2Dto = new PageResponseV2Dto<>();
			boolean isPartnerAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
			List<String> partnerIdList = null;
			if (!isPartnerAdmin) {
				String userId = getUserId();
				List<Partner> partnerList = partnerRepository.findByUserId(userId);
				if (partnerList.isEmpty()) {
					LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
					throw new PartnerServiceException(io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
							io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
				}
				partnerIdList = new ArrayList<>();
				for (Partner partner : partnerList) {
					partnerHelper.validatePartnerId(partner, userId);
					partnerIdList.add(partner.getId());
				}
			}

			Pageable pageable = Pageable.unpaged();

			// Pagination
			boolean isPaginationEnabled = (pageNo != null && pageSize != null);
			if (isPaginationEnabled) {
				pageable = PageRequest.of(pageNo, pageSize);
			}

			// Fetch the SBI details
			Page<SbiSummaryEntity> page = getSbiDetails(sortFieldName, sortType, pageNo, pageSize, filterDto, pageable, partnerIdList, isPartnerAdmin);
			if (Objects.nonNull(page) && !page.getContent().isEmpty()) {
				List<SbiSummaryDto> sbiSummaryDtoList = MapperUtils.mapAll(page.getContent(), SbiSummaryDto.class);
				pageResponseV2Dto.setPageNo(page.getNumber());
				pageResponseV2Dto.setPageSize(page.getSize());
				pageResponseV2Dto.setTotalResults(page.getTotalElements());
				pageResponseV2Dto.setData(sbiSummaryDtoList);
			}
			responseWrapper.setResponse(pageResponseV2Dto);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getAllSbiDetails method of SecureBiometricInterfaceServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getAllSbiDetails method of SecureBiometricInterfaceServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.SBI_DETAILS_LIST_FETCH_ERROR.getErrorCode();
			String errorMessage = ErrorCode.SBI_DETAILS_LIST_FETCH_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getAllSbiDetails);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	private Page<SbiSummaryEntity> getSbiDetails(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, SbiFilterDto filterDto, Pageable pageable, List<String> partnerIdList, boolean isPartnerAdmin) {
		// Sorting
		if (Objects.nonNull(sortFieldName) && Objects.nonNull(sortType)) {
			String sortKey = sortFieldName + "_" + sortType.toLowerCase();
			switch (sortKey) {
				case "status_asc":
					return sbiSummaryRepository.getSummaryOfSbiDetailsByStatusAsc(
							filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getSbiId(),
							filterDto.getSbiVersion(), filterDto.getStatus(),
							filterDto.getSbiExpiryStatus(), partnerIdList, isPartnerAdmin, pageable);

				case "status_desc":
					return sbiSummaryRepository.getSummaryOfSbiDetailsByStatusDesc(
							filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getSbiId(),
							filterDto.getSbiVersion(), filterDto.getStatus(),
							filterDto.getSbiExpiryStatus(), partnerIdList, isPartnerAdmin, pageable);

				case "sbiExpiryStatus_asc":
					return sbiSummaryRepository.getSummaryOfSbiDetailsByExpiryStatusAsc(
							filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getSbiId(),
							filterDto.getSbiVersion(), filterDto.getStatus(),
							filterDto.getSbiExpiryStatus(), partnerIdList, isPartnerAdmin, pageable);

				case "sbiExpiryStatus_desc":
					return sbiSummaryRepository.getSummaryOfSbiDetailsByExpiryStatusDesc(
							filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getSbiId(),
							filterDto.getSbiVersion(), filterDto.getStatus(),
							filterDto.getSbiExpiryStatus(), partnerIdList, isPartnerAdmin, pageable);

				case "countOfAssociatedDevices_asc":
					return sbiSummaryRepository.getSummaryOfSbiDetailsByDevicesCountAsc(
							filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getSbiId(),
							filterDto.getSbiVersion(), filterDto.getStatus(),
							filterDto.getSbiExpiryStatus(), partnerIdList, isPartnerAdmin, pageable);

				case "countOfAssociatedDevices_desc":
					return sbiSummaryRepository.getSummaryOfSbiDetailsByDevicesCountDesc(
							filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getSbiId(),
							filterDto.getSbiVersion(), filterDto.getStatus(),
							filterDto.getSbiExpiryStatus(), partnerIdList, isPartnerAdmin, pageable);

				default:
					// generic sorting logic for other fields
					Sort sort = partnerHelper.getSortingRequest(
							getSortColumn(partnerHelper.sbiAliasToColumnMap, sortFieldName), sortType);
					pageable = PageRequest.of(pageNo, pageSize, sort);
			}
		}
		return sbiSummaryRepository.getSummaryOfSbiDetails(
				filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getSbiId(),
				filterDto.getSbiVersion(), filterDto.getStatus(),
				filterDto.getSbiExpiryStatus(), partnerIdList, isPartnerAdmin, pageable);
	}

	public String getSortColumn(Map<String, String> aliasToColumnMap, String alias) {
		return aliasToColumnMap.getOrDefault(alias, alias); // Return alias if no match found
	}

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getUserId() {
		String userId = authUserDetails().getUserId();
		return userId;
	}
}
