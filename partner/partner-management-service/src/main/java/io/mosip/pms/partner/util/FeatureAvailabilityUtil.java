package io.mosip.pms.partner.util;

import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeatureAvailabilityUtil {

    @Value("${mosip.pms.root.and.intermediate.certificates.available}")
    private boolean rootAndIntermediateCertsAvailable;

    @Value("${mosip.pms.oidc.client.available}")
    private boolean oidcClientAvailable;

    @Value("${mosip.pms.ca.signed.partner.certificate.available}")
    private boolean caSignedCertAvailable;

    public void validateRootAndIntermediateCertificatesFeatureEnabled() {
        if (!rootAndIntermediateCertsAvailable) {
            throw new PartnerServiceException(
                    ErrorCode.ROOT_AND_INTERMEDIATE_CERTS_DISABLED.getErrorCode(),
                    ErrorCode.ROOT_AND_INTERMEDIATE_CERTS_DISABLED.getErrorMessage()
            );
        }
    }

    public void validateOidcClientFeatureEnabled() {
        if (!oidcClientAvailable) {
            throw new PartnerServiceException(
                    ErrorCode.OIDC_CLIENT_FEATURE_DISABLED.getErrorCode(),
                    ErrorCode.OIDC_CLIENT_FEATURE_DISABLED.getErrorMessage()
            );
        }
    }

    public void validateCaSignedPartnerCertificateFeatureEnabled() {
        if (!caSignedCertAvailable) {
            throw new PartnerServiceException(
                    ErrorCode.CA_SIGNED_CERT_DISABLED.getErrorCode(),
                    ErrorCode.CA_SIGNED_CERT_DISABLED.getErrorMessage()
            );
        }
    }

}
