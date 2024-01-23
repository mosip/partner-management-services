package io.mosip.pms.partner.misp.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
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
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.entity.Partner;

import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.MispLicenseRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.UserDetailUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.response.dto.ColumnCodeValue;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.exception.MISPErrorMessages;
import io.mosip.pms.partner.misp.exception.MISPServiceException;
import io.mosip.pms.partner.misp.service.InfraServiceProviderService;

@Component
public class InfraProviderServiceImpl implements InfraServiceProviderService {

	@Value("${mosip.kernel.idgenerator.misp.license-key-length}")
	private int licenseKeyLength;

	@Value("${mosip.pmp.misp.license.expiry.period.indays}")
	private int mispLicenseExpiryInDays;

	@Autowired
	MispLicenseRepository mispLicenseRepository;

	@Autowired
	PartnerServiceRepository partnerRepository;

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

		MISPLicenseEntity mispLicenseFromDb = mispLicenseRepository.findByIdAndKey(id, licenseKey);
		if (mispLicenseFromDb == null) {
			throw new MISPServiceException(MISPErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorCode(),
					MISPErrorMessages.MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID.getErrorMessage());
		}
		mispLicenseFromDb.setUpdatedBy(getLoggedInUserId());
		mispLicenseFromDb.setUpdatedDateTime(LocalDateTime.now());
		mispLicenseFromDb.setIsActive(status.toLowerCase().equals(ACTIVE_STATUS) ? true : false);
		mispLicenseRepository.save(mispLicenseFromDb);
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		response.setLicenseKey(mispLicenseFromDb.getLicenseKey());
		response.setLicenseKeyExpiry(mispLicenseFromDb.getValidToDate());
		response.setLicenseKeyStatus(mispLicenseFromDb.getIsActive() ? ACTIVE_STATUS : NOTACTIVE_STATUS);
		response.setProviderId(mispLicenseFromDb.getMispId());
		notify(MapperUtils.mapDataToPublishDto(mispLicenseFromDb), EventType.MISP_LICENSE_UPDATED);
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
		if(!approvedPolicyMappedReq.isEmpty() && !mispPolicy.getPolicyId().isBlank()) {
			mispPolicy= approvedPolicyMappedReq.get(0);
			policyId = mispPolicy.getId();
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
}
