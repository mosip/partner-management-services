package io.mosip.pmp.misp.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.idgenerator.spi.MispIdGenerator;
import io.mosip.pmp.misp.entity.Misp;
import io.mosip.pmp.misp.exception.MISPException;
import io.mosip.pmp.misp.repository.MispRepository;

/**
 * @author Nagarjuna
 * @since 1.0.0
 *
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class MispIdServiceTest {

	@Value("${mosip.kernel.mispid.test.valid-initial-mispid}")
	private int initialMispid;

	@Value("${mosip.kernel.mispid.test.valid-new-mispid}")
	private int newMispId;

	@Autowired
	MispIdGenerator<String> service;

	@MockBean
	MispRepository mispRepository;

	@MockBean
	private RestTemplate restTemplate;

	@Test
	public void generateNewIdTest() {
		Misp entity = new Misp();
		entity.setMispId(initialMispid);
		when(mispRepository.findLastMispId()).thenReturn(null);
		when(mispRepository.create(ArgumentMatchers.any())).thenReturn(entity);
		assertThat(service.generateId(), is(Integer.toString(initialMispid)));
	}

	@Test
	public void generateIdTest() {
		Misp entity = new Misp();
		entity.setMispId(initialMispid);
		when(mispRepository.findLastMispId()).thenReturn(entity);
		when(mispRepository.create(ArgumentMatchers.any())).thenReturn(entity);
		assertThat(service.generateId(), is(Integer.toString(newMispId)));
	}

	@Test(expected = MISPException.class)
	public void generateIdFetchExceptionTest() {
		when(mispRepository.findLastMispId())
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", new RuntimeException()));
		service.generateId();
	}

	@Test(expected = MISPException.class)
	public void generateIdInsertExceptionTest() {
		when(mispRepository.findLastMispId()).thenReturn(null);
		when(mispRepository.create(ArgumentMatchers.any()))
				.thenThrow(new MISPException("", "cannot execute statement", new RuntimeException()));
		service.generateId();
	}

	@Test(expected = MISPException.class)
	public void mispIdServiceFetchExceptionTest() throws Exception {

		when(mispRepository.findLastMispId())
				.thenThrow(new MISPException("", "cannot execute statement", new RuntimeException()));
		service.generateId();
	}

	@Test(expected = MISPException.class)
	public void mispIdServiceInsertExceptionTest() throws Exception {
		when(mispRepository.create(ArgumentMatchers.any()))
				.thenThrow(new MISPException("", "cannot execute statement", new RuntimeException()));
		service.generateId();
	}

	@Test(expected = MISPException.class)
	public void mispIdServiceExceptionTest() throws Exception {
		Misp entity = new Misp();
		entity.setMispId(1000);
		when(mispRepository.findLastMispId()).thenReturn(entity);
		when(mispRepository.create(ArgumentMatchers.any()))
				.thenThrow(new DataAccessLayerException("", "cannot execute statement", new RuntimeException()));
		service.generateId();
	}
}
