package io.mosip.pms.tasklets;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

import com.fasterxml.jackson.core.JsonProcessingException;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.FtmDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.repository.FTPChipDetailRepository;
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

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("WeeklyNotificationsTasklet: START");
		List<String> createdNotificationIds = new ArrayList<String>();
		try {
			//Step 1: Fetch Partner Admin User IDs from Keycloak, which are Valid Partners in PMS
			List<Partner> pmsPartnerAdmins = keycloakHelper.getPartnerIdsWithPartnerAdminRole();
			pmsPartnerAdmins.forEach(admin -> {
				log.info("PMS Partner Admin Id: {}", admin.getId());
			});
			//Step 2: Get the list of all partner certificates expiring this week
			Map<Partner, X509Certificate> expiringPartnerCertificates = getListOfExpiringPartnerCertificates(
					pmsPartnerAdmins);
			//Step 3: Get the list of all FTM chip certificates expiring this week
			Map<FTPChipDetail, X509Certificate> expiringFtmCertificates = getListofExpiringFtmCertificates();
			//Step 4: Create a weekly notification for all the partner admin users
			log.info("Creating weekly summary notifications");
			createdNotificationIds = createWeeklySummaryNotifications(pmsPartnerAdmins, expiringPartnerCertificates,
					expiringFtmCertificates);

		} catch (Exception e) {
			log.error("Error occurred while running WeeklyNotificationsTasklet: {}", e.getMessage(), e);
		}

		log.info("WeeklyNotificationsTasklet: DONE, created {}", createdNotificationIds.size() + " notifications.");
		createdNotificationIds.forEach(notificationId -> {
			log.info(notificationId);
		});

		return RepeatStatus.FINISHED;
	}

	private Map<FTPChipDetail, X509Certificate> getListofExpiringFtmCertificates() {
		log.info("WeeklyNotificationsTasklet: getListofExpiringFtmCertificates(): START");

		Map<FTPChipDetail, X509Certificate> expiringFtmCertificates = new HashMap<FTPChipDetail, X509Certificate>();
		// Step 1: Get all FTM Providers which are Active and Approved
		List<Partner> ftmProvidersList = batchJobHelper.getAllActiveAndApprovedFtmProviders();
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
					.findAllActiveAndApprovedFtmChipDetailsByProviderId(ftmProviderId);
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

	private boolean checkIfCertIsExpiringThisWeek(Partner pmsPartner, X509Certificate decodedPartnerCertificate) {
		// Check if the certificate is expiring within 7 days
		log.info("Checking if certificate is expiring within next 7 days.");
		LocalDateTime expiryDate = partnerCertificateExpiryHelper
				.getCertificateExpiryDateTime(decodedPartnerCertificate);
		log.info("The certificate expiry date is {}", expiryDate);
		boolean isExpiringWithin7Days = partnerCertificateExpiryHelper.checkIfCertificateIsExpiring(pmsPartner,
				expiryDate, 7, true);
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
			Map<FTPChipDetail, X509Certificate> expiringFtmCertificates) {

		List<String> createdNotificationIds = new ArrayList<String>();
		List<CertificateDetailsDto> certificateDetailsList = new ArrayList<CertificateDetailsDto>();
		List<FtmDetailsDto> ftmDetailsList = new ArrayList<FtmDetailsDto>();

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

		Iterator<Partner> pmsPartnerAdminsIterator = pmsPartnerAdmins.iterator();
		while (pmsPartnerAdminsIterator.hasNext()) {
			Partner pmsPartnerAdmin = pmsPartnerAdminsIterator.next();
			// Decrypt the email ID if it's already encrypted to avoid encrypting it again
			String decryptedEmailId = keyManagerHelper.decryptData(pmsPartnerAdmin.getEmailId());
			NotificationEntity savedNotification = batchJobHelper.saveNotification(PartnerConstants.WEEKLY_SUMMARY,
					pmsPartnerAdmin, certificateDetailsList, ftmDetailsList, decryptedEmailId);
			// Step 6: send email notification
			emailNotificationService.sendEmailNotification(savedNotification, decryptedEmailId);
			log.info("Created weekly summary notification with notification id " + savedNotification.getId());
			createdNotificationIds.add(savedNotification.getId());
		}
		return createdNotificationIds;

	}

}