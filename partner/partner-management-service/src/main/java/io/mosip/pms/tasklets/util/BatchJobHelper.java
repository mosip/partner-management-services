package io.mosip.pms.tasklets.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.partner.dto.AdminDetailsDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.response.dto.FtmCertificateDownloadResponseDto;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.MISPLicenseEntityV2;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.ApiKeyDetailsDto;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.FtmDetailsDto;
import io.mosip.pms.common.dto.MISPLicenseKeyDetailsDto;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.common.dto.SbiDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;
import io.mosip.pms.partner.manager.constant.AuditConstant;
import io.mosip.pms.partner.manager.constant.ErrorCode;

@Component
public class BatchJobHelper {

	private Logger log = PMSLogger.getLogger(BatchJobHelper.class);

	@Autowired
	AuditUtil auditUtil;

	@Value("${mosip.pms.batch.job.skips.partner.ids:}")
	private String skipPartnerIdsConfig;

	private Set<String> skipPartnerIds = Collections.emptySet();

	@Value("#{'${mosip.pms.batch.job.partner.cert.expiry.periods}'.split(',')}")
	private List<Integer> partnerCertExpiryPeriods;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	AuthPolicyRepository authPolicyRepository;

	@Autowired
	PartnerServiceRepository partnerRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	NotificationServiceRepository notificationServiceRepository;

	@PostConstruct
	public void initSkipPartnerIds() {
		if (skipPartnerIdsConfig != null && !skipPartnerIdsConfig.trim().isEmpty()) {
			skipPartnerIds = Arrays.stream(skipPartnerIdsConfig.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.collect(Collectors.toSet());
		}
	}

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

	public List<Partner> getAllActiveNonAdminPartners(List<AdminDetailsDto> partnerAdmins) {
		log.info("As per configuration, number of partners for which notifications are to be skipped is {}",
				skipPartnerIds.size());
		List<Partner> partnersList = partnerRepository.findAllByIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue();
		List<Partner> nonAdminPartnersList = new ArrayList<Partner>();
		partnersList.forEach(partner -> {
			List<String> foundList = new ArrayList<String>();
			partnerAdmins.forEach(partnerAdmin -> {
				if (partnerAdmin.getUserName().equals(partner.getId())) {
					foundList.add(partnerAdmin.getUserName());
				}
			});
			if (foundList.size() == 0 && !skipPartnerIds.contains(partner.getId())) {
				// if (foundList.size() == 0 && partner.getId().contains("mayurad")) {
				nonAdminPartnersList.add(partner);
			}
		});
		return nonAdminPartnersList;
	}

	public List<Partner> getAllActiveAndApprovedPartners(String partnerType) {
		log.info("As per configuration, number of partners for which notifications are to be skipped is {}",
				skipPartnerIds.size());
		List<Partner> allPartnersList = partnerRepository.findAllApprovedActivePartnersByPartnerTypeCode(partnerType);
		List<Partner> partnersList = new ArrayList<Partner>();
		allPartnersList.forEach(partner -> {
			if (!skipPartnerIds.contains(partner.getId())) {
				// if (partner.getId().contains("mayurad")) {
				partnersList.add(partner);
			}
		});
		return partnersList;
	}

	public List<AdminDetailsDto> getValidPartnerAdmins(List<AdminDetailsDto> keycloakPartnerAdmins) {
		log.info("As per configuration, number of partners for which notifications are to be skipped is {}",
				skipPartnerIds.size());
		List<AdminDetailsDto> validPartnerAdmins = new ArrayList<AdminDetailsDto>();
		keycloakPartnerAdmins.forEach(keycloakPartnerAdmin -> {
			if (!skipPartnerIds.contains(keycloakPartnerAdmin.getUserName())) {
				validPartnerAdmins.add(keycloakPartnerAdmin);
			}
		});
		return validPartnerAdmins;
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

	public NotificationEntity saveNotification(String notificationType, String partnerId, String emailLangCode,
			List<CertificateDetailsDto> certificateDetailsList, List<FtmDetailsDto> ftmDetailsList,
			List<SbiDetailsDto> sbiList, List<ApiKeyDetailsDto> apiKeyList, List<MISPLicenseKeyDetailsDto> mispLicenseKeyList, String decryptedEmailId)
			throws BatchJobServiceException {
		try {
			NotificationDetailsDto notificationDetailsDto = new NotificationDetailsDto();
			notificationDetailsDto.setCertificateDetails(certificateDetailsList);
			notificationDetailsDto.setFtmDetails(ftmDetailsList);
			notificationDetailsDto.setSbiDetails(sbiList);
			notificationDetailsDto.setApiKeyDetails(apiKeyList);
			notificationDetailsDto.setMispLicenseKeyDetails(mispLicenseKeyList);

			String id = UUID.randomUUID().toString();
			NotificationEntity notification = new NotificationEntity();
			notification.setId(id);
			notification.setPartnerId(partnerId);
			notification.setNotificationType(notificationType);
			notification.setNotificationStatus(PartnerConstants.STATUS_ACTIVE);
			notification.setEmailId(keyManagerHelper.encryptData(decryptedEmailId));
			notification.setEmailLangCode(emailLangCode);
			notification.setEmailSent(false);
			notification.setCreatedBy(PartnerConstants.SYSTEM_USER);
			notification.setCreatedDatetime(LocalDateTime.now(ZoneId.of("UTC")));
			notification.setNotificationDetailsJson(objectMapper.writeValueAsString(notificationDetailsDto));
			log.info("saving notifications, {}", notification);
			NotificationEntity savedNotification = notificationServiceRepository.save(notification);
			auditUtil.setAuditRequestDto(getAuditLogEventTypeForNotification(notificationType, true), id,
					"notificationId", AuditConstant.AUDIT_SYSTEM);
			return savedNotification;
		} catch (Exception e) {
			auditUtil.setAuditRequestDto(getAuditLogEventTypeForNotification(notificationType, false), "failure",
					"notificationId", AuditConstant.AUDIT_SYSTEM);
			log.error("Error creating the notification: {}", e.getMessage());
			throw new BatchJobServiceException(ErrorCode.NOTIFICATION_CREATE_ERROR.getErrorCode(),
					ErrorCode.NOTIFICATION_CREATE_ERROR.getErrorMessage());
		}
	}

	public PartnerServiceAuditEnum getAuditLogEventTypeForNotification(String notificationType, boolean forSuccess)
			throws BatchJobServiceException {
		switch (notificationType) {
		case PartnerConstants.ROOT_CERT_EXPIRY:
			return forSuccess ? PartnerServiceAuditEnum.ROOT_CERTIFICATE_EXPIRY_NOTIFICATION_SUCCESS
					: PartnerServiceAuditEnum.ROOT_CERTIFICATE_EXPIRY_NOTIFICATION_FAILURE;
		case PartnerConstants.INTERMEDIATE_CERT_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.INTERMEDIATE_CERTIFICATE_EXPIRY_NOTIFICATION_SUCCESS
					: PartnerServiceAuditEnum.INTERMEDIATE_CERTIFICATE_EXPIRY_NOTIFICATION_FAILURE;
		case PartnerConstants.PARTNER_CERT_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.PARTNER_CERTIFICATE_EXPIRY_NOTIFICATION_SUCCESS
					: PartnerServiceAuditEnum.PARTNER_CERTIFICATE_EXPIRY_NOTIFICATION_FAILURE;
		case PartnerConstants.WEEKLY_SUMMARY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.WEEKLY_SUMMARY_NOTIFICATION_SUCCESS
					: PartnerServiceAuditEnum.WEEKLY_SUMMARY_NOTIFICATION_FAILURE;
		case PartnerConstants.FTM_CHIP_CERT_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.FTM_CHIP_CERTIFICATE_EXPIRY_NOTIFICATION_SUCCESS
					: PartnerServiceAuditEnum.FTM_CHIP_CERTIFICATE_EXPIRY_NOTIFICATION_FAILURE;
		case PartnerConstants.SBI_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.SBI_EXPIRY_NOTIFICATION_SUCCESS
					: PartnerServiceAuditEnum.SBI_EXPIRY_NOTIFICATION_FAILURE;
		case PartnerConstants.API_KEY_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.API_KEY_EXPIRY_NOTIFICATION_SUCCESS
					: PartnerServiceAuditEnum.API_KEY_EXPIRY_NOTIFICATION_FAILURE;
			case PartnerConstants.MISP_LICENSE_KEY_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.MISP_LICENSE_KEY_EXPIRY_NOTIFICATION_SUCCESS
					: PartnerServiceAuditEnum.MISP_LICENSE_KEY_EXPIRY_NOTIFICATION_FAILURE;
		default:
			throw new BatchJobServiceException(ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorCode(),
					ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorMessage());
		}
	}

	public PartnerServiceAuditEnum getAuditLogEventTypeForEmail(String notificationType, boolean forSuccess)
			throws BatchJobServiceException {
		switch (notificationType) {
		case PartnerConstants.ROOT_CERT_EXPIRY:
			return forSuccess ? PartnerServiceAuditEnum.ROOT_CERTIFICATE_EXPIRY_NOTIFICATION_EMAIL_SUCCESS
					: PartnerServiceAuditEnum.ROOT_CERTIFICATE_EXPIRY_NOTIFICATION_EMAIL_FAILURE;
		case PartnerConstants.INTERMEDIATE_CERT_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.INTERMEDIATE_CERTIFICATE_EXPIRY_NOTIFICATION_EMAIL_SUCCESS
					: PartnerServiceAuditEnum.INTERMEDIATE_CERTIFICATE_EXPIRY_NOTIFICATION_EMAIL_FAILURE;
		case PartnerConstants.PARTNER_CERT_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.PARTNER_CERTIFICATE_EXPIRY_NOTIFICATION_EMAIL_SUCCESS
					: PartnerServiceAuditEnum.PARTNER_CERTIFICATE_EXPIRY_NOTIFICATION_EMAIL_FAILURE;
		case PartnerConstants.WEEKLY_SUMMARY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.WEEKLY_SUMMARY_NOTIFICATION_EMAIL_SUCCESS
					: PartnerServiceAuditEnum.WEEKLY_SUMMARY_NOTIFICATION_EMAIL_FAILURE;
		case PartnerConstants.FTM_CHIP_CERT_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.FTM_CHIP_CERTIFICATE_EXPIRY_NOTIFICATION_EMAIL_SUCCESS
					: PartnerServiceAuditEnum.FTM_CHIP_CERTIFICATE_EXPIRY_NOTIFICATION_EMAIL_FAILURE;
		case PartnerConstants.SBI_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.SBI_EXPIRY_NOTIFICATION_EMAIL_SUCCESS
					: PartnerServiceAuditEnum.SBI_EXPIRY_NOTIFICATION_EMAIL_FAILURE;
		case PartnerConstants.API_KEY_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.API_KEY_EXPIRY_NOTIFICATION_EMAIL_SUCCESS
					: PartnerServiceAuditEnum.API_KEY_EXPIRY_NOTIFICATION_EMAIL_FAILURE;
			case PartnerConstants.MISP_LICENSE_KEY_EXPIRY_NOTIFICATION_TYPE:
			return forSuccess ? PartnerServiceAuditEnum.MISP_LICENSE_KEY_EXPIRY_NOTIFICATION_EMAIL_SUCCESS
					: PartnerServiceAuditEnum.MISP_LICENSE_KEY_EXPIRY_NOTIFICATION_EMAIL_FAILURE;
		default:
			throw new BatchJobServiceException(ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorCode(),
					ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorMessage());
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

	public X509Certificate getDecodedCertificate(Partner pmsPartner) {
		X509Certificate decodedPartnerCertificate = null;
		if (pmsPartner.getCertificateAlias() != null) {
			try {
				PartnerCertDownloadResponeDto certResp = keyManagerHelper
						.getPartnerCertificate(pmsPartner.getCertificateAlias());

				decodedPartnerCertificate = decodeCertificateData(certResp.getCertificateData());
			} catch (Exception e) {
				log.debug("Error occurred while fetching certificate for : {}", pmsPartner.getId());
			}
		}
		return decodedPartnerCertificate;
	}

	public boolean checkIfExpiring(Partner pmsPartner, LocalDateTime expiryDateTime, Integer expiryPeriod,
								   boolean withinExpiryPeriod) {
		boolean isExpiring = false;
		LocalDate todayDate = LocalDate.now(ZoneId.of("UTC"));
		LocalDate validTillDate = todayDate.plusDays(expiryPeriod);
		LocalTime validTillMinTime = LocalTime.MIN;
		LocalDateTime validTillMinDateTime = LocalDateTime.of(validTillDate, validTillMinTime);
		if (withinExpiryPeriod) {
			validTillMinDateTime = LocalDateTime.of(todayDate, validTillMinTime);
		}
		LocalTime validTillMaxTime = LocalTime.MAX;
		LocalDateTime validTillMaxDateTime = LocalDateTime.of(validTillDate, validTillMaxTime);
		log.debug("validTillMinDateTime {}", validTillMinDateTime);
		log.debug("validTillMaxDateTime {}", validTillMaxDateTime);

		// Check if the certificate has expired
		if (expiryDateTime.isAfter(validTillMinDateTime) && expiryDateTime.isBefore(validTillMaxDateTime)) {
			log.debug("For partner id {}",
					pmsPartner.getId() + "" + ", it is expiring after " + expiryPeriod + " days.");
			isExpiring = true;
		}
		return isExpiring;
	}

	public CertificateDetailsDto populateCertificateDetails(int expiryPeriod, Partner partner,
															X509Certificate expiringCertificate) {
		CertificateDetailsDto certificateDetails = new CertificateDetailsDto();
		certificateDetails.setCertificateId(partner.getCertificateAlias());
		certificateDetails.setIssuedBy(expiringCertificate.getIssuerX500Principal().getName());
		certificateDetails.setIssuedTo(expiringCertificate.getSubjectX500Principal().getName());
		certificateDetails.setPartnerDomain(getPartnerDomain(partner.getPartnerTypeCode()));
		certificateDetails.setPartnerId(partner.getId());
		certificateDetails.setCertificateType(PartnerConstants.PARTNER);
		certificateDetails.setExpiryDateTime(getCertificateExpiryDateTime(expiringCertificate).toString());
		certificateDetails.setExpiryPeriod("" + expiryPeriod);
		return certificateDetails;
	}

	public LocalDateTime getCertificateExpiryDateTime(X509Certificate decodedCert) {
		return decodedCert.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
	}

	public X509Certificate getDecodedFtmCertificate(FTPChipDetail ftpChipDetail) {
		X509Certificate decodedCaSignedCert = null;
		try {
			log.info("Fetching FTM chip certificate for FTM provider id {}", ftpChipDetail.getFtpChipDetailId());
			FtmCertificateDownloadResponseDto certResponse = partnerHelper.getCertificate(
					ftpChipDetail.getCertificateAlias(), PartnerConstants.GET_SIGNED_PARTNER_CERT_URL,
					FtmCertificateDownloadResponseDto.class);
			log.info("FTM chip certificate is available for {}", ftpChipDetail.getFtpChipDetailId());
			decodedCaSignedCert = MultiPartnerUtil.decodeCertificateData(certResponse.getCaSignedCertificateData());
		} catch (ApiAccessibleException ai) {
			log.debug(ai.getMessage());
		} catch (PartnerServiceException pse) {
			log.debug(pse.getMessage());
		} catch (JsonProcessingException jpe) {
			log.debug(jpe.getMessage());
		}
		return decodedCaSignedCert;
	}

	public FtmDetailsDto populateFtmDetails(int expiryPeriod, FTPChipDetail ftpChipDetail,
											X509Certificate expiringCertificate) {
		FtmDetailsDto ftmDetailsDto = new FtmDetailsDto();
		ftmDetailsDto.setCertificateId(ftpChipDetail.getCertificateAlias());
		ftmDetailsDto.setIssuedBy(expiringCertificate.getIssuerX500Principal().getName());
		ftmDetailsDto.setIssuedTo(expiringCertificate.getSubjectX500Principal().getName());
		ftmDetailsDto.setPartnerDomain(getPartnerDomain(PartnerConstants.FTM_PROVIDER_PARTNER_TYPE));
		ftmDetailsDto.setPartnerId(ftpChipDetail.getFtpProviderId());
		ftmDetailsDto.setCertificateType(PartnerConstants.FTM);
		ftmDetailsDto.setExpiryDateTime(this.getCertificateExpiryDateTime(expiringCertificate).toString());
		ftmDetailsDto.setExpiryPeriod("" + expiryPeriod);
		ftmDetailsDto.setFtmId(ftpChipDetail.getFtpChipDetailId());
		ftmDetailsDto.setMake(ftpChipDetail.getMake());
		ftmDetailsDto.setModel(ftpChipDetail.getModel());
		return ftmDetailsDto;
	}

	public SbiDetailsDto populateSbiDetails(int expiryPeriod, SecureBiometricInterface sbiDetails) {
		SbiDetailsDto sbiDetailsDto = new SbiDetailsDto();
		sbiDetailsDto.setSbiId(sbiDetails.getId());
		sbiDetailsDto.setSbiVersion(sbiDetails.getSwVersion());
		sbiDetailsDto.setPartnerId(sbiDetails.getProviderId());
		sbiDetailsDto.setExpiryDateTime(sbiDetails.getSwExpiryDateTime().toString());
		sbiDetailsDto.setExpiryPeriod("" + expiryPeriod);
		sbiDetailsDto.setSbiBinaryHash(convertBytesToString(sbiDetails.getSwBinaryHash()));
		sbiDetailsDto.setSbiCreationDate(sbiDetails.getCrDtimes().toString());
		return sbiDetailsDto;
	}

	private String convertBytesToString(byte[] bytes) {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public ApiKeyDetailsDto populateApiKeyDetails(int expiryPeriod, PartnerPolicy apiKeyDetails) {
		ApiKeyDetailsDto apiKeyDetailsDto = new ApiKeyDetailsDto();
		apiKeyDetailsDto.setApiKeyName(apiKeyDetails.getLabel());
		apiKeyDetailsDto.setExpiryDateTime(apiKeyDetails.getValidToDatetime().toLocalDateTime().toString());
		apiKeyDetailsDto.setPartnerId(apiKeyDetails.getPartner().getId());
		apiKeyDetailsDto.setExpiryPeriod("" + expiryPeriod);
		//Fetch the policy name and policy group name
		List<String> policyIdList = new ArrayList<String>();
		policyIdList.add(apiKeyDetails.getPolicyId());
		List<AuthPolicy> authPolicies = authPolicyRepository.findAllByPolicyIds(policyIdList);
		if (authPolicies != null && !authPolicies.isEmpty()) {
			AuthPolicy authPolicy = authPolicies.get(0);
			String policyName = authPolicy.getName();
			String policyGroupName = authPolicy.getPolicyGroup().getName();
			apiKeyDetailsDto.setPolicyGroup(policyGroupName);
			apiKeyDetailsDto.setPolicyName(policyName);
		}
		return apiKeyDetailsDto;
	}

	public MISPLicenseKeyDetailsDto populateMispLicenseDetails(int expiryPeriod, MISPLicenseEntityV2 mispLicenseDetails) {
		MISPLicenseKeyDetailsDto MISPLicenseKeyDetailsDto = new MISPLicenseKeyDetailsDto();
		MISPLicenseKeyDetailsDto.setMispLicenseKeyName(mispLicenseDetails.getLicenseKeyName());
		MISPLicenseKeyDetailsDto.setExpiryDateTime(mispLicenseDetails.getValidToDate().toString());
		MISPLicenseKeyDetailsDto.setMispPartnerId(mispLicenseDetails.getMispId());
		MISPLicenseKeyDetailsDto.setExpiryPeriod(String.valueOf(expiryPeriod));

		// Fetch the policy name and policy group name
		if (mispLicenseDetails.getPolicyId() != null) {
			Optional<AuthPolicy> optionalAuthPolicy = authPolicyRepository.findById(mispLicenseDetails.getPolicyId());
			if (optionalAuthPolicy.isEmpty()) {
				log.debug("No Auth Policy found for policy id {} linked to MISP Partner Id {}",
						mispLicenseDetails.getPolicyId(),
						mispLicenseDetails.getMispId());
				return MISPLicenseKeyDetailsDto;
			}
			AuthPolicy authPolicy = optionalAuthPolicy.get();
			MISPLicenseKeyDetailsDto.setPolicyGroup(authPolicy.getPolicyGroup().getName());
			MISPLicenseKeyDetailsDto.setPolicyName(authPolicy.getName());
		}
		return MISPLicenseKeyDetailsDto;
	}
}