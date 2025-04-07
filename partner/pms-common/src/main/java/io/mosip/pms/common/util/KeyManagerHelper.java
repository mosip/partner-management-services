package io.mosip.pms.common.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.common.dto.TrustCertTypeListRequestDto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.common.dto.CryptoRequestDto;
import io.mosip.pms.common.dto.CryptoResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;

@Component
public class KeyManagerHelper {

	private static final Logger LOGGER = PMSLogger.getLogger(KeyManagerHelper.class);

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
			validateApiResponse(response, keyManagerPartnerCertificateUrl);
			return objectMapper.convertValue(response.get(PartnerConstants.RESPONSE), PartnerCertDownloadResponeDto.class);
		} catch (ApiAccessibleException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.debug("sessionId", "idType", "id",
					"In getTrustCertificates method of KeyManagerHelper - " + e.getMessage());
			throw new ApiAccessibleException(
					ApiAccessibleExceptionConstant.PARTNER_CERTIFICATE_FETCH_ERROR.getErrorCode(),
					ApiAccessibleExceptionConstant.PARTNER_CERTIFICATE_FETCH_ERROR.getErrorMessage()
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
			validateApiResponse(response, keyManagerTrustCertificateUrl);
			return objectMapper.convertValue(response.get(PartnerConstants.RESPONSE), TrustCertTypeListResponseDto.class);
		} catch (ApiAccessibleException e) {
			throw e;
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id",
					"In getTrustCertificates method of KeyManagerHelper - " + ex.getMessage());
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.TRUST_CERTIFICATES_FETCH_ERROR.getErrorCode(),
					ApiAccessibleExceptionConstant.TRUST_CERTIFICATES_FETCH_ERROR.getErrorMessage());
		}
	}

	public void validateApiResponse(Map<String, Object> response, String apiUrl) {
		if (response == null) {
			LOGGER.error("Received null response from API: {}", apiUrl);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}
		if (response.containsKey(PartnerConstants.ERRORS)) {
			List<Map<String, Object>> errorList = (List<Map<String, Object>>) response.get(PartnerConstants.ERRORS);
			if (errorList != null && !errorList.isEmpty()) {
				LOGGER.error("Error occurred while fetching data: {}", errorList);
				throw new ApiAccessibleException(String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORCODE)),
						String.valueOf(errorList.getFirst().get(PartnerConstants.ERRORMESSAGE)));
			}
		}
		if (!response.containsKey(PartnerConstants.RESPONSE) || response.get(PartnerConstants.RESPONSE) == null) {
			LOGGER.error("Missing response data in API call: {}", apiUrl);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage(), 
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}
	}

	public String encryptData(String data) {
		try {
			CryptoRequestDto cryptoRequestDto = new CryptoRequestDto();
			cryptoRequestDto.setData(CryptoUtil.encodeToURLSafeBase64(data.getBytes(StandardCharsets.UTF_8)));
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
			validateApiResponse(response, keyManagerEncryptDataUrl);
			CryptoResponseDto responseDto = objectMapper.convertValue(
					response.get(PartnerConstants.RESPONSE), CryptoResponseDto.class);
			return responseDto.getData();
		} catch (ApiAccessibleException e) {
			throw e;
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", "Exception in encryptData of KeyManagerHelper- {}", ex.getMessage());
			throw new ApiAccessibleException(
					ApiAccessibleExceptionConstant.ENCRYPT_DATA_ERROR.getErrorCode(),
					ApiAccessibleExceptionConstant.ENCRYPT_DATA_ERROR.getErrorMessage()
			);
		}
	}

	public String decryptData(String data) {
		try {
			CryptoRequestDto cryptoRequestDto = new CryptoRequestDto();
			cryptoRequestDto.setData(data);
			cryptoRequestDto.setApplicationId(appId);
			cryptoRequestDto.setReferenceId(refId);
			cryptoRequestDto.setTimeStamp(LocalDateTime.now());

			RequestWrapperV2<CryptoRequestDto> requestWrapper = new RequestWrapperV2<>();
			requestWrapper.setRequest(cryptoRequestDto);

			Map<String, Object> response = restUtil.postApi(
					keyManagerDecryptDataUrl,
					null,
					"",
					"",
					MediaType.APPLICATION_JSON,
					requestWrapper,
					Map.class
			);
			validateApiResponse(response, keyManagerDecryptDataUrl);
			CryptoResponseDto responseDto = objectMapper.convertValue(
					response.get(PartnerConstants.RESPONSE), CryptoResponseDto.class);
			// Decode Base64-encoded response data
			return new String(
					Base64.getDecoder().decode(responseDto.getData()),
					StandardCharsets.UTF_8
			);
		} catch (ApiAccessibleException e) {
			LOGGER.info("Error in decryptData of KeyManagerHelper: {}", e.getMessage());
			// Return original data in case of any decryption error for backward compatibility
			return data;
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", "Exception in decryptData of KeyManagerHelper- {}", ex.getMessage());
			throw new ApiAccessibleException(
					ApiAccessibleExceptionConstant.DECRYPT_DATA_ERROR.getErrorCode(),
					ApiAccessibleExceptionConstant.DECRYPT_DATA_ERROR.getErrorMessage()
			);
		}
	}

}
