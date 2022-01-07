package io.mosip.pms.test.partner.controller;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.mosip.pms.common.dto.RoleExtnDto;
import io.mosip.pms.partner.service.RoleService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class RoleControllerTest {

	@MockBean
	RoleService roleService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void getUIRequiredRolesTest() throws Exception {
		RoleExtnDto response = new RoleExtnDto();
		response.setRoles(List.of("MISP_Partner","Auth_Partner"));
		Mockito.when(roleService.getUIRequiredRoles()).thenReturn(response);
		mockMvc.perform(MockMvcRequestBuilders.get("/roles")).andExpect(MockMvcResultMatchers.status().isOk());
	}
}
