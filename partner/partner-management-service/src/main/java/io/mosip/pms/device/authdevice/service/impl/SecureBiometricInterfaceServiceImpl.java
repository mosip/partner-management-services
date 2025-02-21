package io.mosip.pms.device.authdevice.service.impl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
		if (toDate.isBefore(fromDate)) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_LESSTHAN_EXPIRYDATE.getErrorCode(),
							SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_LESSTHAN_EXPIRYDATE
									.getErrorMessage()),
					"AUT-015");
			throw new RequestException(
					SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_LESSTHAN_EXPIRYDATE.getErrorCode(),
					SecureBiometricInterfaceConstant.SWCREATEDDATE_SHOULD_BE_LESSTHAN_EXPIRYDATE.getErrorMessage());
		}
		if (toDate.toLocalDate().isBefore(LocalDate.now())) {
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
}
