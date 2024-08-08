package io.mosip.pms.partner.util;

import io.micrometer.core.lang.NonNull;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Component
public class MultiPartnerUtil {

    @Resource
    protected Map<String, String> id;

    @Autowired
    protected Environment env;

    private static final Logger LOGGER = PMSLogger.getLogger(MultiPartnerUtil.class);
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    public X509Certificate decodeCertificateData(String certificateData) {
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

    public void validateId(String operation, String requestId) {
        if (Objects.nonNull(requestId)) {
            if (!requestId.equals(id.get(operation))) {
                LOGGER.error("", "", "validateId", "\n" + "Id is not correct");
                throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_ID.getErrorCode(),
                        ErrorCode.INVALID_REQUEST_ID.getErrorMessage());
            }
        } else {
            LOGGER.error("", "", "validateId", "\n" + "Id is null");
            throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_ID.getErrorCode(),
                    ErrorCode.INVALID_REQUEST_ID.getErrorMessage());
        }
    }

    public void validate(@NonNull Object target) {
        RequestWrapper<Object> request = (RequestWrapper<Object>) target;
        validateReqTime(request.getRequesttime());
        validateVersion(request.getVersion());
        validateRequest(request.getRequest());
    }

    protected void validateReqTime(LocalDateTime reqTime) {
        if (Objects.isNull(reqTime)) {
            LOGGER.error("", "", "validateReqTime", "requesttime is null");
            throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_DATETIME.getErrorCode(),
                    ErrorCode.INVALID_REQUEST_DATETIME.getErrorMessage());
        } else {
            LocalDate localDate = reqTime.toLocalDate();
            LocalDate serverDate = new Date().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
            if (localDate.isBefore(serverDate) || localDate.isAfter(serverDate)) {
                throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE.getErrorCode(),
                        ErrorCode.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE.getErrorMessage());
            }
        }
    }

    protected void validateVersion(String ver) {
        String envVersion = env.getProperty("version");
        if (Objects.isNull(ver)) {
            LOGGER.error("", "", "validateVersion", "version is null");
            throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_VERSION.getErrorCode(),
                    ErrorCode.INVALID_REQUEST_VERSION.getErrorMessage());
        } else if (envVersion != null) {
            if (!envVersion.equalsIgnoreCase(ver)) {
                LOGGER.error("", "", "validateVersion", "version is not correct");
                throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_VERSION.getErrorCode(),
                        ErrorCode.INVALID_REQUEST_VERSION.getErrorMessage());
            }
        }
    }

    protected void validateRequest(Object request) {
        if (Objects.isNull(request)) {
            LOGGER.error("", "", "validateRequest", "\n" + "request is null");
            throw new PartnerServiceException(ErrorCode.INVALID_REQUEST_BODY.getErrorCode(),
                    ErrorCode.INVALID_REQUEST_BODY.getErrorMessage());
        }
    }
}
