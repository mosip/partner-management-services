package io.mosip.pms.tasklets.util;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.util.PMSLogger;

@Component
public class PartnerCertificateExpiryHelper {

	private Logger log = PMSLogger.getLogger(PartnerCertificateExpiryHelper.class);

	@Value("#{'${mosip.pms.batch.job.partner.cert.expiry.periods}'.split(',')}")
	private List<Integer> partnerCertExpiryPeriods;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	public X509Certificate getDecodedCertificate(Partner pmsPartner) {
		X509Certificate decodedPartnerCertificate = null;
		if (pmsPartner.getCertificateAlias() != null) {
			try {
				PartnerCertDownloadResponeDto certResp = keyManagerHelper
						.getPartnerCertificate(pmsPartner.getCertificateAlias());

				decodedPartnerCertificate = batchJobHelper.decodeCertificateData(certResp.getCertificateData());
			} catch (Exception e) {
				log.debug("Error occurred while fetching certificate for : {}", pmsPartner.getId());
			}
		}
		return decodedPartnerCertificate;
	}

	public boolean checkIfCertificateIsExpiring(Partner pmsPartner, LocalDateTime partnerCertificateExpiryDate,
			Integer expiryPeriod, boolean withinExpiryPeriod) {
		boolean isCertificateExpiring = false;
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
		if (partnerCertificateExpiryDate.isAfter(validTillMinDateTime)
				&& partnerCertificateExpiryDate.isBefore(validTillMaxDateTime)) {
			log.debug("The certificate for partner id {}",
					pmsPartner.getId() + "" + " is expiring after " + expiryPeriod + " days.");
			isCertificateExpiring = true;
		}
		return isCertificateExpiring;
	}

	public CertificateDetailsDto populateCertificateDetails(int expiryPeriod, Partner partner,
			X509Certificate expiringCertificate) {
		CertificateDetailsDto certificateDetails = new CertificateDetailsDto();
		certificateDetails.setCertificateId(partner.getCertificateAlias());
		certificateDetails.setIssuedBy(expiringCertificate.getIssuerX500Principal().getName());
		certificateDetails.setIssuedTo(expiringCertificate.getSubjectX500Principal().getName());
		certificateDetails.setPartnerDomain(batchJobHelper.getPartnerDomain(partner.getPartnerTypeCode()));
		certificateDetails.setPartnerId(partner.getId());
		certificateDetails.setCertificateType(PartnerConstants.PARTNER);
		certificateDetails.setExpiryDateTime(
				expiringCertificate.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime().toString());
		certificateDetails.setExpiryPeriod("" + expiryPeriod);
		return certificateDetails;
	}
}
