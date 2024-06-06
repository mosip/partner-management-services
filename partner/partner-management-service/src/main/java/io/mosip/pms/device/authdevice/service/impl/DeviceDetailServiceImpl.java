package io.mosip.pms.device.authdevice.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pms.common.constant.CommonConstant;
import io.mosip.pms.common.dto.DeviceFilterValueDto;
import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.RegistrationDeviceSubType;
import io.mosip.pms.device.authdevice.entity.RegistrationDeviceType;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.RegistrationDeviceSubTypeRepository;
import io.mosip.pms.device.authdevice.service.DeviceDetailService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.constant.DeviceDetailExceptionsConstant;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.request.dto.DeviceDetailUpdateDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.response.dto.ColumnCodeValue;
import io.mosip.pms.device.response.dto.DeviceDetailSearchResponseDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.RegistrationSubTypeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.device.util.DeviceUtil;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceDetailServiceImpl implements DeviceDetailService {
	
	@Autowired
	FilterColumnValidator filterColumnValidator;


	@Autowired
	FilterHelper filterHelper;
	
	@Autowired
	AuditUtil auditUtil;

	@Autowired
	DeviceDetailRepository deviceDetailRepository;

	@Autowired
	RegistrationDeviceSubTypeRepository registrationDeviceSubTypeRepository;

	@Autowired
	PartnerServiceRepository partnerRepository;
	
	@Autowired
	SearchHelper searchHelper;
	
	@Autowired
	private PageUtils pageUtils;

	@Override
	@Transactional
	public IdDto createDeviceDetails(DeviceDetailDto deviceDetailDto) {
		DeviceDetail entity = new DeviceDetail();
		DeviceDetail deviceDetail = null;
		IdDto dto = new IdDto();

		RegistrationDeviceSubType registrationDeviceSubType = registrationDeviceSubTypeRepository
				.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(
						deviceDetailDto.getDeviceSubTypeCode(), deviceDetailDto.getDeviceTypeCode());
		if (registrationDeviceSubType == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, DeviceDetail.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorCode(),
							DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorMessage()),
					"AUT-002", deviceDetailDto.getDeviceProviderId(), "partnerId");
			throw new RequestException(DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorCode(),
					DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorMessage());
		} else {
			entity.setDeviceSubTypeCode(registrationDeviceSubType.getCode());
			entity.setDeviceTypeCode(registrationDeviceSubType.getDeviceTypeCode());
		}
		Partner partner = partnerRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(
				deviceDetailDto.getDeviceProviderId()); 
		if (partner == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, DeviceDetail.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorCode(),
							DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorMessage()),
					"AUT-003", deviceDetailDto.getDeviceProviderId(), "partnerId");
			throw new RequestException(DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorCode(),
					DeviceDetailExceptionsConstant.DEVICE_PROVIDER_NOT_FOUND.getErrorMessage());
		}
		entity.setPartnerOrganizationName(partner.getName());
		if (deviceDetailRepository.findUniqueDeviceDetail(deviceDetailDto.getMake(), deviceDetailDto.getModel(),
				deviceDetailDto.getDeviceProviderId(), deviceDetailDto.getDeviceSubTypeCode(),
				deviceDetailDto.getDeviceTypeCode()) != null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_CREATE, DeviceDetail.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_EXIST.getErrorCode(),
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_EXIST.getErrorMessage()),
					"AUT-004", deviceDetailDto.getDeviceProviderId(), "partnerId");
			throw new RequestException(DeviceDetailExceptionsConstant.DEVICE_DETAIL_EXIST.getErrorCode(),
					DeviceDetailExceptionsConstant.DEVICE_DETAIL_EXIST.getErrorMessage());
		}
		entity = getCreateMapping(entity, deviceDetailDto);
		deviceDetail = deviceDetailRepository.save(entity);
		dto.setId(deviceDetail.getId());
		return dto;
	}

	private DeviceDetail getCreateMapping(DeviceDetail deviceDetail, DeviceDetailDto deviceDetailDto) {		
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

	@Override
	@Transactional
	public IdDto updateDeviceDetails(DeviceDetailUpdateDto deviceDetailDto) {
		DeviceDetail entity = new DeviceDetail();
		DeviceDetail deviceDetail = null;
		IdDto dto = new IdDto();
		entity = deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(deviceDetailDto.getId());
		if (entity == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, DeviceDetail.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorCode(),
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorMessage()),
					"AUT-008", deviceDetailDto.getId(), "deviceDetailId");
			throw new RequestException(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorCode(), String
					.format(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorMessage(), dto.getId()));
		}
		RegistrationDeviceSubType registrationDeviceSubType = registrationDeviceSubTypeRepository
				.findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(
						deviceDetailDto.getDeviceSubTypeCode(), deviceDetailDto.getDeviceTypeCode());
		if (registrationDeviceSubType == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, DeviceDetail.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorCode(),
							DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorMessage()),
					"AUT-009", deviceDetailDto.getId(), "deviceDetailId");
			throw new RequestException(DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorCode(),
					DeviceDetailExceptionsConstant.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorMessage());
		} else {
			entity.setDeviceSubTypeCode(registrationDeviceSubType.getCode());
			entity.setDeviceTypeCode(registrationDeviceSubType.getDeviceTypeCode());
		}
		entity = getUpdateMapping(entity, deviceDetailDto);
		deviceDetail = deviceDetailRepository.save(entity);
		dto.setId(deviceDetail.getId());
		return dto;
	}

	private DeviceDetail getUpdateMapping(DeviceDetail deviceDetail, DeviceDetailUpdateDto deviceDetailDto) {
		deviceDetail.setId(deviceDetailDto.getId());

		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			deviceDetail.setUpdBy(authN.getName());
		}
		deviceDetail.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));		
		deviceDetail.setMake(deviceDetailDto.getMake());
		deviceDetail.setModel(deviceDetailDto.getModel());		
		return deviceDetail;

	}

	@Override
	@Transactional
	public String updateDeviceDetailStatus(UpdateDeviceDetailStatusDto deviceDetails) {
		DeviceDetail entity = deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(deviceDetails.getId());
		if (entity == null) {
			auditUtil.auditRequest(
					String.format(DeviceConstant.FAILURE_UPDATE, DeviceDetail.class.getCanonicalName()),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorCode(),
							DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorMessage()),
					"AUT-008", deviceDetails.getId(), "deviceDetailId");
			throw new RequestException(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorCode(),
					String.format(DeviceDetailExceptionsConstant.DEVICE_DETAIL_NOT_FOUND.getErrorMessage(),
							deviceDetails.getId()));
		}
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			entity.setUpdBy(authN.getName());
			entity.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		}

		if (deviceDetails.getApprovalStatus().equals(DeviceConstant.APPROVE)) {
			entity.setApprovalStatus(CommonConstant.APPROVED);
			entity.setIsActive(true);
			deviceDetailRepository.save(entity);
			return "Device details approved successfully.";
		}
		if (deviceDetails.getApprovalStatus().equals(DeviceConstant.REJECT)) {
			entity.setApprovalStatus(CommonConstant.REJECTED);
			entity.setIsActive(false);
			deviceDetailRepository.save(entity);
			return "Device details rejected successfully.";
		}

		auditUtil.auditRequest(
				String.format(DeviceConstant.STATUS_UPDATE_FAILURE, DeviceDetail.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.FAILURE_DESC,
						DeviceDetailExceptionsConstant.DEVICE_STATUS_CODE.getErrorCode(),
						DeviceDetailExceptionsConstant.DEVICE_STATUS_CODE.getErrorMessage()),
				"AUT-008", deviceDetails.getId(), "deviceDetailId");
		throw new RequestException(DeviceDetailExceptionsConstant.DEVICE_STATUS_CODE.getErrorCode(), String
				.format(DeviceDetailExceptionsConstant.DEVICE_STATUS_CODE.getErrorMessage(), deviceDetails.getId()));
	}

	@Override
	public <E> PageResponseDto<DeviceDetailSearchResponseDto> searchDeviceDetails(Class<E> entity,
			DeviceSearchDto dto) {
		List<DeviceDetailSearchResponseDto> deviceDetails = new ArrayList<>();
		PageResponseDto<DeviceDetailSearchResponseDto> pageDto = new PageResponseDto<>();
		Optional<SearchFilter> searchFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("partnerOrganizationName")).findFirst();
		if (searchFilter.isPresent()) {
			dto.getFilters().removeIf(f -> f.getColumnName().equalsIgnoreCase("partnerOrganizationName"));
			List<SearchFilter> filters = new ArrayList<>();
			SearchFilter partnerSearch = new SearchFilter();
			partnerSearch.setColumnName("deviceProviderId");
			List<String> partnersFromDb = partnerRepository.findByNameIgnoreCase(searchFilter.get().getValue());
			if(!partnersFromDb.isEmpty()) {
				partnerSearch.setValues(partnersFromDb);
				partnerSearch.setType("in");
				filters.addAll(dto.getFilters());
				filters.add(partnerSearch);
				dto.setFilters(filters);
			} else {
				return new PageResponseDto<>();
			}
		}
		Page<E> page = searchHelper.search(entity, dto, "deviceProviderId");
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			deviceDetails = MapperUtils.mapAll(page.getContent(), DeviceDetailSearchResponseDto.class);
			pageDto = pageUtils.sortPage(deviceDetails, dto.getSort(), dto.getPagination(), page.getTotalElements());
		}
		return pageDto;
	}

	@Override
	public <E> PageResponseDto<RegistrationSubTypeDto> searchDeviceType(Class<E> entity, DeviceSearchDto dto) {
		List<RegistrationSubTypeDto> deviceSubTypes = new ArrayList<>();
		PageResponseDto<RegistrationSubTypeDto> pageDto = new PageResponseDto<>();
		Page<E> page = searchHelper.search(entity, dto, null);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			deviceSubTypes = MapperUtils.mapAll(page.getContent(), RegistrationSubTypeDto.class);
			pageDto = pageUtils.sortPage(deviceSubTypes, dto.getSort(), dto.getPagination(),page.getTotalElements());
		}
		return pageDto;
	}

	@Override
	public FilterResponseCodeDto deviceFilterValues(DeviceFilterValueDto deviceFilterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, deviceFilterValueDto.getFilters(), DeviceDetail.class)) {
			for (FilterDto filterDto : deviceFilterValueDto.getFilters()) {				
				filterDto.setColumnName(filterDto.getColumnName() + "," + "make" + "," + "model");
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(DeviceDetail.class,
						filterDto, deviceFilterValueDto, "id");
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
	
	/**
	 * 
	 * @param deviceFilterValueDto
	 * @return
	 */
	@Override
	public FilterResponseCodeDto deviceTypeFilterValues(DeviceFilterValueDto deviceFilterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, deviceFilterValueDto.getFilters(), RegistrationDeviceType.class)) {
			for (FilterDto filterDto : deviceFilterValueDto.getFilters()) {
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(RegistrationDeviceType.class,
						filterDto, deviceFilterValueDto, "code");
				filterValues.forEach(filterValue -> {
					ColumnCodeValue columnValue = new ColumnCodeValue();
					columnValue.setFieldCode(filterValue.getFieldCode());
					columnValue.setFieldID(filterDto.getColumnName());
					columnValue.setFieldValue(filterValue.getFieldValue());
					columnValueList.add(columnValue);
				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		return filterResponseDto;
	}
	
	/**
	 * 
	 * @param deviceFilterValueDto
	 * @return
	 */
	@Override
	public FilterResponseCodeDto deviceSubTypeFilterValues(DeviceFilterValueDto deviceFilterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, deviceFilterValueDto.getFilters(), RegistrationDeviceSubType.class)) {
			for (FilterDto filterDto : deviceFilterValueDto.getFilters()) {
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(RegistrationDeviceSubType.class,
						filterDto, deviceFilterValueDto, "code");
				filterValues.forEach(filterValue -> {
					ColumnCodeValue columnValue = new ColumnCodeValue();
					columnValue.setFieldCode(filterValue.getFieldCode());
					columnValue.setFieldID(filterDto.getColumnName());
					columnValue.setFieldValue(filterValue.getFieldValue());
					columnValueList.add(columnValue);
				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		return filterResponseDto;
	}
}
