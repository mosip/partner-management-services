package io.mosip.pms.batchjob.util;

import java.time.LocalDateTime;
import java.util.Map;

import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.OriginalCertDownloadResponseDto;
import io.mosip.pms.common.dto.TrustCertTypeListRequestDto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.common.util.RestUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.common.request.dto.RequestWrapper;

@Component
public class KeyManagerHelper {

	private static final Logger LOGGER = LoggerConfiguration.logConfig(KeyManagerHelper.class);

	private static final String PARTNER_CERT_ID = "partnerCertId";

	@Value("${pmp.partner.original.certificate.get.rest.uri}")
	private String keyManagerPartnerCertificateUrl;

	@Value("${pmp.trust.certificates.post.rest.uri}")
	private String keyManagerTrustCertificateUrl;

	@Autowired
	RestUtil restUtil;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	BatchJobHelper batchJobHelper;

	public OriginalCertDownloadResponseDto getPartnerCertificate(String certificateAlias) {
		Map<String, String> pathSegments = Map.of(PARTNER_CERT_ID, certificateAlias);

		try {
			Map<String, Object> response = restUtil.getApi(keyManagerPartnerCertificateUrl, pathSegments, Map.class);
			batchJobHelper.validateApiResponse(response, keyManagerPartnerCertificateUrl);
			return objectMapper.convertValue(response.get(PartnerConstants.RESPONSE), OriginalCertDownloadResponseDto.class);
		} catch (BatchJobServiceException e) {
			LOGGER.error("Error fetching partner certificate: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			LOGGER.error("Unexpected error occurred while fetching partner certificate", e);
			throw new BatchJobServiceException(
					ErrorCodes.PARTNER_CERTIFICATE_FETCH_ERROR.getCode(),
					ErrorCodes.PARTNER_CERTIFICATE_FETCH_ERROR.getMessage()
			);
		}
	}

	public TrustCertTypeListResponseDto getTrustCertificates(String certificateType, LocalDateTime validTillDate) {
		try {
			TrustCertTypeListRequestDto trustCertTypeListRequestDto = new TrustCertTypeListRequestDto();
			trustCertTypeListRequestDto.setCaCertificateType(certificateType);
			trustCertTypeListRequestDto.setExcludeMosipCA(true);
			trustCertTypeListRequestDto.setValidTillDate(validTillDate);

			RequestWrapper<TrustCertTypeListRequestDto> request = new RequestWrapper<>();
			request.setRequest(trustCertTypeListRequestDto);

			LOGGER.info("request sent, {}", request);
			Map<String, Object> response = restUtil
					.postApi(keyManagerTrustCertificateUrl, null, "", "", MediaType.APPLICATION_JSON, request, Map.class);
			batchJobHelper.validateApiResponse(response, keyManagerTrustCertificateUrl);
			return objectMapper.convertValue(response.get(PartnerConstants.RESPONSE), TrustCertTypeListResponseDto.class);
		} catch (BatchJobServiceException e) {
			LOGGER.error("Error fetching partner certificate: {}", e.getMessage());
			throw e;
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getTrustCertificates method of KeyManagerHelper - " + ex.getMessage());
			throw new BatchJobServiceException(ErrorCodes.TRUST_CERTIFICATES_FETCH_ERROR.getCode(),
					ErrorCodes.TRUST_CERTIFICATES_FETCH_ERROR.getMessage());
		}
	}
}
