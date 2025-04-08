package io.mosip.pms.tasklets.util;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.common.dto.TrustCertTypeListRequestDto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;

@Component
public class KeyManagerHelper {

	private Logger log = PMSLogger.getLogger(KeyManagerHelper.class);

	private static final String PARTNER_CERT_ID = "partnerCertId";

	@Value("${pmp.partner.certificaticate.get.rest.uri}")
	private String keyManagerPartnerCertificateUrl;

	@Value("${pmp.trust.certificates.post.rest.uri}")
	private String keyManagerTrustCertificateUrl;

	@Autowired
	RestUtil restUtil;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	BatchJobHelper batchJobHelper;

	public PartnerCertDownloadResponeDto getPartnerCertificate(String certificateAlias) {
		Map<String, String> pathSegments = Map.of(PARTNER_CERT_ID, certificateAlias);

		try {
			Map<String, Object> response = restUtil.getApi(keyManagerPartnerCertificateUrl, pathSegments, Map.class);
			batchJobHelper.validateApiResponse(response, keyManagerPartnerCertificateUrl);
			return objectMapper.convertValue(response.get(PartnerConstants.RESPONSE),
					PartnerCertDownloadResponeDto.class);
		} catch (BatchJobServiceException e) {
			throw e;
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id",
					"In getTrustCertificates method of KeyManagerHelper - " + e.getMessage());
			throw new BatchJobServiceException(ErrorCode.PARTNER_CERTIFICATE_FETCH_ERROR.getErrorCode(),
					ErrorCode.PARTNER_CERTIFICATE_FETCH_ERROR.getErrorMessage());
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

			log.info("request sent, {}", request);
			Map<String, Object> response = restUtil.postApi(keyManagerTrustCertificateUrl, null, "", "",
					MediaType.APPLICATION_JSON, request, Map.class);
			batchJobHelper.validateApiResponse(response, keyManagerTrustCertificateUrl);
			return objectMapper.convertValue(response.get(PartnerConstants.RESPONSE),
					TrustCertTypeListResponseDto.class);
		} catch (BatchJobServiceException e) {
			throw e;
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id",
					"In getTrustCertificates method of KeyManagerHelper - " + ex.getMessage());
			throw new BatchJobServiceException(ErrorCode.TRUST_CERTIFICATES_FETCH_ERROR.getErrorCode(),
					ErrorCode.TRUST_CERTIFICATES_FETCH_ERROR.getErrorMessage());
		}
	}
}
