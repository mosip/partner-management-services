package io.mosip.pms.batchjob.impl;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.dto.OriginalCertDownloadResponseDto;
import io.mosip.pms.batchjob.util.KeyManagerHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CertificateExpiryService {

    private Logger log = LoggerConfiguration.logConfig(CertificateExpiryService.class);
    private static final String SAMPLE_PARTNER_CERT_ID = "f6f6e340-d4bc-480f-96e9-94765872f4a9";

    @Autowired
    KeyManagerHelper keyManagerHelper;

    public OriginalCertDownloadResponseDto getPartnerCertificate() {
    	return keyManagerHelper.getPartnerCertificate(SAMPLE_PARTNER_CERT_ID);
    }
}
