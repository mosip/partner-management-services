package io.mosip.pms.tasklets.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.CryptoRequestDto;
import io.mosip.pms.common.dto.CryptoResponseDto;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.common.dto.TrustCertTypeListRequestDto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import io.mosip.pms.partner.util.PartnerUtil;

@Component
public class KeyManagerHelper {

	private Logger log = PMSLogger.getLogger(KeyManagerHelper.class);

	private static final String PARTNER_CERT_ID = "partnerCertId";

	@Value("${pmp.partner.certificaticate.get.rest.uri}")
	private String keyManagerPartnerCertificateUrl;

	@Value("${pmp.trust.certificates.post.rest.uri}")
	private String keyManagerTrustCertificateUrl;

	@Value("${pmp.encrypt.data.post.rest.uri}")
	private String keyManagerEncryptDataUrl;

	@Value("${pmp.decrypt.data.post.rest.uri}")
	private String keyManagerDecryptDataUrl;

	@Value("${mosip.service.keymanager.crypto.appId}")
	private String appId;

	@Value("${mosip.service.keymanager.crypto.refId}")
	private String refId;

	@Autowired
	RestUtil restUtil;

	@Autowired
	ObjectMapper objectMapper;

	public PartnerCertDownloadResponeDto getPartnerCertificate(String certificateAlias) {
		Map<String, String> pathSegments = Map.of(PARTNER_CERT_ID, certificateAlias);

		try {
			Map<String, Object> response = restUtil.getApi(keyManagerPartnerCertificateUrl, pathSegments, Map.class);
			PartnerUtil.validateApiResponse(response, keyManagerPartnerCertificateUrl);
			return objectMapper.convertValue(response.get(PartnerConstants.RESPONSE),
					PartnerCertDownloadResponeDto.class);
		} catch (BatchJobServiceException e) {
			throw e;
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id",
					"In getPartnerCertificate method of KeyManagerHelper - " + e.getMessage());
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
			PartnerUtil.validateApiResponse(response, keyManagerTrustCertificateUrl);
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

	public String encryptData(String data) {
		try {
			CryptoRequestDto cryptoRequestDto = new CryptoRequestDto();
			cryptoRequestDto.setData(CryptoUtil.encodeToPlainBase64(data.getBytes(StandardCharsets.UTF_8)));
			cryptoRequestDto.setApplicationId(appId);
			cryptoRequestDto.setReferenceId(refId);
			cryptoRequestDto.setTimeStamp(LocalDateTime.now());

			RequestWrapperV2<CryptoRequestDto> requestWrapper = new RequestWrapperV2<>();
			requestWrapper.setRequest(cryptoRequestDto);

			Map<String, Object> response = restUtil.postApi(
					keyManagerEncryptDataUrl,
					null,
					"",
					"",
					MediaType.APPLICATION_JSON,
					requestWrapper,
					Map.class
			);
			PartnerUtil.validateApiResponse(response, keyManagerEncryptDataUrl);
			CryptoResponseDto responseDto = objectMapper.convertValue(
					response.get(PartnerConstants.RESPONSE), CryptoResponseDto.class);
			return responseDto.getData();
		} catch (BatchJobServiceException e) {
			throw e;
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", "Exception in encryptData of KeyManagerHelper- {}", ex.getMessage());
			throw new BatchJobServiceException(
					ErrorCode.ENCRYPT_DATA_ERROR.getErrorCode(),
					ErrorCode.ENCRYPT_DATA_ERROR.getErrorMessage()
			);
		}
	}

	public String decryptData(String data) {
		try {
			if (!Base64.isBase64(data)) {
				log.debug("Data does not appear to be Base64 encoded. Likely not encrypted previously, returning original value.");
				return data;
			}
			CryptoRequestDto cryptoRequestDto = new CryptoRequestDto();
			cryptoRequestDto.setData(data);
			cryptoRequestDto.setApplicationId(appId);
			cryptoRequestDto.setReferenceId(refId);
			cryptoRequestDto.setTimeStamp(LocalDateTime.now());

			RequestWrapperV2<CryptoRequestDto> requestWrapper = new RequestWrapperV2<>();
			requestWrapper.setRequest(cryptoRequestDto);
			log.debug("calling keyManagerDecryptDataUrl ");
			Map<String, Object> response = restUtil.postApi(
					keyManagerDecryptDataUrl,
					null,
					"",
					"",
					MediaType.APPLICATION_JSON,
					requestWrapper,
					Map.class
			);
			log.debug("response {}", response);
			PartnerUtil.validateApiResponse(response, keyManagerDecryptDataUrl);
			CryptoResponseDto responseDto = objectMapper.convertValue(
					response.get(PartnerConstants.RESPONSE), CryptoResponseDto.class);
			// Decode Base64-encoded response data
			return new String(Base64.decodeBase64(responseDto.getData().getBytes()),
					StandardCharsets.UTF_8
			);
		} catch (BatchJobServiceException e) {
			log.info("Possible error in decryptData of KeyManagerHelper: {}, but it can be ignored since data may not be encrypted previously", e.getMessage());
			// Return original data in case of any decryption error for backward compatibility
			return data;
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", "Exception in decryptData of KeyManagerHelper- {}", ex.getMessage());
			throw new BatchJobServiceException(
					ErrorCode.DECRYPT_DATA_ERROR.getErrorCode(),
					ErrorCode.DECRYPT_DATA_ERROR.getErrorMessage()
			);
		}
	}
}
