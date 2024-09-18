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
import java.util.stream.Collectors;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.DeviceDetailDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.device.response.dto.SbiDetailsResponseDto;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceHistoryRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.device.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.constant.DeviceDetailExceptionsConstant;
import io.mosip.pms.device.constant.SecureBiometricInterfaceConstant;
import io.mosip.pms.device.request.dto.DeviceDetailSBIMappingDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.response.dto.ColumnCodeValue;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.MappedDeviceDetailsReponse;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;
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

	@Value("${mosip.pms.api.id.sbi.devices.get}")
	private  String getSbiDevicesId;

	@Value("${mosip.pms.api.id.deactivate.sbi.post}")
	private  String postDeactivateSbi;

	@Autowired
	DeviceDetailRepository deviceDetailRepository;

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
	FilterColumnValidator filterColumnValidator;

	@Autowired
	FilterHelper filterHelper;

	@Value("${mosip.pms.expiry.date.max.year}")
	private int maxAllowedYear;

	
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
		List<SecureBiometricInterface> existsRecordsFromDb = sbiRepository.findByProviderIdAndSwVersion(sbiDto.getProviderId(), sbiDto.getSwVersion());
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
		// Check if toDate is more than 10 years from today
		LocalDate maxYear = LocalDate.now().plusYears(maxAllowedYear);
		if (toDate.toLocalDate().isAfter(maxYear)) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorCode(),
							SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorMessage(), maxAllowedYear),
					"AUT-015");
			throw new RequestException(
					SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS.getErrorMessage(), maxAllowedYear));
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
	public ResponseWrapperV2<List<DeviceDetailDto>> getAllDevicesForSbi(String sbiId) {
		ResponseWrapperV2<List<DeviceDetailDto>> responseWrapper = new ResponseWrapperV2<>();
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
					validatePartnerId(partner, userId);
					validateDevicePartnerType(partner, userId);
					partnerIdExists = true;
					break;
				}
			}
			if (!partnerIdExists) {
				LOGGER.info("sessionId", "idType", "id", "Partner id is not associated with user.");
				throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
						ErrorCode.PARTNER_ID_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
			}
			// fetch devices list
			List<DeviceDetailSBI> deviceDetailSBIList = deviceDetailSbiRepository.findByDeviceProviderIdAndSbiId(sbi.getProviderId(), sbiId);
			if (!deviceDetailSBIList.isEmpty()) {
				List<DeviceDetailDto> deviceDetailDtoList = new ArrayList<>();
				for (DeviceDetailSBI deviceDetailSBI : deviceDetailSBIList) {
					Optional<DeviceDetail> optionalDeviceDetail = deviceDetailRepository.
							findByIdAndDeviceProviderId(deviceDetailSBI.getId().getDeviceDetailId(), deviceDetailSBI.getProviderId());
					if (optionalDeviceDetail.isPresent()) {
						DeviceDetail deviceDetail = optionalDeviceDetail.get();
						DeviceDetailDto deviceDetailDto = new DeviceDetailDto();
						deviceDetailDto.setId(deviceDetail.getId());
						deviceDetailDto.setDeviceTypeCode(deviceDetail.getDeviceTypeCode());
						deviceDetailDto.setDeviceSubTypeCode(deviceDetail.getDeviceSubTypeCode());
						deviceDetailDto.setDeviceProviderId(deviceDetail.getDeviceProviderId());
						deviceDetailDto.setMake(deviceDetail.getMake());
						deviceDetailDto.setModel(deviceDetail.getModel());
						deviceDetailDto.setStatus(deviceDetail.getApprovalStatus());
						deviceDetailDto.setActive(deviceDetail.getIsActive());
						deviceDetailDto.setCreatedDateTime(deviceDetail.getCrDtimes());

						deviceDetailDtoList.add(deviceDetailDto);
					}
				}
				responseWrapper.setResponse(deviceDetailDtoList);
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

	public static void validatePartnerId(Partner partner, String userId) {
		if (Objects.isNull(partner.getId()) || partner.getId().equals(BLANK_STRING)) {
			LOGGER.info("Partner Id is null or empty for user id : " + userId);
			throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorMessage());
		}
	}

	private void validateDevicePartnerType(Partner partner, String userId) {
		if (!partner.getPartnerTypeCode().equals(DEVICE_PROVIDER)) {
			LOGGER.info("Invalid Partner type for partner id : " + partner.getId());
			throw new PartnerServiceException(ErrorCode.INVALID_DEVICE_PARTNER_TYPE.getErrorCode(),
					ErrorCode.INVALID_DEVICE_PARTNER_TYPE.getErrorMessage());
		}
	}

	@Override
	public ResponseWrapperV2<SbiDetailsResponseDto> deactivateSbi(String sbiId) {
		ResponseWrapperV2<SbiDetailsResponseDto> responseWrapper = new ResponseWrapperV2<>();
		try {
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
			// check if the SBI is associated with user.
			String sbiProviderId = sbi.getProviderId();
			boolean sbiProviderExist = false;
			Partner partnerDetails = new Partner();
			for (Partner partner : partnerList) {
				if (partner.getId().equals(sbiProviderId)) {
					validatePartnerId(partner, userId);
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
			//check if Partner is Active or not
			if (!partnerDetails.getIsActive()) {
				LOGGER.error("Partner is not Active with id {}", sbiProviderId);
				throw new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
						ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
			}
			// Deactivate only if the SBI is approved and is_active true.
			if (sbi.getApprovalStatus().equals(APPROVED) && sbi.isActive()) {
				// Deactivate approved devices
				List <DeviceDetail> approvedDevices = deviceDetailRepository.findApprovedDevicesBySbiId(sbiId);
				if (!approvedDevices.isEmpty()) {
					for (DeviceDetail deviceDetail: approvedDevices) {
						deviceDetail.setIsActive(false);
						deviceDetailRepository.save(deviceDetail);
					}
				}
				// Reject pending_approval devices
				List <DeviceDetail> pendingApprovalDevices = deviceDetailRepository.findPendingApprovalDevicesBySbiId(sbiId);
				if (!pendingApprovalDevices.isEmpty()) {
					for (DeviceDetail deviceDetail: pendingApprovalDevices) {
						deviceDetail.setApprovalStatus(REJECTED);
						deviceDetailRepository.save(deviceDetail);
					}
				}
				sbi.setActive(false);
				SecureBiometricInterface updatedSbi = sbiRepository.save(sbi);
				SbiDetailsResponseDto sbiDetailsResponseDto = new SbiDetailsResponseDto();

				sbiDetailsResponseDto.setSbiId(updatedSbi.getId());
				sbiDetailsResponseDto.setSbiVersion(updatedSbi.getSwVersion());
				sbiDetailsResponseDto.setStatus(updatedSbi.getApprovalStatus());
				sbiDetailsResponseDto.setActive(updatedSbi.isActive());

				responseWrapper.setResponse(sbiDetailsResponseDto);
			} else {
				LOGGER.error("Unable to deactivate sbi with id {}", sbi.getId());
				throw new PartnerServiceException(ErrorCode.UNABLE_TO_DEACTIVATE_SBI.getErrorCode(),
						ErrorCode.UNABLE_TO_DEACTIVATE_SBI.getErrorMessage());
			}
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
		responseWrapper.setId(postDeactivateSbi);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getUserId() {
		String userId = authUserDetails().getUserId();
		return userId;
	}
}
