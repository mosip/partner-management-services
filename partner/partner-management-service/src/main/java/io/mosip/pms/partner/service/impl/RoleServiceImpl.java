package io.mosip.pms.partner.service.impl;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.pms.common.dto.RoleExtnDto;
import io.mosip.pms.partner.service.RoleService;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

	
	@Value("${mosip.pms.ui.required.roles}")
	private String requiredRoles;
	
	@Override
	public RoleExtnDto getUIRequiredRoles() {
		RoleExtnDto roleExtnDto = new RoleExtnDto();
		roleExtnDto.setRoles(Arrays.asList(requiredRoles.split(",")));
		return roleExtnDto;
	}

}
