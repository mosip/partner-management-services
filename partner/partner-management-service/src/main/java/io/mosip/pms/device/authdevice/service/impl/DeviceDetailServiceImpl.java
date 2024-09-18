package io.mosip.pms.device.authdevice.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.entity.DeviceDetailSBI;
import io.mosip.pms.common.entity.DeviceDetailSBIPK;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.device.response.dto.DeviceDetailResponseDto;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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

	@Value("${mosip.pms.api.id.add.inactive.mapping.device.to.sbi.id.post}")
	private  String postInactiveMappingDeviceToSbiId;

	@Value("${mosip.pms.api.id.deactivate.device.post}")
	private  String postDeactivateDevice;

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

	@Override
	public ResponseWrapperV2<Boolean> inactiveMappingDeviceToSbi(SbiAndDeviceMappingRequestDto requestDto) {
		ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
		try {
			String partnerId = requestDto.getPartnerId();
			String sbiId = requestDto.getSbiId();
			String deviceDetailId = requestDto.getDeviceDetailId();
			if (Objects.isNull(partnerId) || Objects.isNull(sbiId) || Objects.isNull(deviceDetailId)  ){
				LOGGER.info("sessionId", "idType", "id", "User id does not exist.");
				throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
						ErrorCode.INVALID_REQUEST_PARAM.getErrorMessage());
			}
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
					validatePartnerId(partner, userId);
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

			// validate sbi and device mapping
			partnerHelper.validateSbiDeviceMapping(partnerId, sbiId, deviceDetailId);

			DeviceDetailSBI deviceDetailSBI = deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(partnerId, sbiId, deviceDetailId);
			if (Objects.nonNull(deviceDetailSBI)){
				LOGGER.info("sessionId", "idType", "id", "SBI and Device mapping already exists in DB.");
				throw new PartnerServiceException(ErrorCode.SBI_DEVICE_MAPPING_ALREADY_EXIST.getErrorCode(),
						ErrorCode.SBI_DEVICE_MAPPING_ALREADY_EXIST.getErrorMessage());
			}

			DeviceDetailSBI entity = new DeviceDetailSBI();

			DeviceDetailSBIPK pk = new DeviceDetailSBIPK();
			pk.setSbiId(sbiId);
			pk.setDeviceDetailId(deviceDetailId);

			entity.setId(pk);
			entity.setProviderId(partnerId);
			entity.setPartnerName(partnerOrgname);
			entity.setIsActive(false);
			entity.setIsDeleted(false);
			entity.setCrBy(userId);
			entity.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));

			DeviceDetailSBI savedEntity = deviceDetailSbiRepository.save(entity);
			LOGGER.info("sessionId", "idType", "id", "saved inactive device mapping to sbi successfully in Db.");
			responseWrapper.setResponse(true);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In inactiveMappingDeviceToSbi method of DeviceDetailServiceImpl - " + ex.getMessage());
			deleteDeviceDetail(requestDto.getDeviceDetailId());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In inactiveMappingDeviceToSbi method of DeviceDetailServiceImpl - " + ex.getMessage());
			deleteDeviceDetail(requestDto.getDeviceDetailId());
			String errorCode = ErrorCode.ADD_INACTIVE_DEVICE_MAPPING_WITH_SBI_ERROR.getErrorCode();
			String errorMessage = ErrorCode.ADD_INACTIVE_DEVICE_MAPPING_WITH_SBI_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(postInactiveMappingDeviceToSbiId);
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

	private void deleteDeviceDetail(String deviceDetailId) {
		try {
			deviceDetailRepository.deleteById(deviceDetailId);
			LOGGER.info("sessionId", "idType", "id", "Device detail with id " + deviceDetailId + " deleted successfully.");
		} catch (Exception e) {
			LOGGER.error("sessionId", "idType", "id", "Error while deleting device detail with id " + deviceDetailId + ": " + e.getMessage());
		}
	}

	@Override
	public ResponseWrapperV2<DeviceDetailResponseDto> deactivateDevice(String deviceDetailId) {
		ResponseWrapperV2<DeviceDetailResponseDto> responseWrapper = new ResponseWrapperV2<>();
		try {
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
			// check if the device is associated with user.
			String deviceProviderId = device.getDeviceProviderId();
			boolean deviceProviderExist = false;
			Partner partnerDetails = new Partner();
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
			//check if Partner is Active or not
			if (!partnerDetails.getIsActive()) {
				LOGGER.error("Partner is not Active with id {}", deviceProviderId);
				throw new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
						ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
			}
			// Deactivate only if the device is approved status and is_active true.
			if (device.getApprovalStatus().equals(APPROVED) && device.getIsActive()) {
				DeviceDetailResponseDto deviceDetailResponseDto = new DeviceDetailResponseDto();

				device.setIsActive(false);
				DeviceDetail updatedDetail = deviceDetailRepository.save(device);
				deviceDetailResponseDto.setDeviceId(updatedDetail.getId());
				deviceDetailResponseDto.setStatus(updatedDetail.getApprovalStatus());
				deviceDetailResponseDto.setActive(updatedDetail.getIsActive());

				responseWrapper.setResponse(deviceDetailResponseDto);
			} else {
				LOGGER.error("Unable to deactivate device with id {}", device.getId());
				throw new PartnerServiceException(ErrorCode.UNABLE_TO_DEACTIVATE_DEVICE.getErrorCode(),
						ErrorCode.UNABLE_TO_DEACTIVATE_DEVICE.getErrorMessage());
			}
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
		responseWrapper.setId(postDeactivateDevice);
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
