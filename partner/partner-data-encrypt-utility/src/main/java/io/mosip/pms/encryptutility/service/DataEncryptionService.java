package io.mosip.pms.encryptutility.service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerContact;
import io.mosip.pms.common.entity.PartnerH;
import io.mosip.pms.common.repository.PartnerContactRepository;
import io.mosip.pms.common.repository.PartnerHRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.encryptutility.util.KeyManagerUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DataEncryptionService {

    private static final Logger LOGGER = PMSLogger.getLogger(DataEncryptionService.class);
    public static final String SYSTEM = "SYSTEM";

    @Autowired
    private PartnerServiceRepository partnerServiceRepository;

    @Autowired
    PartnerHRepository partnerHRepository;

    @Autowired
    PartnerContactRepository partnerContactRepository;

    @Autowired
    private KeyManagerUtil keyManagerUtil;

    /**
     * Encrypts all sensitive data for partners, partner history, and partner contacts.
     */
    public void encryptData() {
        LOGGER.info("DataEncryptionService: encryptData - START");
        try {
            List<Partner> partners = encryptPartnerData();
            List<PartnerH> partnerHList = encryptPartnerHData();
            List<PartnerContact> partnerContacts = encryptPartnerContactData();
            LOGGER.info("DataEncryptionService: Encryption completed - Partner records: {}, PartnerH records: {}, PartnerContact records: {}",
                    partners.size(), partnerHList.size(), partnerContacts.size());
            LOGGER.info("DataEncryptionService: encryptData - END");
        } catch (Exception ex) {
            LOGGER.error("An error occurred while encrypting data. Error: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Encrypts existing Partner entities.
     */
    private List<Partner> encryptPartnerData() {
        LOGGER.info("Starting encryption process for partner data.");
        List<Partner> partnerList = partnerServiceRepository.findPartnersWithNullEmailHash();

        if (partnerList.isEmpty()) {
            LOGGER.info("No partner records found for encryption.");
            return partnerList;
        }

        for (Partner partner : partnerList) {
            String emailId = partner.getEmailId();

            if (emailId == null || emailId.trim().isEmpty()) {
                LOGGER.info("Skipping encryption for Partner record with ID: {} due to missing email ID.", partner.getId());
                continue;
            }

            partner.setEmailId(keyManagerUtil.encryptData(emailId));
            partner.setAddress(keyManagerUtil.encryptData(partner.getAddress()));
            partner.setContactNo(keyManagerUtil.encryptData(partner.getContactNo()));
            partner.setEmailIdHash(DigestUtils.sha256Hex(emailId.toLowerCase()));
            partner.setUpdBy(SYSTEM);
            partner.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
            LOGGER.info("Encrypted partner data successfully for Partner ID: {}", partner.getId());
        }

        partnerServiceRepository.saveAll(partnerList);
        LOGGER.info("Successfully saved {} encrypted partner record(s).", partnerList.size());
        return partnerList;
    }

    /**
     * Encrypts existing PartnerH entities.
     */
    private List<PartnerH> encryptPartnerHData() {
        LOGGER.info("Starting encryption process for partner history data.");
        List<PartnerH> partnerHList = partnerHRepository.findPartnersWithNullEmailHash();

        if (partnerHList.isEmpty()) {
            LOGGER.info("No PartnerH records found for encryption.");
            return partnerHList;
        }

        for (PartnerH partnerH : partnerHList) {
            String emailId = partnerH.getEmailId();

            if (emailId == null || emailId.trim().isEmpty()) {
                LOGGER.warn("Skipping encryption for PartnerH record with ID: {} due to missing email ID.", partnerH.getId());
                continue;
            }

            partnerH.setEmailId(keyManagerUtil.encryptData(emailId));
            partnerH.setAddress(keyManagerUtil.encryptData(partnerH.getAddress()));
            partnerH.setContactNo(keyManagerUtil.encryptData(partnerH.getContactNo()));
            partnerH.setEmailIdHash(DigestUtils.sha256Hex(emailId.toLowerCase()));
            partnerH.setUpdBy(SYSTEM);
            partnerH.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
            LOGGER.info("Encrypted partner history data successfully for ID: {}", partnerH.getId().getId());
        }

        partnerHRepository.saveAll(partnerHList);
        LOGGER.info("Successfully saved {} encrypted partner history record(s).", partnerHList.size());
        return partnerHList;
    }

    /**
     * Encrypts existing PartnerContact entities.
     */
    private List<PartnerContact> encryptPartnerContactData() {
        LOGGER.info("Starting encryption process for partner contact data.");
        List<PartnerContact> contactList = partnerContactRepository.findAllWithNullEmailIdHash();

        if (contactList.isEmpty()) {
            LOGGER.info("No PartnerContact records found for encryption.");
            return contactList;
        }

        for (PartnerContact contact : contactList) {
            String emailId = contact.getEmailId();

            if (emailId == null || emailId.trim().isEmpty()) {
                LOGGER.warn("Skipping encryption for PartnerContact record with ID: {} due to missing email ID.", contact.getId());
                continue;
            }

            contact.setEmailId(keyManagerUtil.encryptData(emailId));
            contact.setEmailIdHash(DigestUtils.sha256Hex(emailId.toLowerCase()));
            contact.setContactNo(keyManagerUtil.encryptData(contact.getContactNo()));
            contact.setAddress(keyManagerUtil.encryptData(contact.getAddress()));
            contact.setUpdBy(SYSTEM);
            contact.setUpdDtimes(LocalDateTime.now());
            LOGGER.info("Encrypted partner contact data successfully for Partner ID: {}", contact.getId());
        }

        partnerContactRepository.saveAll(contactList);
        LOGGER.info("Successfully saved {} encrypted partner contact(s).", contactList.size());
        return contactList;
    }

}
