package io.mosip.pms.device.authdevice.service;

import java.io.IOException;
import java.util.List;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.FtmChipDetailsDto;
import io.mosip.pms.device.dto.FtmChipFilterDto;
import io.mosip.pms.device.response.dto.*;
import io.mosip.pms.partner.response.dto.FtmCertificateDownloadResponseDto;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.DeactivateFtmRequestDto;
import io.mosip.pms.device.request.dto.FtpChipCertDownloadRequestDto;
import io.mosip.pms.device.request.dto.FtpChipCertificateRequestDto;
import io.mosip.pms.device.request.dto.FtpChipDetailDto;
import io.mosip.pms.device.request.dto.FtpChipDetailStatusDto;
import io.mosip.pms.device.request.dto.FtpChipDetailUpdateDto;

@Service
public interface FtpChipDetailService {	
	
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
	public IdDto createFtpChipDetails(FtpChipDetailDto chipDetails);

	/**
	 * Updated the chip make & mode details
	 * @param chipDetails
	 * @return
	 */
	public IdDto updateFtpChipDetails(FtpChipDetailUpdateDto chipDetails);
	
	/**
	 * Changes the chip status
	 * @param chipDetails
	 * @return
	 */
	public String updateFtpChipDetailStatus(FtpChipDetailStatusDto chipDetails);
	
	/**
     * Function to Upload Partner certificates
     * 
     * @param FtpChipCertificateRequestDto partnerCertResponseDto
     * @return {@link FtpCertificateResponseDto} instance
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
    */
    public FtpCertificateResponseDto uploadCertificate(FtpChipCertificateRequestDto ftpChipCertRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;

    /**
     * Function to Download Partner certificates
     * 
     * @param FtpChipCertDownloadRequestDto certDownloadRequestDto
     * @return {@link FtpCertDownloadResponeDto} instance
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
    */
    public FtpCertDownloadResponeDto getCertificate(FtpChipCertDownloadRequestDto certDownloadRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;
    
    /**
     * 
     * @param <E>
     * @param entity
     * @param dto
     * @return
     */
    public <E> PageResponseDto<FTPSearchResponseDto> searchFTPChipDetails(Class<E> entity, DeviceSearchDto dto);

	public ResponseWrapperV2<FtmDetailResponseDto> deactivateFtm(String ftmId, DeactivateFtmRequestDto requestDto);

	public ResponseWrapperV2<FtmCertificateDownloadResponseDto> getFtmCertificateData(String ftmId);

	public ResponseWrapperV2<PageResponseV2Dto<FtmDetailSummaryDto>> getPartnersFtmChipDetails(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, FtmChipFilterDto filterDto);

	public ResponseWrapperV2<List<FtmChipDetailsDto>> ftmChipDetail();

}
