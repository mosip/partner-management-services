package io.mosip.pms.user.service;

import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserDetailsDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;

public interface UserManagementService {
	
	public MosipUserDto registerUser(UserRegistrationRequestDto userToBeRegistred);

	public UserDetailsDto saveUserConsent();

	public UserDetailsDto isUserConsentGiven();
}
