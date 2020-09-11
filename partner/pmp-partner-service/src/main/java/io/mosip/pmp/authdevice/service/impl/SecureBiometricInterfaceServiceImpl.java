package io.mosip.pmp.authdevice.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pmp.authdevice.constants.SecureBiometricInterfaceConstant;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pmp.authdevice.entity.DeviceDetail;
import io.mosip.pmp.authdevice.entity.SecureBiometricInterface;
import io.mosip.pmp.authdevice.entity.SecureBiometricInterfaceHistory;
import io.mosip.pmp.authdevice.exception.RequestException;
import io.mosip.pmp.authdevice.repository.DeviceDetailRepository;
import io.mosip.pmp.authdevice.repository.SecureBiometricInterfaceHistoryRepository;
import io.mosip.pmp.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pmp.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.authdevice.util.AuthDeviceConstant;

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
	
	@Override
	public IdDto createSecureBiometricInterface(SecureBiometricInterfaceCreateDto sbiDto) {
		SecureBiometricInterface sbi=null;
		SecureBiometricInterface entity=new SecureBiometricInterface();
		IdDto dto=new IdDto();
		DeviceDetail deviceDetail =deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(sbiDto.getDeviceDetailId());
		if (deviceDetail == null) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_CREATE, SecureBiometricInterface.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorCode(),
							SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorMessage()),
					"AUT-015");
			throw new RequestException(SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorCode(),
					SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorMessage());
		}else {
			entity.setDeviceDetailId(deviceDetail.getId());
		}
		String id = UUID.randomUUID().toString();
		entity.setId(id);
		byte[] swNinaryHashArr = sbiDto.getSwBinaryHash().getBytes();
		entity.setSwBinaryHash(swNinaryHashArr);
		entity=getCreateMapping(entity,sbiDto);
		sbi=sbiRepository.save(entity);
		dto.setId(sbi.getId());
		SecureBiometricInterfaceHistory history=new SecureBiometricInterfaceHistory();
		history=getCreateHistoryMapping(history,sbi);
		history.setDeviceDetailId(sbiDto.getDeviceDetailId());
		sbiHistoryRepository.save(history);
		return dto;
	}

	private SecureBiometricInterface getCreateMapping(SecureBiometricInterface entity,SecureBiometricInterfaceCreateDto dto) {
		
		entity.setActive(false);
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
	
	private SecureBiometricInterfaceHistory getCreateHistoryMapping(SecureBiometricInterfaceHistory historyEntity,SecureBiometricInterface entity) {
		historyEntity.setId(entity.getId());
		historyEntity.setActive(entity.isActive());
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
		SecureBiometricInterface sbi=null;
		SecureBiometricInterface entity=new SecureBiometricInterface();
		IdDto dto=new IdDto();
		entity=sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(sbiupdateDto.getId());
		if(entity==null) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(),
							String.format(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), dto.getId())),
					"AUT-016");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), dto.getId()));
		}
		DeviceDetail deviceDetail =deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(sbiupdateDto.getDeviceDetailId());
		if (deviceDetail == null) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorCode(),
							String.format(SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorMessage(), dto.getId())),
					"AUT-018");
			throw new RequestException(SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorCode(),
					SecureBiometricInterfaceConstant.DEVICE_DETAIL_INVALID.getErrorMessage());
		}else {
			entity.setDeviceDetailId(deviceDetail.getId());
		}
		
		entity.setId(sbiupdateDto.getId());
		byte[] swNinaryHashArr = sbiupdateDto.getSwBinaryHash().getBytes();
		entity.setSwBinaryHash(swNinaryHashArr);
		entity=getUpdateMapping(entity,sbiupdateDto);
		sbi=sbiRepository.save(entity);
		dto.setId(sbi.getId());
		SecureBiometricInterfaceHistory history=new SecureBiometricInterfaceHistory();
		history=getUpdateHistoryMapping(history,sbi);
		history.setDeviceDetailId(sbiupdateDto.getDeviceDetailId());
		sbiHistoryRepository.save(history);
		return dto;
	}
	
	private SecureBiometricInterface getUpdateMapping(SecureBiometricInterface entity,SecureBiometricInterfaceUpdateDto dto) {
		
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
	
	private SecureBiometricInterfaceHistory getUpdateHistoryMapping(SecureBiometricInterfaceHistory historyEntity,SecureBiometricInterface entity) {
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
		
		return historyEntity;
		
	}
	
	@Override
	public String updateSecureBiometricInterfaceStatus(SecureBiometricInterfaceStatusUpdateDto secureBiometricInterfaceDto) {
		SecureBiometricInterface entity=sbiRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(secureBiometricInterfaceDto.getId());
		if(entity==null) {
			auditUtil.auditRequest(
					String.format(
							AuthDeviceConstant.FAILURE_UPDATE, SecureBiometricInterface.class.getCanonicalName()),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(),
							String.format(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), secureBiometricInterfaceDto.getId())),
					"AUT-016");
			throw new RequestException(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorCode(),
					String.format(SecureBiometricInterfaceConstant.SBI_NOT_FOUND.getErrorMessage(), secureBiometricInterfaceDto.getId()));
		}

		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			entity.setUpdBy(authN.getName());
			entity.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));			
		}

		if(secureBiometricInterfaceDto.getApprovalStatus().equals(AuthDeviceConstant.APPROVE)) {
			entity.setApprovalStatus(AuthDeviceConstant.APPROVED);
			entity.setActive(true);
			sbiRepository.save(entity);			
			return "Secure biometric details approved successfully.";
		}
		if(secureBiometricInterfaceDto.getApprovalStatus().equals(AuthDeviceConstant.REJECT)) {
			entity.setApprovalStatus(AuthDeviceConstant.REJECTED);	
			entity.setActive(false);
			sbiRepository.save(entity);
			return "Secure biometric details rejected successfully.";
		}

		auditUtil.auditRequest(
				String.format(
						AuthDeviceConstant.STATUS_UPDATE_FAILURE, DeviceDetail.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.FAILURE_DESC,
						SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorCode(),
						SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorMessage()),
				"AUT-008");
		throw new RequestException(SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorCode(),
				String.format(SecureBiometricInterfaceConstant.SBI_STATUS_CODE.getErrorMessage(), secureBiometricInterfaceDto.getId()));
	}
}
