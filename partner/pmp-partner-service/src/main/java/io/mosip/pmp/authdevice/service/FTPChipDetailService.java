package io.mosip.pmp.authdevice.service;

import org.springframework.stereotype.Service;

import io.mosip.pmp.authdevice.dto.FTPChipDetailDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailStatusDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.IdDto;

@Service
public interface FTPChipDetailService {	
	
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
}
