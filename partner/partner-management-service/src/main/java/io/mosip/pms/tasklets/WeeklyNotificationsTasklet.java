package io.mosip.pms.tasklets;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.ApiKeyDetailsDto;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.FtmDetailsDto;
import io.mosip.pms.common.dto.SbiDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.repository.FTPChipDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.tasklets.service.EmailNotificationService;
import io.mosip.pms.tasklets.util.BatchJobHelper;
import io.mosip.pms.tasklets.util.KeyManagerHelper;
import io.mosip.pms.tasklets.util.KeycloakHelper;
import io.mosip.pms.tasklets.util.PartnerCertificateExpiryHelper;

/**
 * This Batch Job will create weekly notifications for the Partner certificates
 * expiring as per the configured period.
 * 
 * @author Mayura Deshmukh
 * @since 1.3.x
 */
@Component
public class WeeklyNotificationsTasklet implements Tasklet {
	private Logger log = PMSLogger.getLogger(WeeklyNotificationsTasklet.class);

	@Autowired
	KeycloakHelper keycloakHelper;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Autowired
	PartnerCertificateExpiryHelper partnerCertificateExpiryHelper;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	@Autowired
	FTPChipDetailRepository ftpChipDetailRepository;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	SecureBiometricInterfaceRepository sbiRepository;

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("WeeklyNotificationsTasklet: START");
		List<String> createdNotificationIds = new ArrayList<String>();
		try {
			// Step 1: Fetch Partner Admin User IDs from Keycloak, which are Valid Partners
			// in PMS
			List<Partner> pmsPartnerAdmins = keycloakHelper.getPartnerIdsWithPartnerAdminRole();
			pmsPartnerAdmins.forEach(admin -> {
				log.info("PMS Partner Admin Id: {}", admin.getId());
			});
			// Step 2: Get the list of all partner certificates expiring this week
			Map<Partner, X509Certificate> expiringPartnerCertificates = getListOfExpiringPartnerCertificates(
					pmsPartnerAdmins);
			// Step 3: Get the list of all FTM chip certificates expiring this week
			Map<FTPChipDetail, X509Certificate> expiringFtmCertificates = getListofExpiringFtmCertificates();
			// Step 4: Get the list of all SBI details expiring this week
			List<SecureBiometricInterface> expiringSbiDetails = getListofExpiringSbi();
			// Step 5: Get the list of all API Keys expiring this week
			List<PartnerPolicy> expiringApiKeys = getListofExpiringApiKeys();
			// Step 6: Create a weekly notification for all the partner admin users
			log.info("Creating weekly summary notifications");
			createdNotificationIds = createWeeklySummaryNotifications(pmsPartnerAdmins, expiringPartnerCertificates,
					expiringFtmCertificates, expiringSbiDetails, expiringApiKeys);

		} catch (Exception e) {
			log.error("Error occurred while running WeeklyNotificationsTasklet: {}", e.getMessage(), e);
		}

		log.info("WeeklyNotificationsTasklet: DONE, created {}", createdNotificationIds.size() + " notifications.");
		createdNotificationIds.forEach(notificationId -> {
			log.info(notificationId);
		});

		return RepeatStatus.FINISHED;
	}

	private Map<Partner, X509Certificate> getListOfExpiringPartnerCertificates(List<Partner> pmsPartnerAdmins) {

		log.info("WeeklyNotificationsTasklet: getListOfExpiringPartnerCertificates(): START");

		Map<Partner, X509Certificate> expiringPartnerCertificates = new HashMap<Partner, X509Certificate>();

		// Step 1: Get all PMS partners which are ACTIVE and NOT partner admins
		int countOfPartnersWithInvalidCerts = 0;
		List<Partner> activePartnersList = batchJobHelper.getAllActiveNonAdminPartners(pmsPartnerAdmins);
		int activePartnersCount = activePartnersList.size();
		log.info("PMS has {} Active Partner (Non Admin) users.", activePartnersCount);
		// Step 2: For each partner get the certificate and check if it is expiring
		Iterator<Partner> activePartnersListIterator = activePartnersList.iterator();
		while (activePartnersListIterator.hasNext()) {
			Partner pmsPartner = activePartnersListIterator.next();
			log.info("Fetching certificate for partner id {}", pmsPartner.getId());
			X509Certificate decodedPartnerCertificate = partnerCertificateExpiryHelper
					.getDecodedCertificate(pmsPartner);
			if (decodedPartnerCertificate != null) {
				boolean isExpiring = checkIfCertIsExpiringThisWeek(pmsPartner, decodedPartnerCertificate);
				if (isExpiring) {
					expiringPartnerCertificates.put(pmsPartner, decodedPartnerCertificate);
				}
			} else {
				countOfPartnersWithInvalidCerts++;
				log.info("Valid certificate not found for partner id {}", pmsPartner.getId());
			}
		}
		log.info("Checked certificate expiry for " + activePartnersCount + " partners");
		log.info("Found {} certificates expiring in next 7 days. ", expiringPartnerCertificates.size());
		if (countOfPartnersWithInvalidCerts > 0) {
			log.info("Note: Valid partner certificate is not available for " + countOfPartnersWithInvalidCerts
					+ " partners.");
		}
		return expiringPartnerCertificates;
	}

	private Map<FTPChipDetail, X509Certificate> getListofExpiringFtmCertificates() {
		log.info("WeeklyNotificationsTasklet: getListofExpiringFtmCertificates(): START");

		Map<FTPChipDetail, X509Certificate> expiringFtmCertificates = new HashMap<FTPChipDetail, X509Certificate>();
		// Step 1: Get all FTM Providers which are Active and Approved
		List<Partner> ftmProvidersList = batchJobHelper
				.getAllActiveAndApprovedPartners(PartnerConstants.FTM_PROVIDER_PARTNER_TYPE);
		int ftmProvidersCount = ftmProvidersList.size();
		log.info("PMS has {} FTM Providers which are Active and Approved.", ftmProvidersCount);
		int countOfPartnersWithInvalidCerts = 0;
		// Step 2: For each FTM Provider get all the FTM chips which are Active and
		// Approved
		Iterator<Partner> ftmProvidersListIterator = ftmProvidersList.iterator();
		while (ftmProvidersListIterator.hasNext()) {
			Partner ftmProvider = ftmProvidersListIterator.next();
			String ftmProviderId = ftmProvider.getId();
			log.info("Fetching all the FTM chips for the FTM provider id {}", ftmProviderId);
			List<FTPChipDetail> ftpChipDetailList = ftpChipDetailRepository
					.findAllActiveAndApprovedByProviderId(ftmProviderId);
			int ftmChipDetailsCount = ftpChipDetailList.size();
			log.info("Found {} FTM chip details which are Active and Approved.", ftmChipDetailsCount);
			for (FTPChipDetail ftpChipDetail : ftpChipDetailList) {
				// Step 3: For each FTM chip get the cerificate and check if it is expiring or
				// not
				if (ftpChipDetail.getCertificateAlias() != null) {
					X509Certificate decodedFtmCert = partnerCertificateExpiryHelper
							.getDecodedFtmCertificate(ftpChipDetail);
					if (decodedFtmCert != null) {
						boolean isExpiring = checkIfCertIsExpiringThisWeek(ftmProvider, decodedFtmCert);
						if (isExpiring) {
							expiringFtmCertificates.put(ftpChipDetail, decodedFtmCert);
						}
					} else {
						countOfPartnersWithInvalidCerts++;
						log.debug("Valid FTM chip certificate not found for FTM provider id {}", ftmProviderId);
					}
				} else {
					countOfPartnersWithInvalidCerts++;
					log.info("Skipping this FTM chip since certificate alias are missing {}",
							ftpChipDetail.getFtpChipDetailId());
				}
			}
		}
		log.info("Checked FTM chip certificate expiry for " + ftmProvidersCount + " partners");
		log.info("Found {} FTM chip certificates expiring in next 7 days. ", expiringFtmCertificates.size());
		if (countOfPartnersWithInvalidCerts > 0) {
			log.info("Note: Valid FTM chip certificate is not available for " + countOfPartnersWithInvalidCerts
					+ " FTM providers.");
		}
		return expiringFtmCertificates;
	}

	private List<SecureBiometricInterface> getListofExpiringSbi() {
		log.info("WeeklyNotificationsTasklet: getListofExpiringSbi(): START");

		List<SecureBiometricInterface> listofExpiringSbi = new ArrayList<SecureBiometricInterface>();
		// Step 1: Get all Device Providers which are Active and Approved
		List<Partner> deviceProvidersList = batchJobHelper
				.getAllActiveAndApprovedPartners(PartnerConstants.DEVICE_PROVIDER_PARTNER_TYPE);
		int deviceProvidersCount = deviceProvidersList.size();
		log.info("PMS has {} Device Providers which are Active and Approved.", deviceProvidersCount);
		// Step 2: For each Device Provider get all the SBI which are Active and
		// Approved
		Iterator<Partner> deviceProvidersListIterator = deviceProvidersList.iterator();
		while (deviceProvidersListIterator.hasNext()) {
			Partner deviceProvider = deviceProvidersListIterator.next();
			String deviceProviderId = deviceProvider.getId();
			log.info("Fetching all the SBI details for the device provider id {}", deviceProviderId);
			List<SecureBiometricInterface> sbiList = sbiRepository
					.findAllActiveAndApprovedByProviderId(deviceProviderId);
			int sbiCount = sbiList.size();
			log.info("Found {} SBI details which are Active and Approved.", sbiCount);
			for (SecureBiometricInterface sbiDetail : sbiList) {
				// Step 3: For each SBI check if it is expiring or not
				if (sbiDetail.getSwExpiryDateTime() != null) {
					LocalDateTime sbiExpiryDateTime = sbiDetail.getSwExpiryDateTime();
					log.info("The SBI expiry date is {}", sbiExpiryDateTime);
					boolean isExpiring = partnerCertificateExpiryHelper.checkIfExpiring(deviceProvider,
							sbiExpiryDateTime, 7, true);
					if (isExpiring) {
						listofExpiringSbi.add(sbiDetail);
					}
				}
			}
		}
		log.info("Checked SBI expiry for " + deviceProvidersCount + " partners");
		log.info("Found {} SBI expiring in next 7 days. ", listofExpiringSbi.size());
		return listofExpiringSbi;
	}

	private List<PartnerPolicy> getListofExpiringApiKeys() {
		log.info("WeeklyNotificationsTasklet: getListofExpiringApiKeys(): START");

		List<PartnerPolicy> listofExpiringApiKeys = new ArrayList<PartnerPolicy>();
		// Step 1: Get all Auth Partners which are Active and Approved
		List<Partner> authPartnersList = batchJobHelper
				.getAllActiveAndApprovedPartners(PartnerConstants.AUTH_PARTNER_TYPE);
		int authPartnersCount = authPartnersList.size();
		log.info("PMS has {} Device Providers which are Active and Approved.", authPartnersCount);
		// Step 2: For each Auth Partners get all the API Keys which are Active
		Iterator<Partner> authPartnersListIterator = authPartnersList.iterator();
		while (authPartnersListIterator.hasNext()) {
			Partner authPartner = authPartnersListIterator.next();
			String authPartnerId = authPartner.getId();
			log.info("Fetching all the API Keys for the auth partner id {}", authPartnerId);
			List<PartnerPolicy> apiKeyList = partnerPolicyRepository.findByPartnerIdAndIsActiveTrue(authPartnerId);
			int apiKeysCount = apiKeyList.size();
			log.info("Found {} API Keys which are Active.", apiKeysCount);
			for (PartnerPolicy apiKeyDetails : apiKeyList) {
				// Step 3: For each API Key check if it is expiring or not
				if (apiKeyDetails.getValidToDatetime() != null) {
					LocalDateTime apiKeyExpiryDateTime = apiKeyDetails.getValidToDatetime().toLocalDateTime();
					log.info("The API Key expiry date is {}", apiKeyExpiryDateTime);
					boolean isExpiring = partnerCertificateExpiryHelper.checkIfExpiring(authPartner,
							apiKeyExpiryDateTime, 7, true);
					if (isExpiring) {
						listofExpiringApiKeys.add(apiKeyDetails);
					}
				}
			}
		}
		log.info("Checked API Key expiry for " + authPartnersCount + " partners");
		log.info("Found {} API Keys expiring in next 7 days. ", listofExpiringApiKeys.size());
		return listofExpiringApiKeys;
	}

	private boolean checkIfCertIsExpiringThisWeek(Partner pmsPartner, X509Certificate decodedPartnerCertificate) {
		// Check if the certificate is expiring within 7 days
		log.info("Checking if certificate is expiring within next 7 days.");
		LocalDateTime expiryDate = partnerCertificateExpiryHelper
				.getCertificateExpiryDateTime(decodedPartnerCertificate);
		log.info("The certificate expiry date is {}", expiryDate);
		boolean isExpiringWithin7Days = partnerCertificateExpiryHelper.checkIfExpiring(pmsPartner, expiryDate, 7, true);
		if (isExpiringWithin7Days) {
			log.info("Certificate is expiring during the next 7 days.");
		} else {
			log.info("Certificate is NOT expiring during the next 7 days.");
		}
		return isExpiringWithin7Days;
	}

	/**
	 * Weekly summary notification is to be created even when there are zero partner
	 * certificates / API keys / SBI / FTM chip certificates expiring.
	 */
	private List<String> createWeeklySummaryNotifications(List<Partner> pmsPartnerAdmins,
			Map<Partner, X509Certificate> expiringPartnerCertificates,
			Map<FTPChipDetail, X509Certificate> expiringFtmCertificates, List<SecureBiometricInterface> expiringSbi,
			List<PartnerPolicy> expiringApiKeys) {

		List<String> createdNotificationIds = new ArrayList<String>();
		List<CertificateDetailsDto> certificateDetailsList = new ArrayList<CertificateDetailsDto>();
		List<FtmDetailsDto> ftmDetailsList = new ArrayList<FtmDetailsDto>();
		List<SbiDetailsDto> sbiDetailsList = new ArrayList<SbiDetailsDto>();
		List<ApiKeyDetailsDto> apiKeyDetailsList = new ArrayList<ApiKeyDetailsDto>();

		if (expiringPartnerCertificates.size() > 0) {
			expiringPartnerCertificates.forEach((partnerWithExpiringCert, decodedPartnerCertificate) -> {
				log.info("Weekly Summary - adding certificate expiry details for partner id {}",
						partnerWithExpiringCert.getId());
				CertificateDetailsDto certificateDetails = partnerCertificateExpiryHelper.populateCertificateDetails(7,
						partnerWithExpiringCert, decodedPartnerCertificate);
				certificateDetailsList.add(certificateDetails);
			});
		}

		if (expiringFtmCertificates.size() > 0) {
			expiringFtmCertificates.forEach((ftpChipDetail, decodedFtmCert) -> {
				log.info("Weekly Summary - adding FTM expiry details for FTM provider id {}",
						ftpChipDetail.getFtpProviderId());
				FtmDetailsDto ftmDetailsDto = partnerCertificateExpiryHelper.populateFtmDetails(7, ftpChipDetail,
						decodedFtmCert);
				ftmDetailsList.add(ftmDetailsDto);
			});
		}

		if (expiringSbi.size() > 0) {
			expiringSbi.forEach((sbiDetail) -> {
				log.info("Weekly Summary - adding SBI details for device provider id {}", sbiDetail.getProviderId());
				SbiDetailsDto sbiDetailsDto = partnerCertificateExpiryHelper.populateSbiDetails(7, sbiDetail);
				sbiDetailsList.add(sbiDetailsDto);
			});
		}

		if (expiringApiKeys.size() > 0) {
			expiringApiKeys.forEach((apiKeyDetails) -> {
				log.info("Weekly Summary - adding API key details for auth partner id {}",
						apiKeyDetails.getPartner().getId());
				ApiKeyDetailsDto apiKeyDetailsDto = partnerCertificateExpiryHelper.populateApiKeyDetails(7,
						apiKeyDetails);
				apiKeyDetailsList.add(apiKeyDetailsDto);
			});
		}

		if (certificateDetailsList.size() > 0 || ftmDetailsList.size() > 0 || sbiDetailsList.size() > 0
				|| expiringApiKeys.size() > 0) {
			Iterator<Partner> pmsPartnerAdminsIterator = pmsPartnerAdmins.iterator();
			while (pmsPartnerAdminsIterator.hasNext()) {
				Partner pmsPartnerAdmin = pmsPartnerAdminsIterator.next();
				// Decrypt the email ID if it's already encrypted to avoid encrypting it again
				String decryptedEmailId = keyManagerHelper.decryptData(pmsPartnerAdmin.getEmailId());
				NotificationEntity savedNotification = batchJobHelper.saveNotification(
						PartnerConstants.WEEKLY_SUMMARY_NOTIFICATION_TYPE, pmsPartnerAdmin, certificateDetailsList,
						ftmDetailsList, sbiDetailsList, apiKeyDetailsList, decryptedEmailId);
				// Step 6: send email notification
				emailNotificationService.sendEmailNotification(savedNotification, decryptedEmailId);
				log.info("Created weekly summary notification with notification id " + savedNotification.getId());
				createdNotificationIds.add(savedNotification.getId());
			}
		}
		return createdNotificationIds;

	}

}