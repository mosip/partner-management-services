package io.mosip.pms.partner.service.impl;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.CertificateDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.service.AllPartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
public class AllPartnerServiceImpl implements AllPartnerService {

    private static final Logger LOGGER = PMSLogger.getLogger(AllPartnerServiceImpl.class);
    public static final String BLANK_STRING="";
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    @Autowired
    PartnerServiceRepository partnerRepository;

    @Autowired
    PartnerServiceImpl partnerServiceImpl;

    @Override
    public List<CertificateDto> getAllCertificateDetails() {
        List<CertificateDto> certificateDtoList = new ArrayList<>();
        try {
            String userId = getUserId();
            List<Partner> partnerList = partnerRepository.findByUserId(userId);
            if (!partnerList.isEmpty()) {
                for (Partner partner : partnerList) {
                    CertificateDto certificateDto = new CertificateDto();
                    try {
                        if (Objects.isNull(partner.getId()) || partner.getId().equals(BLANK_STRING)) {
                            LOGGER.info("Partner Id is null or empty for user id : " + userId);
                            throw new PartnerServiceException(ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorCode(),
                                    ErrorCode.PARTNER_ID_NOT_EXISTS.getErrorMessage());
                        }
                        PartnerCertDownloadRequestDto requestDto = new PartnerCertDownloadRequestDto();
                        requestDto.setPartnerId(partner.getId());
                        PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = partnerServiceImpl.getPartnerCertificate(requestDto);
                        String certificateData = partnerCertDownloadResponeDto.getCertificateData();
                        certificateData = certificateData.replaceAll(BEGIN_CERTIFICATE, "")
                                .replaceAll(END_CERTIFICATE, "")
                                .replaceAll("\n", "");

                        byte[] decodedCertificate = Base64.getDecoder().decode(certificateData);

                        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decodedCertificate));

                        certificateDto.setIsCertificateAvailable(true);
                        certificateDto.setCertificateName(getCertificateName(cert.getSubjectDN().getName()));
                        certificateDto.setCertificateUploadDate(cert.getNotBefore());
                        certificateDto.setCertificateExpiryDate(cert.getNotAfter());
                        certificateDto.setPartnerId(partner.getId());
                        certificateDto.setPartnerType(partner.getPartnerTypeCode());
                    } catch (PartnerServiceException ex) {
                        LOGGER.info("Could not fetch partner certificate :" + ex.getMessage());
                        certificateDto.setIsCertificateAvailable(false);
                        certificateDto.setPartnerId(partner.getId());
                        certificateDto.setPartnerType(partner.getPartnerTypeCode());
                    }
                    certificateDtoList.add(certificateDto);
                }
            } else {
                LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
                throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
                        ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
            }
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In getAllCertificateDetails method of AllPartnerServiceImpl - " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In getAllCertificateDetails method of AllPartnerServiceImpl - " + ex.getMessage());
            throw new PartnerServiceException(ErrorCode.PARTNER_CERTIFICATES_FETCH_ERROR.getErrorCode(),
                    ErrorCode.PARTNER_CERTIFICATES_FETCH_ERROR.getErrorMessage());
        }
        return certificateDtoList;
    }

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getUserId() {
        String userId = authUserDetails().getUserId();
        return userId;
    }

    public static String getCertificateName(String subjectDN) {
        String[] parts = subjectDN.split(",");
        for (String part : parts) {
            if (part.trim().startsWith("CN=")) {
                return part.trim().substring(3);
            }
        }
        return BLANK_STRING;
    }
}
