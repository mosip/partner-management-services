package io.mosip.pms.tasklets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.mosip.pms.partner.dto.AdminDetailsDto;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.MISPLicenseKeyDetailsDto;
import io.mosip.pms.common.entity.MISPLicenseEntityV2;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.MispLicenseV2Repository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.tasklets.service.EmailNotificationService;
import io.mosip.pms.tasklets.util.BatchJobHelper;
import io.mosip.pms.tasklets.util.KeyManagerHelper;
import io.mosip.pms.tasklets.util.KeycloakHelper;

/**
 * This Batch Job will create notifications for all MISP license keys
 * expiring as per the configured period.
 */
@Component
public class MispLicenseExpiryTasklet implements Tasklet {

	private Logger log = PMSLogger.getLogger(MispLicenseExpiryTasklet.class);

	@Value("#{'${mosip.pms.batch.job.misp.license.key.expiry.periods}'.split(',')}")
	private List<Integer> mispLicenseExpiryPeriods;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	@Autowired
	KeycloakHelper keycloakHelper;

	@Autowired
	MispLicenseV2Repository mispLicenseRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("MispLicenseExpiryTasklet: START");
		List<String> totalNotificationsCreated = new ArrayList<String>();
		int countOfMispLicensesExpiringWithin30Days = 0;
		int partnerAdminsCount = 0;
		List<MISPLicenseEntityV2> allActiveMispLicenses = new ArrayList<>();
		try {
			// Step 1: Fetch Partner Admin User IDs from Keycloak
			List<AdminDetailsDto> partnerAdmins = keycloakHelper.getPartnerIdsWithPartnerAdminRole();
			partnerAdminsCount = partnerAdmins.size();
			partnerAdmins.forEach(admin -> {
				log.info("Keycloak Partner Admin Id: {}", admin.getUserName());
			});
			
			if (partnerAdminsCount > 0) {
				// Step 2: Get all Active MISP License Keys
				allActiveMispLicenses = mispLicenseRepository.findAllActiveMISPLicenses();
				log.info("Found {} Active MISP License Keys.", allActiveMispLicenses.size());
				
				// Step 3: Process each MISP License Key
				for (MISPLicenseEntityV2 mispLicenseDetails : allActiveMispLicenses) {
					// Get partner information from MISP license
					String mispPartnerId = mispLicenseDetails.getMispId();
					Optional<Partner> partnerOptional = batchJobHelper.getPartnerById(mispPartnerId);
	 
					if (partnerOptional.isEmpty()) {
						log.warn("Partner not found for MISP ID: {}", mispPartnerId);
						continue;
					}
	 
					Partner mispPartner = partnerOptional.get();
	 
					// Step 4: For each MISP License Key check if it is expiring or not
					if (mispLicenseDetails.getValidToDate() != null) {
						log.info("Checking if MISP License key is expiring for partner id {}",
								mispPartnerId + " within next 30 days.");
						LocalDateTime mispLicenseExpiryDateTime = mispLicenseDetails.getValidToDate();
						log.info("The MISP License key expiry date is {}", mispLicenseExpiryDateTime);
						boolean isExpiringWithin30Days = batchJobHelper.checkIfExpiring(mispPartner,
								mispLicenseExpiryDateTime, 30, true);
						if (isExpiringWithin30Days) {
							countOfMispLicensesExpiringWithin30Days++;
							log.info("MISP License key is expiring for partner id {}",
									mispPartnerId + " during the next 30 days.");
							// Step 5: Check if the MISP License Key is expiring after 30 days, 15 days, 10 days,
							// 9 days and so on, if yes create notifications
							Iterator<Integer> expiryPeriodsIterator = mispLicenseExpiryPeriods.iterator();
							while (expiryPeriodsIterator.hasNext()) {
								Integer expiryPeriod = expiryPeriodsIterator.next();
								log.info("Checking for MISP License key expiry after " + expiryPeriod + " days.");
								boolean isExpiringAfterExpiryPeriod = batchJobHelper
										.checkIfExpiring(mispPartner, mispLicenseExpiryDateTime, expiryPeriod, false);
								// Step 6: If yes, create notifications for all Partner Admins
								if (isExpiringAfterExpiryPeriod) {
									List<MISPLicenseKeyDetailsDto> expiringMispLicensesList = new ArrayList<MISPLicenseKeyDetailsDto>();
									MISPLicenseKeyDetailsDto MISPLicenseKeyDetailsDto = batchJobHelper
											.populateMispLicenseDetails(expiryPeriod, mispLicenseDetails);
									expiringMispLicensesList.add(MISPLicenseKeyDetailsDto);
									
									// Step 7: Send notification to all Partner Admins
									partnerAdmins.forEach(partnerAdminDetails -> {
										String emailId = partnerAdminDetails.getEmailId();
										NotificationEntity savedNotification = batchJobHelper.saveNotification(
												PartnerConstants.MISP_LICENSE_KEY_EXPIRY_NOTIFICATION_TYPE, partnerAdminDetails.getUserName(), partnerAdminDetails.getLangCode(), null, null,
												null, null, expiringMispLicensesList, emailId);
										// Step 8: send email notification
										emailNotificationService.sendEmailNotification(savedNotification, emailId);
										log.info("Created MISP License expiry notification with notification id {} for Partner Admin {}",
												savedNotification.getId(), partnerAdminDetails.getUserName());
										totalNotificationsCreated.add(savedNotification.getId());
									});
									break;
								} else {
									log.info("MISP License key is NOT expiring after " + expiryPeriod + " days.");
									// check for next time interval
								}
							}
						} else {
							log.info("MISP License key is NOT expiring for partner id {}",
									mispPartnerId + " during the next 30 days.");
						}
					}
				}
			} else {
				log.info("There are no {} partner admin users in Keycloak. Hence skipping creation of notifications.", partnerAdminsCount);
			}
		} catch (Exception e) {
			log.error("Error occurred while running MispLicenseExpiryTasklet: {}", e.getMessage(), e);
		}
		log.info("Overall found " + countOfMispLicensesExpiringWithin30Days
				+ " MISP License keys which are expiring during the next 30 days. But notifications will only be created as per the configured expiry days.");
		log.info("MispLicenseExpiryTasklet: DONE, created {} notifications for {} partner admins."
				+ " Checked {} active MISP License keys.", totalNotificationsCreated.size(), partnerAdminsCount, allActiveMispLicenses.size());
		totalNotificationsCreated.forEach(notificationId -> {
			log.info(notificationId);
		});

		return RepeatStatus.FINISHED;
	}

}
