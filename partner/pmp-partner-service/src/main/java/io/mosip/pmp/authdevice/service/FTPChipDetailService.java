package io.mosip.pmp.authdevice.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.pmp.authdevice.dto.FTPChipCertDownloadRequestDto;
import io.mosip.pmp.authdevice.dto.FTPChipCertificateRequestDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailStatusDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.entity.FTPChipDetail;
import io.mosip.pmp.partner.dto.PartnerCertDownloadResponeDto;
import io.mosip.pmp.partner.dto.PartnerCertificateResponseDto;

@Service
public interface FTPChipDetailService {	
	
	/**
	 * 
	 * @param ftpChipId
	 * @return
	 */
	public FTPChipDetail getFtpChipDeatils(String ftpChipId);
	
	/**
	 * Inserts the chip make and model into database
	 * @param chipDetails
	 * @return
	 */
	public IdDto createFtpChipDetails(FTPChipDetailDto chipDetails);

	/**
	 * Updated the chip make & mode details
	 * @param chipDetails
	 * @return
	 */
	public IdDto updateFtpChipDetails(FTPChipDetailUpdateDto chipDetails);
	
	/**
	 * Changes the chip status
	 * @param chipDetails
	 * @return
	 */
	public String updateFtpChipDetailStatus(FTPChipDetailStatusDto chipDetails);
	
	/**
     * Function to Upload Partner certificates
     * 
     * @param FTPChipCertificateRequestDto partnerCertResponseDto
     * @return {@link PartnerCertificateResponseDto} instance
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
    */
    public PartnerCertificateResponseDto uploadPartnerCertificate(FTPChipCertificateRequestDto ftpChipCertRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;

    /**
     * Function to Download Partner certificates
     * 
     * @param FTPChipCertDownloadRequestDto certDownloadRequestDto
     * @return {@link PartnerCertDownloadResponeDto} instance
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
    */
    public PartnerCertDownloadResponeDto getPartnerCertificate(FTPChipCertDownloadRequestDto certDownloadRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;

}
