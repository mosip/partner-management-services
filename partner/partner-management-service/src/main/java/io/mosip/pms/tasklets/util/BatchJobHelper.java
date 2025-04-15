package io.mosip.pms.tasklets.util;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;

@Component
public class BatchJobHelper {

	private Logger log = PMSLogger.getLogger(BatchJobHelper.class);

	@Value("#{'${mosip.pms.batch.job.skips.partner.ids}'.split(',')}")
	private List<String> skipPartnerIds;

	@Autowired
	PartnerServiceRepository partnerRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	NotificationServiceRepository notificationServiceRepository;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	public boolean validatePartnerId(Optional<Partner> partnerById) {
		if (partnerById.isEmpty()) {
			return false;
		} else {
			return true;

		}
	}

	public Optional<Partner> getPartnerById(String partnerId) {
		Optional<Partner> partnerById = partnerRepository.findById(partnerId);
		return partnerById;
	}

	public List<Partner> getAllActiveNonAdminPartners(List<Partner> pmsPartnerAdmins) {
		log.info("As per configuration, number of partners for which notifications are to be skipped is {}",
				skipPartnerIds.size());
		List<Partner> partnersList = partnerRepository.findAllByIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue();
		List<Partner> nonAdminPartnersList = new ArrayList<Partner>();
		partnersList.forEach(partner -> {
			List<String> foundList = new ArrayList<String>();
			pmsPartnerAdmins.forEach(pmsPartnerAdmin -> {
				if (pmsPartnerAdmin.getId().equals(partner.getId())) {
					foundList.add(pmsPartnerAdmin.getId());
				}
			});
			if (foundList.size() == 0 && !skipPartnerIds.contains(partner.getId())) {
				// if (foundList.size() == 0 && partner.getId().contains("mayurad")) {
				nonAdminPartnersList.add(partner);
			}
		});
		return nonAdminPartnersList;
	}

	public List<Partner> getValidPartnerAdminsInPms(List<String> keycloakPartnerAdmins) {
		log.info("As per configuration, number of partners for which notifications are to be skipped is {}",
				skipPartnerIds.size());
		List<Partner> pmsPartnerAdmins = new ArrayList<Partner>();
		keycloakPartnerAdmins.forEach(keycloakPartnerAdminId -> {
			Optional<Partner> partnerAdminDetails = getPartnerById(keycloakPartnerAdminId);
			if (validatePartnerId(partnerAdminDetails)) {
				if (!skipPartnerIds.contains(partnerAdminDetails.get().getId())) {
					pmsPartnerAdmins.add(partnerAdminDetails.get());
				}
			} else {
				log.debug("this partner admin is not a valid user in PMS, {}", keycloakPartnerAdminId);
			}
		});
		return pmsPartnerAdmins;
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
			log.error("Could not decode the certificate data :" + ex.getMessage());
			throw new BatchJobServiceException(ErrorCode.UNABLE_TO_DECODE_CERTIFICATE.getErrorCode(),
					ErrorCode.UNABLE_TO_DECODE_CERTIFICATE.getErrorMessage());
		}
		return cert;
	}

	public NotificationEntity saveCertificateExpiryNotification(String certificateType, Partner partnerDetails,
			List<CertificateDetailsDto> certificateDetailsList) throws BatchJobServiceException {
		try {
			NotificationDetailsDto notificationDetailsDto = new NotificationDetailsDto();
			notificationDetailsDto.setCertificateDetails(certificateDetailsList);
			String id = UUID.randomUUID().toString();
			NotificationEntity notification = new NotificationEntity();
			notification.setId(id);
			notification.setPartnerId(partnerDetails.getId());
			notification.setNotificationType(getNotificationType(certificateType));
			notification.setNotificationStatus(PartnerConstants.STATUS_ACTIVE);
			notification.setEmailId(keyManagerHelper.encryptData(partnerDetails.getEmailId()));
			notification.setEmailLangCode(partnerDetails.getLangCode());
			notification.setEmailSent(false);
			notification.setCreatedBy(PartnerConstants.SYSTEM_USER);
			notification.setCreatedDatetime(LocalDateTime.now(ZoneId.of("UTC")));
			notification.setNotificationDetailsJson(objectMapper.writeValueAsString(notificationDetailsDto));
			log.info("saving notifications, {}", notification);
			NotificationEntity savedNotification = notificationServiceRepository.save(notification);
			return savedNotification;
		} catch (JsonProcessingException jpe) {
			log.error("Error creating the notification: {}", jpe.getMessage());
			throw new BatchJobServiceException(ErrorCode.NOTIFICATION_CREATE_ERROR.getErrorCode(),
					ErrorCode.NOTIFICATION_CREATE_ERROR.getErrorMessage());
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
		case PartnerConstants.WEEKLY:
			return PartnerConstants.WEEKLY_SUMMARY;
		default:
			throw new BatchJobServiceException(ErrorCode.INVALID_CERTIFICATE_TYPE.getErrorCode(),
					ErrorCode.INVALID_CERTIFICATE_TYPE.getErrorMessage());
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