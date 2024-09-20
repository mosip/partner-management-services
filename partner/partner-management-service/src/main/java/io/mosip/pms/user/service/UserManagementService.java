package io.mosip.pms.user.service;

import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserDetailsDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;

public interface UserManagementService {
	
	public MosipUserDto registerUser(UserRegistrationRequestDto userToBeRegistred);

	public ResponseWrapperV2<UserDetailsDto> saveUserConsent();

	public ResponseWrapperV2<UserDetailsDto> isUserConsentGiven();
}
