package io.mosip.pms.user.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.user.service.UserManagementService;

@RestController
@RequestMapping(value = "/users")
public class UserController {

	@Autowired
	UserManagementService userManagementService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MISP_PARTNER','PARTNER_ADMIN','AUTH_PARTNER','CREDENTIAL_PARTNER','ONLINE_VERIFICATION_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','ABIS_PARTNER','MANUAL_ADJUDICATION','SDK_PARTNER')")
	public ResponseWrapper<MosipUserDto> registerUser(
			@RequestBody @Valid RequestWrapper<UserRegistrationRequestDto> request) {
		ResponseWrapper<MosipUserDto> response = new ResponseWrapper<MosipUserDto>();
		response.setResponse(userManagementService.registerUser(request.getRequest()));
		return response;

	}
}
