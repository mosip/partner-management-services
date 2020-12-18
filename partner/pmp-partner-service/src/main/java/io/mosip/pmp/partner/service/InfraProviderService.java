package io.mosip.pmp.partner.service;

import java.util.List;

import org.springframework.stereotype.Service;

import io.mosip.pmp.misp.dto.MISPLicenseResponseDto;
import io.mosip.pmp.partner.entity.MISPLicenseEntity;

/**
 * 
 * @author Nagarjuna
 *
 */

/**
 * This interface provide necessary methods to manage mosip infra service providers.
 */
@Service
public interface InfraProviderService {
	
	/**
	 * 
	 * @return
	 */
	public MISPLicenseResponseDto approveInfraProvider(String mispId);
	
	/**
	 * 
	 * @return
	 */
	public MISPLicenseResponseDto updateInfraProvider(String id, String licenseKey, String status);
	
	/**
	 * 
	 * @return
	 */
	public List<MISPLicenseEntity> getInfraProvider();
	
	/**
	 * 
	 * @return
	 */
	public MISPLicenseResponseDto regenerateKey(String mispId);
}
