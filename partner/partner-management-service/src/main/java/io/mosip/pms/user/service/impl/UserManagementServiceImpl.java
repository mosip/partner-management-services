package io.mosip.pms.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.partner.keycloak.service.KeycloakImpl;
import io.mosip.pms.user.service.UserManagementService;

@Service
public class UserManagementServiceImpl implements UserManagementService{

	@Autowired
	KeycloakImpl keycloakService;
	
	@Override
	public MosipUserDto registerUser(UserRegistrationRequestDto userToBeRegistred) {
		return keycloakService.registerUser(userToBeRegistred);
	}	

}
