package io.mosip.pms.device.authdevice.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.Set;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.entity.DeviceDetailSBI;
import io.mosip.pms.common.entity.DeviceDetailSBIPK;
import io.mosip.pms.device.authdevice.entity.DeviceDetailEntity;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.device.authdevice.repository.DeviceDetailSummaryRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.dto.DeviceDetailFilterDto;
import io.mosip.pms.device.dto.DeviceDetailSummaryDto;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.device.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import io.mosip.pms.device.request.dto.DeactivateDeviceRequestDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.response.dto.ColumnCodeValue;
import io.mosip.pms.device.response.dto.DeviceDetailSearchResponseDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.RegistrationSubTypeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.device.util.DeviceUtil;

@Service
@Transactional
public class DeviceDetailServiceImpl implements DeviceDetailService {

	private static final Logger LOGGER = PMSLogger.getLogger(DeviceDetailServiceImpl.class);
	public static final String BLANK_STRING = "";
	public static final String VERSION = "1.0";
	public static final String APPROVED = "approved";

	@Value("${mosip.pms.api.id.deactivate.device.patch}")
	private  String patchDeactivateDevice;

	@Value("${mosip.pms.api.id.get.all.device.details.get}")
	private  String getAllDeviceDetailsId;

	@Value("${mosip.pms.api.id.approval.mapping.device.to.sbi.post}")
	private String postApprovalMappingDeviceToSbiId;

	@Autowired
	DeviceDetailSbiRepository deviceDetailSbiRepository;

	@Autowired
	PartnerHelper partnerHelper;
	
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
	DeviceDetailSummaryRepository deviceDetailSummaryRepository;
	
	@Autowired
	private PageUtils pageUtils;

	@Override
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
		if (deviceDetailRepository.findUniqueDeviceDetail(PartnerUtil.trimAndReplace(deviceDetailDto.getMake()), PartnerUtil.trimAndReplace(deviceDetailDto.getModel()),
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

	public static void validatePartnerId(Partner partner, String userId) {
		if (Objects.isNull(partner.getId()) || partner.getId().equals(BLANK_STRING)) {
			LOGGER.info("Partner Id is null or empty for user id : " + userId);
			throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorMessage());
		}
	}

	@Override
	public ResponseWrapperV2<DeviceDetailResponseDto> deactivateDevice(String deviceDetailId, DeactivateDeviceRequestDto requestDto) {
		ResponseWrapperV2<DeviceDetailResponseDto> responseWrapper = new ResponseWrapperV2<>();
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
			if (Objects.isNull(deviceDetailId)) {
				LOGGER.info("sessionId", "idType", "id", "Device id is null.");
				throw new PartnerServiceException(ErrorCode.INVALID_DEVICE_ID.getErrorCode(),
						ErrorCode.INVALID_DEVICE_ID.getErrorMessage());
			}
			Optional<DeviceDetail> deviceDetail = deviceDetailRepository.findById(deviceDetailId);
			if (!deviceDetail.isPresent()) {
				LOGGER.error("Device not exists with id {}", deviceDetailId);
				throw new PartnerServiceException(ErrorCode.DEVICE_NOT_EXISTS.getErrorCode(),
						ErrorCode.DEVICE_NOT_EXISTS.getErrorMessage());
			}
			DeviceDetail device = deviceDetail.get();
			boolean isAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
			if (!isAdmin) {
				Partner partnerDetails = getAssociatedPartner(partnerList, device.getDeviceProviderId(), userId);
				partnerHelper.checkIfPartnerIsNotActive(partnerDetails);
			}
			if (!device.getApprovalStatus().equals(APPROVED)) {
				LOGGER.error("Unable to deactivate device with id {}", device.getId());
				throw new PartnerServiceException(ErrorCode.DEVICE_NOT_APPROVED.getErrorCode(),
						ErrorCode.DEVICE_NOT_APPROVED.getErrorMessage());
			}
			if (device.getApprovalStatus().equals(APPROVED) && !device.getIsActive()) {
				LOGGER.error("Unable to deactivate device with id {}", device.getId());
				throw new PartnerServiceException(ErrorCode.DEVICE_ALREADY_DEACTIVATED.getErrorCode(),
						ErrorCode.DEVICE_ALREADY_DEACTIVATED.getErrorMessage());
			}

			DeviceDetailResponseDto deviceDetailResponseDto = new DeviceDetailResponseDto();

			device.setIsActive(false);
			device.setUpdDtimes(LocalDateTime.now());
			device.setUpdBy(getUserId());
			DeviceDetail updatedDetail = deviceDetailRepository.save(device);
			deviceDetailResponseDto.setDeviceId(updatedDetail.getId());
			deviceDetailResponseDto.setStatus(updatedDetail.getApprovalStatus());
			deviceDetailResponseDto.setActive(updatedDetail.getIsActive());

			responseWrapper.setResponse(deviceDetailResponseDto);

		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In deactivateDevice method of DeviceDetailServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In deactivateDevice method of DeviceDetailServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.DEACTIVATE_DEVICE_ERROR.getErrorCode();
			String errorMessage = ErrorCode.DEACTIVATE_DEVICE_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(patchDeactivateDevice);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<Boolean> approveOrRejectMappingDeviceToSbi(String deviceId, SbiAndDeviceMappingRequestDto requestDto) {
		ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
		try {
			String partnerId = requestDto.getPartnerId();
			String sbiId = requestDto.getSbiId();
            String status = requestDto.getStatus();
			if (Objects.isNull(partnerId) || Objects.isNull(deviceId) || Objects.isNull(status)) {
				LOGGER.info("sessionId", "idType", "id", "Partner/Device id does not exist.");
				throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
						ErrorCode.INVALID_REQUEST_PARAM.getErrorMessage());
			}
			if (!Set.of(DeviceConstant.APPROVED, DeviceConstant.REJECTED).contains(status)) {
				throw new PartnerServiceException(ErrorCode.APPROVE_REJECT_STATUS_CODE.getErrorCode(),
						ErrorCode.APPROVE_REJECT_STATUS_CODE.getErrorMessage());
			}
			if (Objects.isNull(sbiId)) {
				LOGGER.info("sessionId", "idType", "id", "SBI id is null.");
				if (status.equals(DeviceConstant.APPROVED)) {
					throw new PartnerServiceException(ErrorCode.NO_SBI_FOUND_FOR_APPROVE.getErrorCode(),
							ErrorCode.NO_SBI_FOUND_FOR_APPROVE.getErrorMessage());
				} else {
					throw new PartnerServiceException(ErrorCode.NO_SBI_FOUND_FOR_REJECT.getErrorCode(),
							ErrorCode.NO_SBI_FOUND_FOR_REJECT.getErrorMessage());
				}
			}
			// validate sbi and device mapping
			partnerHelper.validateSbiDeviceMapping(partnerId, sbiId, deviceId);

			DeviceDetailSBI deviceDetailSBI = deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(partnerId, sbiId, deviceId);
			if (Objects.isNull(deviceDetailSBI)) {
				LOGGER.info("sessionId", "idType", "id", "SBI and Device mapping already exists in DB.");
				throw new PartnerServiceException(ErrorCode.SBI_DEVICE_MAPPING_NOT_EXISTS.getErrorCode(),
						ErrorCode.SBI_DEVICE_MAPPING_NOT_EXISTS.getErrorMessage());
			}

			UpdateDeviceDetailStatusDto deviceDetails = new UpdateDeviceDetailStatusDto();
			deviceDetails.setId(deviceId);
			if (status.equals(DeviceConstant.REJECTED)) {
				deviceDetails.setApprovalStatus(DeviceConstant.REJECT);
			} else {
				deviceDetails.setApprovalStatus(DeviceConstant.APPROVE);
			}
			updateDeviceDetailStatus(deviceDetails);

			deviceDetailSBI.setIsActive(true);
			deviceDetailSBI.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
			deviceDetailSBI.setUpdBy(getUserId());
			deviceDetailSbiRepository.save(deviceDetailSBI);
			LOGGER.info("sessionId", "idType", "id", "updated device mapping to sbi successfully in Db.");
			responseWrapper.setResponse(true);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In approveOrRejectMappingDeviceToSbi method of DeviceDetailServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In approveOrRejectMappingDeviceToSbi method of DeviceDetailServiceImplN - " + ex.getMessage());
			String errorCode = ErrorCode.APPROVE_OR_REJECT_DEVICE_WITH_SBI_MAPPING_ERROR.getErrorCode();
			String errorMessage = ErrorCode.APPROVE_OR_REJECT_DEVICE_WITH_SBI_MAPPING_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(postApprovalMappingDeviceToSbiId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	public Partner getAssociatedPartner (List<Partner> partnerList, String deviceProviderId, String userId) {
		boolean deviceProviderExist = false;
		Partner partnerDetails = null;
		for (Partner partner : partnerList) {
			if (partner.getId().equals(deviceProviderId)) {
				validatePartnerId(partner, userId);
				deviceProviderExist = true;
				partnerDetails = partner;
				break;
			}
		}
		if (!deviceProviderExist) {
			LOGGER.info("sessionId", "idType", "id", "Device is not associated with user.");
			throw new PartnerServiceException(ErrorCode.DEVICE_NOT_ASSOCIATED_WITH_USER.getErrorCode(),
					ErrorCode.DEVICE_NOT_ASSOCIATED_WITH_USER.getErrorMessage());
		}
		return partnerDetails;
	}

	@Override
	public ResponseWrapperV2<PageResponseV2Dto<DeviceDetailSummaryDto>> getAllDeviceDetails(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, DeviceDetailFilterDto filterDto) {
		ResponseWrapperV2<PageResponseV2Dto<DeviceDetailSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		try {
			PageResponseV2Dto<DeviceDetailSummaryDto> pageResponseV2Dto = new PageResponseV2Dto<>();
			// Pagination
			Pageable pageable = PageRequest.of(pageNo, pageSize);

			// Fetch all device details
			Page<DeviceDetailEntity> page = getDeviceDetails(sortFieldName, sortType, pageNo, pageSize, filterDto, pageable);

			if (Objects.nonNull(page) && !page.getContent().isEmpty()) {
				List<DeviceDetailSummaryDto> deviceDetailSummaryDtoList = MapperUtils.mapAll(page.getContent(), DeviceDetailSummaryDto.class);
				pageResponseV2Dto.setPageNo(pageNo);
				pageResponseV2Dto.setPageSize(pageSize);
				pageResponseV2Dto.setTotalResults(page.getTotalElements());
				pageResponseV2Dto.setData(deviceDetailSummaryDtoList);
			}
			responseWrapper.setResponse(pageResponseV2Dto);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getAllDevices method of DeviceDetailServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getAllDevices method of DeviceDetailServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.GET_ALL_DEVICE_DETAILS_FETCH_ERROR.getErrorCode();
			String errorMessage = ErrorCode.GET_ALL_DEVICE_DETAILS_FETCH_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getAllDeviceDetailsId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	private Page<DeviceDetailEntity> getDeviceDetails(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, DeviceDetailFilterDto filterDto, Pageable pageable) {
		//Sorting
		if (Objects.nonNull(sortFieldName) && Objects.nonNull(sortType)) {
			//sorting handling for the 'status' field
			if (sortFieldName.equals("status") && sortType.equalsIgnoreCase(PartnerConstants.ASC)) {
				return deviceDetailSummaryRepository.
						getSummaryOfAllDeviceDetailsByStatusAsc(filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getDeviceType(),
								filterDto.getDeviceSubType(), filterDto.getStatus(), filterDto.getMake(), filterDto.getModel(),
								filterDto.getSbiId(), filterDto.getSbiVersion(), filterDto.getDeviceId(), pageable);
			} else if (sortFieldName.equals("status") && sortType.equalsIgnoreCase(PartnerConstants.DESC)) {
				return deviceDetailSummaryRepository.
						getSummaryOfAllDeviceDetailsByStatusDesc(filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getDeviceType(),
								filterDto.getDeviceSubType(), filterDto.getStatus(), filterDto.getMake(), filterDto.getModel(),
								filterDto.getSbiId(), filterDto.getSbiVersion(), filterDto.getDeviceId(), pageable);
			}
			//Sorting for other fields
			Sort sort = partnerHelper.getSortingRequest(getSortColumn(partnerHelper.deviceAliasToColumnMap, sortFieldName), sortType);
			pageable = PageRequest.of(pageNo, pageSize, sort);
		}
		//Default
		return deviceDetailSummaryRepository.
				getSummaryOfAllDeviceDetails(filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getDeviceType(),
						filterDto.getDeviceSubType(), filterDto.getStatus(), filterDto.getMake(), filterDto.getModel(),
						filterDto.getSbiId(), filterDto.getSbiVersion(), filterDto.getDeviceId(), pageable);
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

	public static String getCertificateName(String subjectDN) {
		String[] parts = subjectDN.split(",");
		for (String part : parts) {
			if (part.trim().startsWith("CN=")) {
				return part.trim().substring(3);
			}
		}
		return BLANK_STRING;
	}
}
