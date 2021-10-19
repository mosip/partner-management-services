package io.mosip.pms.device.authdevice.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterfaceHistory;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceHistoryRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.device.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.constant.SecureBiometricInterfaceConstant;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;
import io.mosip.pms.device.util.AuditUtil;

@Component
@Transactional
public class SecureBiometricInterfaceServiceImpl implements SecureBiometricInterfaceService {

	private static final String PENDING_APPROVAL = "Pending_Approval";
	
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
	
	@Override
	public IdDto createSecureBiometricInterface(SecureBiometricInterfaceCreateDto sbiDto) {
		SecureBiometricInterface sbi = null;
		SecureBiometricInterface entity = new SecureBiometricInterface();
		IdDto dto = new IdDto();
		DeviceDetail deviceDetail = deviceDetailRepository
				.findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(sbiDto.getDeviceDetailId());
		if (deviceDetail == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorCode(),
							SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorMessage()),
					"AUT-015");
			throw new RequestException(SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorCode(),
					SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorMessage());
		} else {
			entity.setDeviceDetailId(deviceDetail.getId());
		}
		String id = UUID.randomUUID().toString();
		entity.setId(id);
		byte[] swNinaryHashArr = sbiDto.getSwBinaryHash().getBytes();
		entity.setSwBinaryHash(swNinaryHashArr);
		entity = getCreateMapping(entity, sbiDto);
		sbi = sbiRepository.save(entity);
		dto.setId(sbi.getId());
		SecureBiometricInterfaceHistory history = new SecureBiometricInterfaceHistory();
		history = getCreateHistoryMapping(history, sbi);
		history.setDeviceDetailId(sbiDto.getDeviceDetailId());
		sbiHistoryRepository.save(history);
		return dto;
	}

	private SecureBiometricInterface getCreateMapping(SecureBiometricInterface entity,
			SecureBiometricInterfaceCreateDto dto) {

		entity.setActive(false);
		entity.setDeleted(false);
		entity.setApprovalStatus(PENDING_APPROVAL);
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
		historyEntity.setDeleted(entity.isDeleted());
		historyEntity.setApprovalStatus(entity.getApprovalStatus());
		historyEntity.setCrBy(entity.getCrBy());
		historyEntity.setEffectDateTime(entity.getCrDtimes());
		historyEntity.setCrDtimes(entity.getCrDtimes());
		historyEntity.setSwVersion(entity.getSwVersion());
		historyEntity.setSwCreateDateTime(entity.getSwCreateDateTime());
		historyEntity.setSwExpiryDateTime(entity.getSwExpiryDateTime());
		historyEntity.setSwBinaryHAsh(entity.getSwBinaryHash());

		return historyEntity;

	}

	@Override
	public IdDto updateSecureBiometricInterface(SecureBiometricInterfaceUpdateDto sbiupdateDto) {
		SecureBiometricInterface sbi = null;
		SecureBiometricInterface entity = new SecureBiometricInterface();
		IdDto dto = new IdDto();
		entity = sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(sbiupdateDto.getId());
		if (entity == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(), String.format(
									SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), dto.getId())),
					"AUT-016");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), dto.getId()));
		}
		DeviceDetail deviceDetail = deviceDetailRepository
				.findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(sbiupdateDto.getDeviceDetailId());
		if (deviceDetail == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorCode(),
							String.format(SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorMessage(),
									dto.getId())),
					"AUT-018");
			throw new RequestException(SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorCode(),
					SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorMessage());
		} else {
			entity.setDeviceDetailId(deviceDetail.getId());
		}

		entity.setId(sbiupdateDto.getId());
		byte[] swNinaryHashArr = sbiupdateDto.getSwBinaryHash().getBytes();
		entity.setSwBinaryHash(swNinaryHashArr);
		entity = getUpdateMapping(entity, sbiupdateDto);
		sbi = sbiRepository.save(entity);
		dto.setId(sbi.getId());
		SecureBiometricInterfaceHistory history = new SecureBiometricInterfaceHistory();
		history = getUpdateHistoryMapping(history, sbi);
		history.setDeviceDetailId(sbiupdateDto.getDeviceDetailId());
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
		historyEntity.setDeviceDetailId(entity.getDeviceDetailId());

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
					"AUT-016");
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
			entity.setApprovalStatus(DeviceConstant.APPROVED);
			entity.setActive(true);
			SecureBiometricInterfaceHistory history = new SecureBiometricInterfaceHistory();
			history = getUpdateHistoryMapping(history, entity);
			sbiHistoryRepository.save(history);
			sbiRepository.save(entity);
			return "Secure biometric details approved successfully.";
		}
		if (secureBiometricInterfaceDto.getApprovalStatus().equals(DeviceConstant.REJECT)) {
			entity.setApprovalStatus(DeviceConstant.REJECTED);
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
				"AUT-008");
		throw new RequestException(SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorCode(),
				String.format(SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorMessage(),
						secureBiometricInterfaceDto.getId()));
	}

	@PersistenceContext(unitName = "authDeviceEntityManagerFactory")
	private EntityManager entityManager;

	@Override
	public <E> PageResponseDto<SbiSearchResponseDto> searchSecureBiometricInterface(Class<E> entity,
			DeviceSearchDto dto) {
		List<SbiSearchResponseDto> sbis = new ArrayList<>();
		PageResponseDto<SbiSearchResponseDto> pageDto = new PageResponseDto<>();		
		Page<E> page = searchHelper.search(entityManager, entity, dto, null);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			sbis = MapperUtils.mapAll(page.getContent(), SbiSearchResponseDto.class);
			pageDto = pageUtils.sortPage(sbis, dto.getSort(), dto.getPagination(),page.getTotalElements());
		}
		return pageDto;
	}
}
