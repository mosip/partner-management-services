package io.mosip.pms.partner.misp.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.misp.dto.MISPFilterDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseSummaryDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseRequestDtoV2;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDtoV2;
import io.mosip.pms.partner.misp.dto.MISPLicenseDetailsDto;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.partner.util.PartnerUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import io.mosip.pms.common.constant.ConfigKeyConstants;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.MISPDataPublishDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.PolicyPublishDto;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.entity.Partner;

import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.entity.MISPLicenseSummaryEntity;
import io.mosip.pms.common.entity.MISPLicenseEntityV2;
import io.mosip.pms.common.entity.MISPLicenseEntityPK;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.MispLicenseRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.MISPLicenseSummaryRepository;
import io.mosip.pms.common.repository.MispLicenseV2Repository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.UserDetailUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.response.dto.ColumnCodeValue;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.dto.MISPDeactivateRequestDto;
import io.mosip.pms.partner.misp.dto.MISPDeactivateResponseDto;
import io.mosip.pms.partner.misp.dto.MISPRegenerateRequestDto;
import io.mosip.pms.partner.misp.exception.MISPErrorMessages;
import io.mosip.pms.partner.misp.exception.MISPServiceException;
import io.mosip.pms.partner.misp.service.InfraServiceProviderService;

@Component
public class InfraProviderServiceImpl implements InfraServiceProviderService {

	private static final Logger LOGGER = PMSLogger.getLogger(InfraProviderServiceImpl.class);
	public static final String VERSION = "1.0";

	@Value("${mosip.kernel.idgenerator.misp.license-key-length}")
	private int licenseKeyLength;

	@Value("${mosip.pmp.misp.license.expiry.period.indays}")
	private int mispLicenseExpiryInDays;

	@Value("${mosip.pms.api.id.misp.licenses.get}")
	private String getAllMispLicensesId;

	@Value("${mosip.pms.api.id.misp.generate.license.post}")
	private String postGenerateMISPApiId;

	@Value("${mosip.pms.api.id.misp.licenses.details.get}")
	private String getMispLicenseDetailsId;

	@Value("${mosip.pms.api.id.deactivate.misp.license.patch}")
	private String patchDeactivateMISPApiId;

	@Value("${mosip.pms.api.id.regenerate.misp.license.put}")
	private String putRegenerateMISPApiId;

	@Autowired
	MISPLicenseSummaryRepository mispLicenseSummaryRepository;

	@Autowired
	MispLicenseRepository mispLicenseRepository;

	@Autowired
	MispLicenseV2Repository mispLicenseV2Repository;

	@Autowired
	PartnerServiceRepository partnerRepository;

	@Autowired
	PolicyGroupRepository policyGroupRepository;

	@Autowired
	private WebSubPublisher webSubPublisher;

	@Autowired
	private Environment environment;

	@Autowired
	SearchHelper searchHelper;

	@Autowired
	FilterColumnValidator filterColumnValidator;

	@Autowired
	FilterHelper filterHelper;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	private PageUtils pageUtils;

	@Autowired
	private PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@Autowired
	private AuthPolicyRepository  authPolicyRepository;

	public static final String APPROVED_STATUS = "approved";
	public static final String REJECTED_STATUS = "rejected";
	public static final String ACTIVE_STATUS = "active";
	public static final String NOTACTIVE_STATUS = "de-active";
	public static final String ACTIVE = "ACTIVE";
	public static final String INACTIVE = "INACTIVE";
	public static final String NOTACTIVE = "NOT_ACTIVE";

	/**
	 *
	 */
	@Override
	public MISPLicenseResponseDto approveInfraProvider(String mispId) {
		validateLoggedInUserAuthorization(mispId);
		List<MISPLicenseEntity> mispLicenseFromDb = mispLicenseRepository.findByMispId(mispId);
		if (!mispLicenseFromDb.isEmpty()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_KEY_EXISTS.getErrorCode(),
					MISPErrorMessages.MISP_LICENSE_KEY_EXISTS.getErrorMessage());
		}
		Optional<Partner> partnerFromDb = partnerRepository.findById(mispId);
		if (partnerFromDb.isEmpty()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_ID_NOT_EXISTS.getErrorCode(),
					MISPErrorMessages.MISP_ID_NOT_EXISTS.getErrorMessage());
		}
		if (!partnerFromDb.get().getPartnerTypeCode()
				.equalsIgnoreCase(environment.getProperty(ConfigKeyConstants.MISP_PARTNER_TYPE, "MISP_Partner"))) {
			throw new MISPServiceException(MISPErrorMessages.MISP_ID_NOT_VALID.getErrorCode(),
					MISPErrorMessages.MISP_ID_NOT_VALID.getErrorMessage());
		}
		if (!partnerFromDb.get().getIsActive()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_IS_INACTIVE.getErrorCode(),
					MISPErrorMessages.MISP_IS_INACTIVE.getErrorMessage());
		}

		List<PartnerPolicyRequest> approvedPolicyMappedReq = partnerPolicyRequestRepository.findByPartnerId(mispId);
		Optional<AuthPolicy> mispPolicyFromDb = Optional.empty();
		if(!approvedPolicyMappedReq.isEmpty()) {
			if(!approvedPolicyMappedReq.stream().allMatch(p->p.getStatusCode().equalsIgnoreCase(APPROVED_STATUS))){
				throw new MISPServiceException(MISPErrorMessages.MISP_POLICY_NOT_APPROVED.getErrorCode(),
						MISPErrorMessages.MISP_POLICY_NOT_APPROVED.getErrorMessage());
			}

			mispPolicyFromDb = authPolicyRepository.findById(approvedPolicyMappedReq.get(0).getPolicyId());
			if(mispPolicyFromDb.isEmpty()) {
				throw new MISPServiceException(MISPErrorMessages.MISP_POLICY_NOT_EXISTS.getErrorCode(),
						MISPErrorMessages.MISP_POLICY_NOT_EXISTS.getErrorMessage());
			}
		}

		String policyId = mispPolicyFromDb.isPresent()?mispPolicyFromDb.get().getId():null;
		MISPLicenseEntity newLicenseKey = generateLicense(mispId, policyId);
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		response.setLicenseKey(newLicenseKey.getLicenseKey());
		response.setLicenseKeyExpiry(newLicenseKey.getValidToDate());
		response.setLicenseKeyStatus("Active");
		response.setProviderId(mispId);
		if(mispPolicyFromDb.isPresent()) {
			notify(MapperUtils.mapDataToPublishDto(newLicenseKey), MapperUtils.mapPolicyToPublishDto(mispPolicyFromDb.get(),
					getPolicyObject(mispPolicyFromDb.get().getPolicyFileId())), EventType.MISP_LICENSE_GENERATED);
		}
		else {
			notify(MapperUtils.mapDataToPublishDto(newLicenseKey), null, EventType.MISP_LICENSE_GENERATED);
		}

		return response;
	}

	/**
	 *
	 * @param policy
	 * @return
	 */
	private JSONObject getPolicyObject(String policy) {
		JSONParser parser = new JSONParser();
		String error = null;
		try {
			return ((JSONObject) parser.parse(policy));
		} catch (ParseException e) {
			error = e.getMessage();
		}
		throw new MISPServiceException(ErrorCode.POLICY_PARSING_ERROR.getErrorCode(),
				ErrorCode.POLICY_PARSING_ERROR.getErrorMessage() + error);
	}

	/**
	 *
	 * @return
	 */
	private String generateLicenseKey() {
		String chrs = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		SecureRandom secureRandom = new SecureRandom();
		String licenseKey = secureRandom.ints(licenseKeyLength, 0, chrs.length()).mapToObj(i -> chrs.charAt(i))
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
		return licenseKey;
	}

	/**
	 *
	 */
	@Override
	public MISPLicenseResponseDto updateInfraProvider(String id, String licenseKey, String status) {
		if (!(status.toLowerCase().equals(ACTIVE_STATUS) || status.toLowerCase().equals(NOTACTIVE_STATUS))) {
			throw new MISPServiceException(MISPErrorMessages.MISP_STATUS_CODE_EXCEPTION.getErrorCode(),
					MISPErrorMessages.MISP_STATUS_CODE_EXCEPTION.getErrorMessage());
		}

		MISPLicenseEntityV2 mispLicenseFromDb = mispLicenseV2Repository.findByPartnerIdAndLicenseKey(id, licenseKey);
		if (mispLicenseFromDb == null) {
			throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorCode(),
					MISPErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorMessage());
		}
		mispLicenseFromDb.setUpdatedBy(getLoggedInUserId());
		mispLicenseFromDb.setUpdatedDateTime(LocalDateTime.now());
		mispLicenseFromDb.setIsActive(status.toLowerCase().equals(ACTIVE_STATUS) ? true : false);
		mispLicenseV2Repository.save(mispLicenseFromDb);
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		response.setLicenseKey(mispLicenseFromDb.getId().getLicenseKey());
		response.setLicenseKeyExpiry(mispLicenseFromDb.getValidToDate());
		response.setLicenseKeyStatus(mispLicenseFromDb.getIsActive() ? ACTIVE_STATUS : NOTACTIVE_STATUS);
		response.setProviderId(mispLicenseFromDb.getId().getMispId());
		notify(MapperUtils.mapDataToPublishDtoV2(mispLicenseFromDb), EventType.MISP_LICENSE_UPDATED);
		return response;

	}

	/**
	 *
	 */
	@Override
	public List<MISPLicenseEntity> getInfraProvider() {
		return mispLicenseRepository.findAll();
	}

	/**
	 *
	 * @param mispId
	 * @return
	 */
	private MISPLicenseEntity generateLicense(String mispId,@Nullable String policyId) {
		MISPLicenseEntity entity = new MISPLicenseEntity();
		entity.setMispId(mispId);
		entity.setLicenseKey(generateLicenseKey());
		entity.setValidFromDate(LocalDateTime.now());
		entity.setValidToDate(LocalDateTime.now().plusDays(mispLicenseExpiryInDays));
		entity.setCreatedBy(getLoggedInUserId());
		entity.setCreatedDateTime(LocalDateTime.now());
		entity.setIsActive(true);
		entity.setIsDeleted(false);
		entity.setPolicyId(policyId);
		mispLicenseRepository.save(entity);
		return entity;
	}

	/**
	 *
	 */
	@Override
	public MISPLicenseResponseDto regenerateKey(String mispId) {
		Optional<Partner> partnerFromDb = partnerRepository.findById(mispId);
		if (partnerFromDb.isEmpty()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_ID_NOT_EXISTS.getErrorCode(),
					MISPErrorMessages.MISP_ID_NOT_EXISTS.getErrorMessage());
		}
		if (!partnerFromDb.get().getIsActive()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_IS_INACTIVE.getErrorCode(),
					MISPErrorMessages.MISP_IS_INACTIVE.getErrorMessage());
		}
		List<MISPLicenseEntity> mispValidLicenses = mispLicenseRepository.findByMispIdandExpirydate(mispId);
		if(mispLicenseRepository.findByMispId(mispId).isEmpty()) {
			throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorCode(),
					MISPErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorMessage());
		}
		List<PartnerPolicyRequest> approvedPolicyMappedReq = partnerPolicyRequestRepository.findByPartnerId(mispId);
		PartnerPolicyRequest mispPolicy= new  PartnerPolicyRequest();
		String policyId = null;
		if (!approvedPolicyMappedReq.isEmpty()) {
			mispPolicy = approvedPolicyMappedReq.get(0);
			if (mispPolicy.getPolicyId() != null && !mispPolicy.getPolicyId().isBlank()) {
				policyId = mispPolicy.getPolicyId();
			}
		}
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		if (mispValidLicenses.isEmpty()) {
			MISPLicenseEntity newLicenseKey = generateLicense(mispId, policyId);
			response.setLicenseKey(newLicenseKey.getLicenseKey());
			response.setLicenseKeyExpiry(newLicenseKey.getValidToDate());
			response.setLicenseKeyStatus("Active");
			response.setProviderId(mispId);

			Optional<AuthPolicy> mispPolicyFromDb = Optional.empty();
			if(!approvedPolicyMappedReq.isEmpty()) {
				if(!mispPolicy.getStatusCode().equalsIgnoreCase(APPROVED_STATUS)){
					throw new MISPServiceException(MISPErrorMessages.MISP_POLICY_NOT_APPROVED.getErrorCode(),
							MISPErrorMessages.MISP_POLICY_NOT_APPROVED.getErrorMessage());
				}

				mispPolicyFromDb = authPolicyRepository.findById(mispPolicy.getPolicyId());
				if(mispPolicyFromDb.isEmpty()) {
					throw new MISPServiceException(MISPErrorMessages.MISP_POLICY_NOT_EXISTS.getErrorCode(),
							MISPErrorMessages.MISP_POLICY_NOT_EXISTS.getErrorMessage());
				}
			}

			if(mispPolicyFromDb.isPresent()) {
				notify(MapperUtils.mapDataToPublishDto(newLicenseKey), MapperUtils.mapPolicyToPublishDto(mispPolicyFromDb.get(),
						getPolicyObject(mispPolicyFromDb.get().getPolicyFileId())), EventType.MISP_LICENSE_UPDATED);
			}
			else {
				notify(MapperUtils.mapDataToPublishDto(newLicenseKey), EventType.MISP_LICENSE_UPDATED);
			}
		}
		else {
			response.setLicenseKey(mispValidLicenses.get(0).getLicenseKey());
			response.setLicenseKeyExpiry(mispValidLicenses.get(0).getValidToDate());
			response.setLicenseKeyStatus("Active");
			response.setProviderId(mispId);
		}


		return response;
	}

	/**
	 *
	 * @return
	 */
	public String getLoggedInUserId() {
		return UserDetailUtil.getLoggedInUserId();
	}

	/**
	 *
	 * @param dataToPublish
	 * @param eventType
	 */
	private void notify(MISPDataPublishDto dataToPublish, EventType eventType) {
		Type type = new Type();
		type.setName("InfraProviderServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.InfraProviderServiceImpl");
		Map<String, Object> data = new HashMap<>();
		data.put("mispLicenseData", dataToPublish);
		webSubPublisher.notify(eventType, data, type);
	}

	private void notify(MISPDataPublishDto dataToPublish, PolicyPublishDto policyDataToPublish,EventType eventType) {
		Type type = new Type();
		type.setName("InfraProviderServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.InfraProviderServiceImpl");
		Map<String, Object> data = new HashMap<>();
		data.put("mispLicenseData", dataToPublish);
		if (dataToPublish != null) {
			data.put(PartnerConstants.MISP_DATA, dataToPublish);
		}
		if (policyDataToPublish != null) {
			data.put(PartnerConstants.POLICY_DATA, policyDataToPublish);
		}
		webSubPublisher.notify(eventType, data, type);
	}

	@Override
	public FilterResponseCodeDto filterValues(FilterValueDto filterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (searchHelper.isLoggedInUserFilterRequired()) {
			SearchFilter loggedInUserFilterDto = new SearchFilter();
			loggedInUserFilterDto.setColumnName("misp_id");
			loggedInUserFilterDto.setValue(getLoggedInUserId());
			loggedInUserFilterDto.setType("equals");
			filterValueDto.getOptionalFilters().add(loggedInUserFilterDto);
		}
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), MISPLicenseEntity.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(MISPLicenseEntity.class, filterDto, filterValueDto, "mispId");
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
	public PageResponseDto<MISPLicenseEntity> search(SearchDto dto) {
		PageResponseDto<MISPLicenseEntity> pageDto = new PageResponseDto<>();
		Page<MISPLicenseEntity> page = searchHelper.search(MISPLicenseEntity.class, dto, "mispId");
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			pageDto = pageUtils.sortPage(page.getContent(), dto.getSort(), dto.getPagination(),
					page.getTotalElements());
		}
		return pageDto;
	}

	/**
	 * validates the loggedInUser authorization
	 * @param loggedInUserId
	 */
	public void validateLoggedInUserAuthorization(String loggedInUserId) {
		if(searchHelper.isLoggedInUserFilterRequired() && !loggedInUserId.equals(getLoggedInUserId())) {
			throw new PartnerServiceException(ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode(),
					ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorMessage());
		}
	}

	@Override
	public ResponseWrapperV2<PageResponseV2Dto<MISPLicenseSummaryDto>> getAllMISPLicenses(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, MISPFilterDto filterDto) {
		ResponseWrapperV2<PageResponseV2Dto<MISPLicenseSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		try {
			PageResponseV2Dto<MISPLicenseSummaryDto> pageResponseV2Dto = new PageResponseV2Dto<>();
			partnerHelper.validateRequestParameters(partnerHelper.mispAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);

			Pageable pageable = PageRequest.of(pageNo, pageSize);

			if (Objects.nonNull(sortFieldName) && Objects.nonNull(sortType)) {
				if (sortFieldName.equalsIgnoreCase("status")) {
					sortType = sortType.equalsIgnoreCase(PartnerConstants.ASC) ? PartnerConstants.DESC : PartnerConstants.ASC;
				}
				Sort sort = partnerHelper.getSortingRequest(getSortColumn(partnerHelper.mispAliasToColumnMap, sortFieldName), sortType);
				pageable = PageRequest.of(pageNo, pageSize, sort);
			}
			Page<MISPLicenseSummaryEntity> page = mispLicenseSummaryRepository.getSummaryOfAllMispLicenseDetails(filterDto.getPartnerId(), filterDto.getOrgName(), filterDto.getPolicyGroupName(),
					filterDto.getPolicyName(), filterDto.getMispLicenseKeyName(), filterDto.getStatus(), pageable);
			if (Objects.nonNull(page) && !page.getContent().isEmpty()) {
				List<MISPLicenseSummaryDto> mispLicenseSummaryDtoList = MapperUtils.mapAll(page.getContent(), MISPLicenseSummaryDto.class);
				pageResponseV2Dto.setPageNo(pageNo);
				pageResponseV2Dto.setPageSize(pageSize);
				pageResponseV2Dto.setTotalResults(page.getTotalElements());
				pageResponseV2Dto.setData(mispLicenseSummaryDtoList);
			}
			responseWrapper.setResponse(pageResponseV2Dto);

		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getAllMISPLicenses method of InfraProviderServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getAllMISPLicenses method of InfraProviderServiceImpl - " + ex.getMessage());
			String errorCode = MISPErrorMessages.ERROR_FETCHING_MISP_DETAILS.getErrorCode();
			String errorMessage = MISPErrorMessages.ERROR_FETCHING_MISP_DETAILS.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getAllMispLicensesId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	public String getSortColumn(Map<String, String> aliasToColumnMap, String alias) {
		return aliasToColumnMap.getOrDefault(alias, alias); // Return alias if no match found
	}

	private void expiryDateInputValidation(LocalDate expiryDate) {
		if(Objects.isNull(expiryDate) || expiryDate.toString().isBlank()) {
			throw new MISPServiceException(MISPErrorMessages.INVALID_EXPIRY_DATE.getErrorCode(),
					MISPErrorMessages.INVALID_EXPIRY_DATE.getErrorMessage());
		}
	}

	private Optional<AuthPolicy> validatePolicy(String partnerId, String policyId) {
		if (policyId == null || policyId.isBlank()) {
			return Optional.empty();
		}

		Optional<AuthPolicy> mispPolicyFromDb = authPolicyRepository.findById(policyId);
		if (mispPolicyFromDb.isEmpty()) {
			throw new MISPServiceException(
					MISPErrorMessages.POLICY_ID_NOT_EXISTS.getErrorCode(),
					MISPErrorMessages.POLICY_ID_NOT_EXISTS.getErrorMessage()
			);
		}

		List<PartnerPolicyRequest> approvedPartnerPolicyRequests =
				partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(partnerId, policyId, "approved");
		if (approvedPartnerPolicyRequests.isEmpty()) {
			throw new MISPServiceException(
					MISPErrorMessages.PARTNER_POLICY_NOT_APPROVED.getErrorCode(),
					MISPErrorMessages.PARTNER_POLICY_NOT_APPROVED.getErrorMessage()
			);
		}
		return mispPolicyFromDb;
	}


	@Override
	public ResponseWrapperV2<MISPLicenseResponseDtoV2> generateMISPLicense( MISPLicenseRequestDtoV2 request) {
		ResponseWrapperV2<MISPLicenseResponseDtoV2> responseWrapper = new ResponseWrapperV2<>();
		try {
			// input validations
			String partnerId = request.getPartnerId();
			if(Objects.isNull(partnerId) || partnerId.trim().isBlank()) {
				throw new MISPServiceException(MISPErrorMessages.INVALID_PARTNER_ID.getErrorCode(),
						MISPErrorMessages.INVALID_PARTNER_ID.getErrorMessage());
			}
			String licenseKeyName = request.getLicenseKeyName();
			if(Objects.isNull(licenseKeyName) || licenseKeyName.trim().isBlank()) {
				throw new MISPServiceException(MISPErrorMessages.INVALID_LICENSE_KEY_NAME.getErrorCode(),
						MISPErrorMessages.INVALID_LICENSE_KEY_NAME.getErrorMessage());
			}
			LocalDate expiryDate = request.getExpiryDate();
			expiryDateInputValidation(expiryDate);

			// partnerId validation
			Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
			if (partnerFromDb.isEmpty()) {
				throw new MISPServiceException(MISPErrorMessages.PARTNER_ID_NOT_EXISTS.getErrorCode(),
						MISPErrorMessages.PARTNER_ID_NOT_EXISTS.getErrorMessage());
			}
			if (!partnerFromDb.get().getPartnerTypeCode()
					.equalsIgnoreCase(environment.getProperty(ConfigKeyConstants.MISP_PARTNER_TYPE, "MISP_Partner"))) {
				throw new MISPServiceException(MISPErrorMessages.INVALID_PARTNER_TYPE.getErrorCode(),
						MISPErrorMessages.INVALID_PARTNER_TYPE.getErrorMessage());
			}
			if (!partnerFromDb.get().getIsActive()) {
				throw new MISPServiceException(MISPErrorMessages.PARTNER_NOT_ACTIVE.getErrorCode(),
						MISPErrorMessages.PARTNER_NOT_ACTIVE.getErrorMessage());
			}

			// policyId validation
			Optional<AuthPolicy> mispPolicyFromDb = validatePolicy(partnerId, request.getPolicyId());
			String policyId = mispPolicyFromDb.map(AuthPolicy::getId).orElse(null);

			// licenseKeyName validation
			List<MISPLicenseEntityV2> mispLicenseFromDb = mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(partnerId, policyId, PartnerUtil.trimAndReplace(licenseKeyName));
			if (!mispLicenseFromDb.isEmpty()) {
				throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_KEY_NAME_EXISTS.getErrorCode(),
						MISPErrorMessages.MISP_LICENSE_KEY_NAME_EXISTS.getErrorMessage());
			}

			// expiryDate validation
			if(expiryDate.isBefore(LocalDate.now()) || expiryDate.isEqual(LocalDate.now())) {
				throw new MISPServiceException(MISPErrorMessages.EXPIRYDATE_SHOULD_BE_GREATER_THAN_TODAYS_DATE.getErrorCode(),
						MISPErrorMessages.EXPIRYDATE_SHOULD_BE_GREATER_THAN_TODAYS_DATE.getErrorMessage());
			}

			MISPLicenseEntityV2 entity = new MISPLicenseEntityV2();
			MISPLicenseEntityPK pk = new MISPLicenseEntityPK();
			pk.setMispId(partnerId);
			pk.setLicenseKey(generateLicenseKey());
			entity.setId(pk);
			entity.setLicenseKeyName(licenseKeyName);
			entity.setValidFromDate(LocalDateTime.now(ZoneId.of("UTC")));
			// Get current UTC time
			LocalTime currentUtcTime = LocalTime.now(ZoneOffset.UTC);
			entity.setValidToDate(LocalDateTime.of(expiryDate, currentUtcTime));
			entity.setCreatedBy(getLoggedInUserId());
			entity.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
			entity.setIsActive(true);
			entity.setIsDeleted(false);
			entity.setPolicyId(policyId);
			mispLicenseV2Repository.save(entity);

			MISPLicenseResponseDtoV2 responseDtoV2 = new MISPLicenseResponseDtoV2();
			responseDtoV2.setLicenseKey(entity.getId().getLicenseKey());
			responseDtoV2.setExpiryDateTime(entity.getValidToDate());
			responseDtoV2.setLicenseKeyName(entity.getLicenseKeyName());
			responseDtoV2.setLicenseKeyStatus(ACTIVE);
			responseDtoV2.setPartnerId(entity.getId().getMispId());
			responseDtoV2.setPolicyId(entity.getPolicyId());
			if(mispPolicyFromDb.isPresent()) {
				notify(MapperUtils.mapDataToPublishDtoV2(entity), MapperUtils.mapPolicyToPublishDto(mispPolicyFromDb.get(),
						getPolicyObject(mispPolicyFromDb.get().getPolicyFileId())), EventType.MISP_LICENSE_GENERATED);
			}
			else {
				notify(MapperUtils.mapDataToPublishDtoV2(entity), null, EventType.MISP_LICENSE_GENERATED);
			}
			responseWrapper.setResponse(responseDtoV2);
		} catch (MISPServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In generateMISPLicense method of InfraProviderServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In generateMISPLicense method of InfraProviderServiceImpl - " + ex.getMessage());
			String errorCode = MISPErrorMessages.ERROR_GENERATING_MISP_LICENSE.getErrorCode();
			String errorMessage = MISPErrorMessages.ERROR_GENERATING_MISP_LICENSE.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(postGenerateMISPApiId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<MISPLicenseDetailsDto> getMISPLicenseDetails(String partnerId, String policyId, String mispLicenseKeyName){
		ResponseWrapperV2<MISPLicenseDetailsDto> responseWrapper = new ResponseWrapperV2<>();
		try {
			if (Objects.isNull(partnerId) || partnerId.isEmpty()) {
				throw new MISPServiceException(MISPErrorMessages.INVALID_PARTNER_ID.getErrorCode(),
						MISPErrorMessages.INVALID_PARTNER_ID.getErrorMessage());
			}
			Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
			if (partnerFromDb.isEmpty()) {
				throw new MISPServiceException(MISPErrorMessages.PARTNER_ID_NOT_EXISTS.getErrorCode(),
						MISPErrorMessages.PARTNER_ID_NOT_EXISTS.getErrorMessage());
			}
			List<MISPLicenseEntityV2> mispLicenseFromDb = mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(partnerId, policyId, mispLicenseKeyName);
			if (mispLicenseFromDb.isEmpty()) {
				throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_NOT_EXISTS.getErrorCode(),
						MISPErrorMessages.MISP_LICENSE_NOT_EXISTS.getErrorMessage());
			}
			if (mispLicenseFromDb.size() > 1) {
				throw new MISPServiceException(MISPErrorMessages.MULTIPLE_MISP_LICENSES_FOUND.getErrorCode(),
						MISPErrorMessages.MULTIPLE_MISP_LICENSES_FOUND.getErrorMessage());
			}
			MISPLicenseEntityV2 entity = mispLicenseFromDb.getFirst();

			MISPLicenseDetailsDto responseDto = new MISPLicenseDetailsDto();
			responseDto.setPartnerId(entity.getId().getMispId());
			responseDto.setOrgName(partnerFromDb.get().getName());

			String policyGroupId = partnerFromDb.get().getPolicyGroupId();
			if (Objects.nonNull(policyGroupId)) {
				PolicyGroup policyGroup = policyGroupRepository.findPolicyGroupById(policyGroupId);
				if (Objects.isNull(policyGroup)) {
					throw new MISPServiceException(
							ErrorCode.MATCHING_POLICY_GROUP_NOT_EXISTS.getErrorCode(),
							ErrorCode.MATCHING_POLICY_GROUP_NOT_EXISTS.getErrorMessage()
					);
				}
				responseDto.setPolicyGroupId(policyGroupId);
				responseDto.setPolicyGroupName(policyGroup.getName());
				responseDto.setPolicyGroupDescription(policyGroup.getDesc());
			}

			String policyIdFromDb = entity.getPolicyId();
			if (Objects.nonNull(policyIdFromDb)) {
				Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(policyIdFromDb);
				if (authPolicy.isEmpty()) {
					throw new MISPServiceException(MISPErrorMessages.MISP_POLICY_NOT_EXISTS.getErrorCode(),
							MISPErrorMessages.MISP_POLICY_NOT_EXISTS.getErrorMessage());
				}
				responseDto.setPolicyId(policyIdFromDb);
				responseDto.setPolicyName(authPolicy.get().getName());
				responseDto.setPolicyDescription(authPolicy.get().getDescr());
			}

			String key = entity.getId().getLicenseKey();
			String maskedKey = "*".repeat(key.length() - 4) + key.substring(key.length() - 4);
			responseDto.setMispLicenseKey(maskedKey);
			responseDto.setMispLicenseKeyName(entity.getLicenseKeyName());
			responseDto.setStatus(entity.getIsActive() ? ACTIVE : INACTIVE);
			responseDto.setExpiryDateTime(entity.getValidToDate());
			responseDto.setCreatedDateTime(entity.getCreatedDateTime());

			responseWrapper.setResponse(responseDto);
		} catch (MISPServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getMISPLicenseDetails method of InfraProviderServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getMISPLicenseDetails method of InfraProviderServiceImpl - " + ex.getMessage());
			String errorCode = MISPErrorMessages.ERROR_FETCHING_INDIVIDUAL_MISP_DETAILS.getErrorCode();
			String errorMessage = MISPErrorMessages.ERROR_FETCHING_INDIVIDUAL_MISP_DETAILS.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getMispLicenseDetailsId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<MISPDeactivateResponseDto> deactivateMISPLicense(String partnerId, MISPDeactivateRequestDto request) {
		ResponseWrapperV2<MISPDeactivateResponseDto> responseWrapper = new ResponseWrapperV2<>();
		try {
			if (Objects.isNull(partnerId) || partnerId.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "Partner id is null.");
				throw new MISPServiceException(MISPErrorMessages.INVALID_PARTNER_ID.getErrorCode(),
						MISPErrorMessages.INVALID_PARTNER_ID.getErrorMessage());
			}
			String status = request.getStatus();
			if (Objects.isNull(status) || !status.equals(PartnerConstants.DEACTIVATE)) {
				LOGGER.info("sessionId", "idType", "id", " Status is invalid, it should be (De-Activate)");
				throw new MISPServiceException(MISPErrorMessages.DEACTIVATE_STATUS_CODE.getErrorCode(),
						MISPErrorMessages.DEACTIVATE_STATUS_CODE.getErrorMessage());
			}
			Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
			if (partnerFromDb.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "Partner id does not exist.");
				throw new MISPServiceException(MISPErrorMessages.PARTNER_ID_NOT_EXISTS.getErrorCode(),
						MISPErrorMessages.PARTNER_ID_NOT_EXISTS.getErrorMessage());
			}
			if (!partnerFromDb.get().getIsActive()) {
				LOGGER.info("sessionId", "idType", "id", "Partner is not active.");
				throw new MISPServiceException(MISPErrorMessages.PARTNER_NOT_ACTIVE.getErrorCode(),
						MISPErrorMessages.PARTNER_NOT_ACTIVE.getErrorMessage());
			}
			List<MISPLicenseEntityV2> mispLicenseFromDb = mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(partnerId, request.getPolicyId(), request.getLicenseKeyName());
			if (mispLicenseFromDb.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "MISP License does not exist.");
				throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_NOT_EXISTS.getErrorCode(),
						MISPErrorMessages.MISP_LICENSE_NOT_EXISTS.getErrorMessage());
			}
			if (mispLicenseFromDb.size() > 1) {
				LOGGER.info("sessionId", "idType", "id", "Multiple MISP licenses found.");
				throw new MISPServiceException(MISPErrorMessages.MULTIPLE_MISP_LICENSES_FOUND.getErrorCode(),
						MISPErrorMessages.MULTIPLE_MISP_LICENSES_FOUND.getErrorMessage());
			}
			MISPLicenseEntityV2 entity = mispLicenseFromDb.getFirst();
			if (!entity.getIsActive()) {
				LOGGER.info("sessionId", "idType", "id", "MISP License is already deactivated.");
				throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_ALREADY_DEACTIVATED.getErrorCode(),
						MISPErrorMessages.MISP_LICENSE_ALREADY_DEACTIVATED.getErrorMessage());
			}
			entity.setIsActive(false);
			entity.setUpdatedBy(getLoggedInUserId());
			entity.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
			MISPLicenseEntityV2 updatedEntity = mispLicenseV2Repository.save(entity);

			MISPDeactivateResponseDto responseDto = new MISPDeactivateResponseDto();
			responseDto.setPartnerId(updatedEntity.getId().getMispId());
			responseDto.setPolicyId(updatedEntity.getPolicyId());
			responseDto.setLicenseKeyName(updatedEntity.getLicenseKeyName());
			responseDto.setLicenseKeyStatus(INACTIVE);

			notify(MapperUtils.mapDataToPublishDtoV2(updatedEntity), EventType.MISP_LICENSE_UPDATED);

			responseWrapper.setResponse(responseDto);
		} catch (MISPServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In deactivateMISPLicense method of InfraProviderServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In deactivateMISPLicense method of InfraProviderServiceImpl - " + ex.getMessage());
			String errorCode = MISPErrorMessages.ERROR_DEACTIVATING_MISP_LICENSE.getErrorCode();
			String errorMessage = MISPErrorMessages.ERROR_DEACTIVATING_MISP_LICENSE.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(patchDeactivateMISPApiId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<MISPLicenseResponseDtoV2> regenerateMISPLicense(String partnerId, MISPRegenerateRequestDto request) {
		ResponseWrapperV2<MISPLicenseResponseDtoV2> responseWrapper = new ResponseWrapperV2<>();
		try {
			if(Objects.isNull(partnerId) || partnerId.trim().isBlank()) {
				throw new MISPServiceException(MISPErrorMessages.INVALID_PARTNER_ID.getErrorCode(),
						MISPErrorMessages.INVALID_PARTNER_ID.getErrorMessage());
			}
			String licenseKeyName = request.getLicenseKeyName();
			if(Objects.isNull(licenseKeyName) || licenseKeyName.trim().isBlank()) {
				throw new MISPServiceException(MISPErrorMessages.INVALID_LICENSE_KEY_NAME.getErrorCode(),
						MISPErrorMessages.INVALID_LICENSE_KEY_NAME.getErrorMessage());
			}
			LocalDate expiryDate = request.getExpiryDate();
			expiryDateInputValidation(expiryDate);

			// check the license key exist for given partner id and policy id combination
			List<MISPLicenseEntityV2> existingLicense = mispLicenseV2Repository.findActiveLicenseKeyByPartnerIdAndPolicyId(partnerId, request.getPolicyId());
			if(existingLicense.isEmpty()) {
				throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_NOT_FOUND.getErrorCode(),
						MISPErrorMessages.MISP_LICENSE_NOT_FOUND.getErrorMessage());
			}

			// partnerId validation
			Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
			if (partnerFromDb.isEmpty()) {
				throw new MISPServiceException(MISPErrorMessages.PARTNER_ID_NOT_EXISTS.getErrorCode(),
						MISPErrorMessages.PARTNER_ID_NOT_EXISTS.getErrorMessage());
			}
			if (!partnerFromDb.get().getIsActive()) {
				throw new MISPServiceException(MISPErrorMessages.PARTNER_NOT_ACTIVE.getErrorCode(),
						MISPErrorMessages.PARTNER_NOT_ACTIVE.getErrorMessage());
			}

			// policyId validation
			Optional<AuthPolicy> mispPolicyFromDb = validatePolicy(partnerId, request.getPolicyId());
			String policyId = mispPolicyFromDb.map(AuthPolicy::getId).orElse(null);

			// licenseKeyName validation
			List<MISPLicenseEntityV2> mispLicenseFromDb = mispLicenseV2Repository.findByPartnerIdAndPolicyIdAndLicenseKeyName(partnerId, policyId, PartnerUtil.trimAndReplace(licenseKeyName));
			if (!mispLicenseFromDb.isEmpty()) {
				throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_KEY_NAME_EXISTS.getErrorCode(),
						MISPErrorMessages.MISP_LICENSE_KEY_NAME_EXISTS.getErrorMessage());
			}

			// expiryDate validation
			if(expiryDate.isBefore(LocalDate.now()) || expiryDate.isEqual(LocalDate.now())) {
				throw new MISPServiceException(MISPErrorMessages.EXPIRYDATE_SHOULD_BE_GREATER_THAN_TODAYS_DATE.getErrorCode(),
						MISPErrorMessages.EXPIRYDATE_SHOULD_BE_GREATER_THAN_TODAYS_DATE.getErrorMessage());
			}

			MISPLicenseEntityV2 entity = new MISPLicenseEntityV2();
			MISPLicenseEntityPK pk = new MISPLicenseEntityPK();
			pk.setMispId(partnerId);
			pk.setLicenseKey(generateLicenseKey());
			entity.setId(pk);
			entity.setLicenseKeyName(licenseKeyName);
			entity.setValidFromDate(LocalDateTime.now(ZoneId.of("UTC")));
			// Get current UTC time
			LocalTime currentUtcTime = LocalTime.now(ZoneOffset.UTC);
			entity.setValidToDate(LocalDateTime.of(expiryDate, currentUtcTime));
			entity.setCreatedBy(getLoggedInUserId());
			entity.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
			entity.setIsActive(true);
			entity.setIsDeleted(false);
			entity.setPolicyId(policyId);
			mispLicenseV2Repository.save(entity);

			MISPLicenseResponseDtoV2 responseDtoV2 = new MISPLicenseResponseDtoV2();
			responseDtoV2.setLicenseKey(entity.getId().getLicenseKey());
			responseDtoV2.setExpiryDateTime(entity.getValidToDate());
			responseDtoV2.setLicenseKeyName(entity.getLicenseKeyName());
			responseDtoV2.setLicenseKeyStatus(ACTIVE);
			responseDtoV2.setPartnerId(entity.getId().getMispId());
			responseDtoV2.setPolicyId(entity.getPolicyId());
			if(mispPolicyFromDb.isPresent()) {
				notify(MapperUtils.mapDataToPublishDtoV2(entity), MapperUtils.mapPolicyToPublishDto(mispPolicyFromDb.get(),
						getPolicyObject(mispPolicyFromDb.get().getPolicyFileId())), EventType.MISP_LICENSE_UPDATED);
			}
			else {
				notify(MapperUtils.mapDataToPublishDtoV2(entity), null, EventType.MISP_LICENSE_UPDATED);
			}
			responseWrapper.setResponse(responseDtoV2);
		} catch (MISPServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In regenerateMISPLicense method of InfraProviderServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In regenerateMISPLicense method of InfraProviderServiceImpl - " + ex.getMessage());
			String errorCode = MISPErrorMessages.ERROR_REGENERATING_MISP_LICENSE.getErrorCode();
			String errorMessage = MISPErrorMessages.ERROR_REGENERATING_MISP_LICENSE.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(putRegenerateMISPApiId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}
}
