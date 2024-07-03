package io.mosip.pms.partner.util;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class MultiPartnerUtil {
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    public static X509Certificate decodeCertificateData(String certificateData) throws CertificateException {
        certificateData = certificateData.replaceAll(BEGIN_CERTIFICATE, "")
                .replaceAll(END_CERTIFICATE, "")
                .replaceAll("\n", "");

        byte[] decodedCertificate = Base64.getDecoder().decode(certificateData);

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decodedCertificate));
        return cert;
    }
}
