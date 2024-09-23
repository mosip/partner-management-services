package io.mosip.pms.partner.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.request.dto.ErrorResponse;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MultiPartnerUtil {
    private static final Logger LOGGER = PMSLogger.getLogger(MultiPartnerUtil.class);
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    public static X509Certificate decodeCertificateData(String certificateData) {
        certificateData = certificateData.replaceAll(BEGIN_CERTIFICATE, "")
                .replaceAll(END_CERTIFICATE, "")
                .replaceAll("\n", "");
        X509Certificate cert = null;
        try {
            byte[] decodedCertificate = Base64.getDecoder().decode(certificateData);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decodedCertificate));
        } catch (Exception ex) {
            LOGGER.error("Could not decode the certificate data :" + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.UNABLE_TO_DECODE_CERTIFICATE.getErrorCode(),
                    ErrorCode.UNABLE_TO_DECODE_CERTIFICATE.getErrorMessage());
        }
        return cert;
    }

    public static List<ErrorResponse> setErrorResponse(String errorCode, String errorMessage) {
        List<ErrorResponse> errorResponseList = new ArrayList<>();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(errorCode);
        errorResponse.setMessage(errorMessage);
        errorResponseList.add(errorResponse);
        return errorResponseList;
    }
}