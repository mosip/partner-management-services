package io.mosip.pms.partner.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pms.common.dto.RoleExtnDto;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.partner.service.RoleService;

@RestController
public class RoleController {

	@Autowired
	RoleService roleService;

	@GetMapping("/roles")
	private ResponseWrapper<RoleExtnDto> getRequiredRoles() {
		ResponseWrapper<RoleExtnDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(roleService.getUIRequiredRoles());
		return responseWrapper;
	}
}
