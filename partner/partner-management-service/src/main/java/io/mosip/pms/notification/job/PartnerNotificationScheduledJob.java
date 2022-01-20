package io.mosip.pms.notification.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.response.dto.NotificationDto;
import io.mosip.pms.common.service.NotificatonService;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.PartnerConstants;

@Component
public class PartnerNotificationScheduledJob {

	private Logger log = PMSLogger.getLogger(PartnerNotificationScheduledJob.class);

	@Autowired
	PartnerPolicyRepository partnerPolicyRepo;

	@Autowired
	AuthPolicyRepository policyRepo;

	@Autowired
	NotificatonService notificationService;

	@Value("${notifications.sent.before.days:3}")
	private int notificationsSentBeforeDays;

	@Scheduled(initialDelayString = "#{60 * 60 * 1000 * ${pms.notifications-schedule.init-delay}}", fixedRateString = "#{60 * 60 * 1000 * ${pms.notifications-schedule.fixed-rate}}")
	public void getAllAPIKeys() {
		List<NotificationDto> notificationsDto = new ArrayList<>();
		List<PartnerPolicy> partnerMappedPolices = partnerPolicyRepo.findAPIKeysLessThanGivenDate(
				LocalDateTime.now().plusDays(notificationsSentBeforeDays), LocalDateTime.now());
		log.info("TotalNoOfRecords " + partnerMappedPolices.size() + "with date less than "
				+ LocalDateTime.now().plusDays(notificationsSentBeforeDays));
		if (partnerMappedPolices.size() > 0) {
			List<AuthPolicy> policies = getPolicies(partnerMappedPolices);
			for (PartnerPolicy partnerPolicy : partnerMappedPolices) {
				AuthPolicy policy = policies.stream().filter(p -> p.getId().equals(partnerPolicy.getPolicyId()))
						.findFirst().orElse(null);
				notificationsDto.add(prepareNotificationDto(partnerPolicy, policy));
			}
		}

		if (notificationsDto.size() > 0) {
			
			try {
				notificationService.sendNotications(EventType.APIKEY_EXPIRED, notificationsDto);
			} catch (Exception e) {
				log.error("Error occured while sending the APIKEY_EXPIRED notifications.", e.getLocalizedMessage(),
						e.getMessage());
			}
			log.info("Notifications sent successfully.",LocalDateTime.now());
		}
	}

	/**
	 * 
	 * @param partnerPolicy
	 * @param policy
	 * @return
	 */
	private NotificationDto prepareNotificationDto(PartnerPolicy partnerPolicy, AuthPolicy policy) {
		NotificationDto dto = new NotificationDto();
		dto.setApiKey(partnerPolicy.getPolicyApiKey());
		dto.setApiKeyExpiryDate(partnerPolicy.getValidToDatetime().toLocalDateTime());
		dto.setEmailId(partnerPolicy.getPartner().getEmailId());
		dto.setPartnerId(partnerPolicy.getPartner().getId());
		dto.setPolicyExpiryDateTime(policy == null ? null : policy.getValidToDate());
		dto.setPolicyId(partnerPolicy.getPolicyId());
		dto.setPolicyName(policy == null ? null : policy.getName());
		dto.setLangCode(partnerPolicy.getPartner().getLangCode());
		dto.setPolicyStatus((policy != null && policy.getIsActive() == true) ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		dto.setApiKeyStatus(partnerPolicy.getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		return dto;
	}

	/**
	 * 
	 * @param partnerPolicies
	 * @return
	 */
	private List<AuthPolicy> getPolicies(List<PartnerPolicy> partnerPolicies) {
		return policyRepo
				.findByPolicyIds(partnerPolicies.stream().map(PartnerPolicy::getPolicyId).collect(Collectors.toList()));
	}
}