package io.mosip.pms.test.partner.service.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.pms.partner.service.impl.RoleServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleServiceImplTest {

	@Autowired
	RoleServiceImpl roleServiceImpl;
	
	@Test
	public void getUIRequiredRoles() {
		assertTrue(roleServiceImpl.getUIRequiredRoles().getRoles().contains("MISP_Partner"));
	}
}
