package io.mosip.pms.batchjob.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.pms.batchjob.util.KeyManagerHelper;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;


@Component
public class CertificateExpiryService {

   
    @Autowired
    KeyManagerHelper keyManagerHelper;

    public PartnerCertDownloadResponeDto getPartnerCertificate(String certificateAlias) {
    	return keyManagerHelper.getPartnerCertificate(certificateAlias);
    }
    
	public TrustCertTypeListResponseDto getTrustCertificates(String certificateType,
			LocalDateTime validTillDate) {
    	return keyManagerHelper.getTrustCertificates(certificateType, validTillDate);
    }
}
