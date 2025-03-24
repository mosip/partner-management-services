package io.mosip.pms.batchjob.util;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;

@Component
public class BatchJobHelper {

	private static final Logger LOGGER = LoggerConfiguration.logConfig(BatchJobHelper.class);

	@Value("#{'${mosip.pms.batch.job.skips.partner.ids}'.split(',')}")
	private List<String> skipPartnerIds;
	
	@Autowired
	PartnerServiceRepository partnerRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	NotificationServiceRepository notificationServiceRepository;

	public boolean validateActivePartnerId(Optional<Partner> partnerById) {
		if (partnerById.isEmpty()) {
			return false;
		} else {
			if (!partnerById.get().getIsActive()) {
				return false;
			} else {
				return true;
			}
		}
	}

	public Optional<Partner> getPartnerById(String partnerId) {
		Optional<Partner> partnerById = partnerRepository.findById(partnerId);
		return partnerById;
	}

	public List<Partner> getAllActiveNonAdminPartners(List<String> keycloakPartnerAdmins) {
		LOGGER.info("Skipping number of partners {}", skipPartnerIds.size());
		List<Partner> partnersList = partnerRepository.findAllByIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue();
		List<Partner> nonAdminPartnersList = new ArrayList<Partner>();
		partnersList.forEach(partner -> {
			List<String> foundList = new ArrayList<String>();
			keycloakPartnerAdmins.forEach(keycloakPartnerAdmin -> {
				if (keycloakPartnerAdmin.equals(partner.getId())) {
					foundList.add(keycloakPartnerAdmin);
				}
			});
			if (foundList.size() == 0 && !skipPartnerIds.contains(partner.getId())) {
			//if (foundList.size() == 0 && partner.getId().contains("mayurad")) {	
				nonAdminPartnersList.add(partner);
			}
		});
		return nonAdminPartnersList;
	}

	public List<Partner> getValidPartnerAdminsInPms(List<String> keycloakPartnerAdmins) {
		LOGGER.info("Skipping number of partners {}", skipPartnerIds.size());
		List<Partner> pmsPartnerAdmins = new ArrayList<Partner>();
		keycloakPartnerAdmins.forEach(keycloakPartnerAdminId -> {
			Optional<Partner> partnerAdminDetails = getPartnerById(keycloakPartnerAdminId);
			if (validateActivePartnerId(partnerAdminDetails)) {
				if (!skipPartnerIds.contains(partnerAdminDetails.get().getId())) {
					pmsPartnerAdmins.add(partnerAdminDetails.get());
				} 
			} else {
				LOGGER.debug("this partner admin is not active or valid in PMS, {}", keycloakPartnerAdminId);
			}
		});
		return pmsPartnerAdmins;
	}

	public void validateApiResponse(Map<String, Object> response, String apiUrl) {
		if (response == null) {
			LOGGER.error("Received null response from API: {}", apiUrl);
			throw new BatchJobServiceException(ErrorCodes.API_NULL_RESPONSE.getCode(),
					ErrorCodes.API_NULL_RESPONSE.getMessage());
		}
		if (response.containsKey(PartnerConstants.ERRORS)) {
			List<Map<String, Object>> errorList = (List<Map<String, Object>>) response.get(PartnerConstants.ERRORS);
			if (errorList != null && !errorList.isEmpty()) {
				LOGGER.error("Error occurred while fetching data: {}", errorList);
				throw new BatchJobServiceException(String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORCODE)),
						String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORMESSAGE)));
			}
		}
		if (!response.containsKey(PartnerConstants.RESPONSE) || response.get(PartnerConstants.RESPONSE) == null) {
			LOGGER.error("Missing response data in API call: {}", apiUrl);
			throw new BatchJobServiceException(ErrorCodes.API_NULL_RESPONSE.getCode(),
					ErrorCodes.API_NULL_RESPONSE.getMessage());
		}
	}

	public X509Certificate decodeCertificateData(String certificateData) {
		certificateData = certificateData.replaceAll(PartnerConstants.BEGIN_CERTIFICATE, "")
				.replaceAll(PartnerConstants.END_CERTIFICATE, "").replaceAll("\n", "");
		X509Certificate cert = null;
		try {
			byte[] decodedCertificate = Base64.getDecoder().decode(certificateData);

			CertificateFactory certificateFactory = CertificateFactory.getInstance(PartnerConstants.X509);
			cert = (X509Certificate) certificateFactory
					.generateCertificate(new ByteArrayInputStream(decodedCertificate));
		} catch (Exception ex) {
			LOGGER.error("Could not decode the certificate data :" + ex.getMessage());
			throw new BatchJobServiceException(ErrorCodes.UNABLE_TO_DECODE_CERTIFICATE.getCode(),
					ErrorCodes.UNABLE_TO_DECODE_CERTIFICATE.getMessage());
		}
		return cert;
	}

	public NotificationEntity saveCertificateExpiryNotification(String certificateType, int expiryPeriod,
			Partner partnerDetails, List<CertificateDetailsDto> certificateDetailsList)
			throws BatchJobServiceException {
		try {
			NotificationDetailsDto notificationDetailsDto = new NotificationDetailsDto();
			notificationDetailsDto.setCertificateDetails(certificateDetailsList);
			String id = UUID.randomUUID().toString();
			NotificationEntity notification = new NotificationEntity();
			notification.setId(id);
			notification.setPartnerId(partnerDetails.getId());
			notification.setNotificationType(getNotificationType(certificateType));
			notification.setNotificationStatus(PartnerConstants.STATUS_ACTIVE);
			notification.setEmailId(partnerDetails.getEmailId());
			notification.setEmailLangCode(partnerDetails.getLangCode());
			notification.setEmailSent(false);
			notification.setCreatedBy(PartnerConstants.SYSTEM_USER);
			notification.setCreatedDatetime(LocalDateTime.now(ZoneId.of("UTC")));
			notification.setNotificationDetailsJson(objectMapper.writeValueAsString(notificationDetailsDto));
			LOGGER.info("saving notifications, {}", notification);
			NotificationEntity savedNotification = notificationServiceRepository.save(notification);
			return savedNotification;
		} catch (JsonProcessingException jpe) {
			LOGGER.error("Error creating the notification: {}", jpe.getMessage());
			throw new BatchJobServiceException(ErrorCodes.NOTIFICATION_CREATE_ERROR.getCode(),
					ErrorCodes.NOTIFICATION_CREATE_ERROR.getMessage());
		}
	}

	public String getNotificationType(String certificateType) throws BatchJobServiceException {
		switch (certificateType) {
		case PartnerConstants.ROOT:
			return PartnerConstants.ROOT_CERT_EXPIRY;
		case PartnerConstants.INTERMEDIATE:
			return PartnerConstants.INTERMEDIATE_CERT_EXPIRY;
		case PartnerConstants.PARTNER:
			return PartnerConstants.PARTNER_CERT_EXPIRY;
		case PartnerConstants.WEEKLY_SUMMARY:
			return PartnerConstants.WEEKLY_SUMMARY;
		default:
			throw new BatchJobServiceException(ErrorCodes.INVALID_CERTIFICATE_TYPE.getCode(),
					ErrorCodes.INVALID_CERTIFICATE_TYPE.getMessage());
		}
	}
	
	public String getPartnerDomain(String partnerType) throws BatchJobServiceException {
		switch (partnerType) {
		case PartnerConstants.DEVICE_PROVIDER_PARTNER_TYPE:
			return PartnerConstants.PARTNER_DOMAIN_DEVICE;
		case PartnerConstants.FTM_PROVIDER_PARTNER_TYPE:
			return PartnerConstants.PARTNER_DOMAIN_FTM;
		default:
			return PartnerConstants.PARTNER_DOMAIN_AUTH;
		}
	}
}