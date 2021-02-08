package io.mosip.pmp.authdevice.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.pmp.authdevice.dto.DeviceSearchDto;
import io.mosip.pmp.authdevice.dto.FTPChipCertDownloadRequestDto;
import io.mosip.pmp.authdevice.dto.FTPChipCertificateRequestDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailStatusDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.FTPSearchResponseDto;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.entity.FTPChipDetail;
import io.mosip.pmp.partner.dto.PartnerCertDownloadResponeDto;
import io.mosip.pmp.partner.dto.PartnerCertificateResponseDto;
import io.mosip.pms.common.dto.PageResponseDto;

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
	 * 
	 * @param ftpChipCertRequestDto
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
    public PartnerCertificateResponseDto uploadPartnerCertificate(FTPChipCertificateRequestDto ftpChipCertRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;
    
    /**
     * 
     * @param certDownloadRequestDto
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws JsonProcessingException
     * @throws IOException
     */
    public PartnerCertDownloadResponeDto getPartnerCertificate(FTPChipCertDownloadRequestDto certDownloadRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;
    
    /**
     * 
     * @param <E>
     * @param entity
     * @param dto
     * @return
     */
    public <E> PageResponseDto<FTPSearchResponseDto> searchFTPChipDetails(Class<E> entity, DeviceSearchDto dto);

}
