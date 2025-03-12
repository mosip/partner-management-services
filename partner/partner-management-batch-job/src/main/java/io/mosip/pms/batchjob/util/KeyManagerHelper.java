package io.mosip.pms.batchjob.util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import io.mosip.pms.common.dto.OriginalCertDownloadResponseDto;
import io.mosip.pms.common.dto.TrustCertTypeListRequestDto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
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
	RestHelper restHelper;

	@Autowired
	ObjectMapper objectMapper;

	public OriginalCertDownloadResponseDto getPartnerCertificate(String certificateAlias) {
		try {
			Map<String, String> pathSegments = new HashMap<>();
			pathSegments.put(PARTNER_CERT_ID, certificateAlias);
			// Build the URL
			String urlWithPath = UriComponentsBuilder.fromUriString(keyManagerPartnerCertificateUrl)
					.buildAndExpand(pathSegments).toUriString();

			return restHelper.sendRequest(urlWithPath, HttpMethod.GET, null,
					new TypeReference<OriginalCertDownloadResponseDto>() {
					}, MediaType.APPLICATION_JSON);
		} catch (BatchJobServiceException e) {
			LOGGER.error("Error fetching partner certificate: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			LOGGER.error("Unexpected error occurred while fetching partner certificate", e);
			throw new BatchJobServiceException(ErrorCodes.PARTNER_CERTIFICATE_FETCH_ERROR.getCode(),
					ErrorCodes.PARTNER_CERTIFICATE_FETCH_ERROR.getMessage());
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
			return restHelper.sendRequest(keyManagerTrustCertificateUrl, HttpMethod.POST, request,
					new TypeReference<TrustCertTypeListResponseDto>() {
					}, MediaType.APPLICATION_JSON);
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
