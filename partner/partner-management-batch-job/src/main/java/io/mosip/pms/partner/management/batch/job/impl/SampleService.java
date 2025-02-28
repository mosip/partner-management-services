package io.mosip.pms.partner.management.batch.job.impl;

import io.mosip.pms.partner.management.batch.job.config.LoggerConfiguration;
import io.mosip.pms.partner.management.batch.job.util.KeyManagerHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SampleService {

    private Logger log = LoggerConfiguration.logConfig(SampleService.class);
    private static final String SAMPLE_PARTNER_CERT_ID = "f6f6e340-d4bc-480f-96e9-94765872f4a9";

    @Autowired
    KeyManagerHelper keyManagerHelper;

    public void getPartnerCertificate() {
        keyManagerHelper.getPartnerCertificate(SAMPLE_PARTNER_CERT_ID);
    }
}
