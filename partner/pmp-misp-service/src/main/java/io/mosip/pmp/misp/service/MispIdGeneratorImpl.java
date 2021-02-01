package io.mosip.pmp.misp.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.EntityExistsException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.idgenerator.spi.MispIdGenerator;
import io.mosip.kernel.core.util.MathUtils;
import io.mosip.pmp.misp.constant.MispIdPropertyConstant;
import io.mosip.pmp.misp.exception.ErrorMessages;
import io.mosip.pmp.misp.exception.MISPException;
import io.mosip.pms.common.entity.Misp;
import io.mosip.pms.common.repository.MispRepository;

/**
 * This service class contains methods for generating MISPID.
 * 
 * @since 1.0.0
 * @author Nagarjuna
 */
@Component
public class MispIdGeneratorImpl implements MispIdGenerator<String> {

	/**
	 * Length of MispId.
	 */
	@Value("${mosip.kernel.mispid.length}")
	private int mispIdLength;
	/**
	 * The reference to MispRepository.
	 */
	@Autowired
	MispRepository mispRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.idgenerator.spi.MispIdGenerator#generateId()
	 */
	@Override
	public String generateId() {

		int generatedId = 0;

		final int initialValue = MathUtils.getPow(Integer.parseInt(MispIdPropertyConstant.ID_BASE.getProperty()),
				mispIdLength - 1);

		Misp entity = null;

		try {

			entity = mispRepository.findLastMispId();

		} catch (DataAccessLayerException e) {
			throw new MISPException(ErrorMessages.MISPID_FETCH_EXCEPTION.getErrorCode(),
					ErrorMessages.MISPID_FETCH_EXCEPTION.getErrorMessage(), e);
		}

		try {
			if (entity != null) {
				generatedId = entity.getMispId() + 1;
				Misp mispId = new Misp();
				mispId.setMispId(generatedId);
				mispId.setCreatedBy("SYSTEM");
				mispId.setUpdatedBy("SYSTEM");
				mispId.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				mispId.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
				mispRepository.create(mispId);

			} else {
				entity = new Misp();
				entity.setMispId(initialValue);
				entity.setCreatedBy("SYSTEM");
				entity.setUpdatedBy("SYSTEM");
				LocalDateTime createdTime = LocalDateTime.now(ZoneId.of("UTC"));
				entity.setCreatedDateTime(createdTime);
				entity.setUpdatedDateTime(null);
				generatedId = initialValue;
				mispRepository.create(entity);
			}

		} catch (DataAccessLayerException e) {
			if (e.getCause().getClass() == EntityExistsException.class) {
				generateId();
			} else {
				throw new MISPException(ErrorMessages.MISPID_INSERTION_EXCEPTION.getErrorCode(),
						ErrorMessages.MISPID_INSERTION_EXCEPTION.getErrorMessage(), e);
			}
		}

		return String.valueOf(generatedId);

	}

}
