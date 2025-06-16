package io.mosip.pms.tasklets.util;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.ApiKeyDetailsDto;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.FtmDetailsDto;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.common.dto.SbiDetailsDto;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.response.dto.FtmCertificateDownloadResponseDto;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerHelper;

@Component
public class PartnerCertificateExpiryHelper {

	private Logger log = PMSLogger.getLogger(PartnerCertificateExpiryHelper.class);

	@Value("#{'${mosip.pms.batch.job.partner.cert.expiry.periods}'.split(',')}")
	private List<Integer> partnerCertExpiryPeriods;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	AuthPolicyRepository authPolicyRepository;

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
		certificateDetails.setPartnerDomain(batchJobHelper.getPartnerDomain(partner.getPartnerTypeCode()));
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
		ftmDetailsDto.setPartnerDomain(batchJobHelper.getPartnerDomain(PartnerConstants.FTM_PROVIDER_PARTNER_TYPE));
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
		sbiDetailsDto.setSbiBinaryHash(sbiDetails.getSwBinaryHash().toString());
		sbiDetailsDto.setSbiCreationDate(sbiDetails.getCrDtimes().toString());
		return sbiDetailsDto;
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
		if (authPolicies != null && authPolicies.size() > 0) {
			AuthPolicy authPolicy = authPolicies.get(0);
			String policyName = authPolicy.getName();
			String policyGroupName = authPolicy.getPolicyGroup().getName();
			apiKeyDetailsDto.setPolicyGroup(policyGroupName);
			apiKeyDetailsDto.setPolicyName(policyName);
		}
		return apiKeyDetailsDto;
	}

}
