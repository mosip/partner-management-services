package io.mosip.pms.partner_batch_job.impl;

import io.mosip.pms.partner_batch_job.config.LoggerConfiguration;
import io.mosip.pms.partner_batch_job.dto.OriginalCertDownloadResponseDto;
import io.mosip.pms.partner_batch_job.util.KeyManagerHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;


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
