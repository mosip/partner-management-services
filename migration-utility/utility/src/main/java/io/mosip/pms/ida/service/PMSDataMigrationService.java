package io.mosip.pms.ida.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.ida.constant.EventType;
import io.mosip.pms.ida.dao.AuthPolicy;
import io.mosip.pms.ida.dao.AuthPolicyRepository;
import io.mosip.pms.ida.dao.MISPLicenseEntity;
import io.mosip.pms.ida.dao.MispLicenseRepository;
import io.mosip.pms.ida.dao.Partner;
import io.mosip.pms.ida.dao.PartnerPolicy;
import io.mosip.pms.ida.dao.PartnerPolicyRepository;
import io.mosip.pms.ida.dao.PartnerRepository;
import io.mosip.pms.ida.dto.APIKeyDataPublishDto;
import io.mosip.pms.ida.dto.MISPDataPublishDto;
import io.mosip.pms.ida.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.ida.dto.PartnerDataPublishDto;
import io.mosip.pms.ida.dto.PolicyPublishDto;
import io.mosip.pms.ida.dto.Type;
import io.mosip.pms.ida.util.MapperUtils;
import io.mosip.pms.ida.util.RestUtil;
import io.mosip.pms.ida.util.UtilityLogger;
import io.mosip.pms.ida.websub.WebSubPublisher;

@Service
public class PMSDataMigrationService {

	private static final Logger LOGGER = UtilityLogger.getLogger(PMSDataMigrationService.class);

	@Autowired
	RestUtil restUtil;

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;

	@Autowired
	PartnerRepository partnerRepository;

	@Autowired
	AuthPolicyRepository authPolicyRepository;

	@Autowired
	MispLicenseRepository mispLicenseRepository;

	@Autowired
	private Environment environment;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WebSubPublisher webSubPublisher;

	public void initialize() {
		LOGGER.error("Started publishing the data");
		try {
			publishAPIKeyData();
			publishMISPLicenseData();
		} catch (Exception e) {
			LOGGER.error("Error occurred while publishing the data");
		}
	}

	public void publishAPIKeyData() throws Exception {
		List<PartnerPolicy> allApprovedPolicies = partnerPolicyRepository.findAll();
		for (PartnerPolicy partnerPolicy : allApprovedPolicies) {
			LOGGER.info("Publishing the data for label :: " + partnerPolicy.getLabel() + " partner :: "
					+ partnerPolicy.getPartner().getId() + "policy :: " + partnerPolicy.getPolicyId());
			Optional<Partner> partnerFromDb = partnerRepository.findById(partnerPolicy.getPartner().getId());
			Optional<AuthPolicy> validPolicy = authPolicyRepository.findById(partnerPolicy.getPolicyId());
			notify(MapperUtils.mapDataToPublishDto(partnerFromDb.get(),
					getPartnerCertificate(partnerFromDb.get().getCertificateAlias())),
					MapperUtils.mapPolicyToPublishDto(validPolicy.get(),
							getPolicyObject(validPolicy.get().getPolicyFileId())),
					MapperUtils.mapKeyDataToPublishDto(partnerPolicy), EventType.APIKEY_APPROVED);
			LOGGER.info("Published the data for label :: " + partnerPolicy.getLabel() + " partner :: "
					+ partnerPolicy.getPartner().getId() + "policy :: " + partnerPolicy.getPolicyId());

		}
	}

	public void publishMISPLicenseData() {
		List<MISPLicenseEntity> mispLicenseFromDb = mispLicenseRepository.findAll();
		for (MISPLicenseEntity mispLicenseEntity : mispLicenseFromDb) {
			LOGGER.info("Publishing the data for MISPID :: " + mispLicenseEntity.getMispId());
			notify(MapperUtils.mapDataToPublishDto(mispLicenseEntity), EventType.MISP_LICENSE_GENERATED);
			LOGGER.info("Published the data for MISPID :: " + mispLicenseEntity.getMispId());
		}
	}

	private void notify(MISPDataPublishDto dataToPublish, EventType eventType) {
		Type type = new Type();
		type.setName("InfraProviderServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.InfraProviderServiceImpl");
		Map<String, Object> data = new HashMap<>();
		data.put("mispLicenseData", dataToPublish);
		webSubPublisher.notify(eventType, data, type);
	}

	private String getPartnerCertificate(String certificateAlias) throws Exception {
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerCertId", certificateAlias);
		Map<String, Object> getApiResponse = restUtil
				.getApi(environment.getProperty("pmp.partner.certificaticate.get.rest.uri"), pathsegments, Map.class);
		PartnerCertDownloadResponeDto responseObject = null;
		try {
			responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")),
					PartnerCertDownloadResponeDto.class);
		} catch (IOException e) {
			LOGGER.error("Error occured while parsing the response ", e);
		}
		if (responseObject == null && getApiResponse.containsKey("errors")) {

		}
		if (responseObject == null) {
			LOGGER.error("Response is null ");
		}

		return responseObject.getCertificateData();
	}

	private void notify(PartnerDataPublishDto partnerDataToPublish, PolicyPublishDto policyDataToPublish,
			APIKeyDataPublishDto apiKeyDataToPublish, EventType eventType) {
		Map<String, Object> data = new HashMap<>();
		if (partnerDataToPublish != null) {
			data.put("partnerData", partnerDataToPublish);
		}
		if (policyDataToPublish != null) {
			data.put("policyData", policyDataToPublish);
		}
		if (apiKeyDataToPublish != null) {
			data.put("apiKeyData", apiKeyDataToPublish);
		}
		notify(data, eventType);
	}

	private void notify(Map<String, Object> data, EventType eventType) {
		Type type = new Type();
		type.setName("PMSDataMigrationService");
		type.setNamespace("PMSDataMigrationService");
		webSubPublisher.notify(eventType, data, type);
	}

	private JSONObject getPolicyObject(String policy) {
		JSONParser parser = new JSONParser();
		try {
			return ((JSONObject) parser.parse(policy));
		} catch (ParseException e) {
			LOGGER.error("Error occurred while parsing the policy file", e.getMessage());
		}
		return null;
	}
}
