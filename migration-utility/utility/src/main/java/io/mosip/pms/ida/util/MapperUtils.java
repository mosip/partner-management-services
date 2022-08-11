
package io.mosip.pms.ida.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import io.mosip.pms.ida.dao.AuthPolicy;
import io.mosip.pms.ida.dao.MISPLicenseEntity;
import io.mosip.pms.ida.dao.Partner;
import io.mosip.pms.ida.dao.PartnerPolicy;
import io.mosip.pms.ida.dto.APIKeyDataPublishDto;
import io.mosip.pms.ida.dto.MISPDataPublishDto;
import io.mosip.pms.ida.dto.PartnerDataPublishDto;
import io.mosip.pms.ida.dto.PolicyPublishDto;



/**
 * MapperUtils class provides methods to map or copy values from source object
 * to destination object.
 * 
 * @author Bal Vikash Sharma
 * @author Urvil Joshi
 * @since 1.0.0
 * @see MapperUtils
 *
 */
@Component
public class MapperUtils {

	/*
	 * @Autowired private ObjectMapper mapper;
	 */

	private MapperUtils() {
		super();
	}

	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	public static final String ACTIVE = "ACTIVE";
	public static final String NOTACTIVE = "NOT_ACTIVE";


	/**
	 * Parse a date string of pattern UTC_DATETIME_PATTERN into
	 * {@link LocalDateTime}
	 * 
	 * @param dateTime of type {@link String} of pattern UTC_DATETIME_PATTERN
	 * @return a {@link LocalDateTime} of given pattern
	 */
	public static LocalDateTime parseToLocalDateTime(String dateTime) {
		return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
	}	

	
	/**
	 * 
	 * @param entity
	 * @return
	 */
	public static PolicyPublishDto mapPolicyToPublishDto(AuthPolicy entity,JSONObject policy) {
		PolicyPublishDto dataToPublish = new PolicyPublishDto();
		dataToPublish.setPolicy(policy);
		dataToPublish.setPolicyCommenceOn(entity.getValidFromDate());
		dataToPublish.setPolicyDescription(entity.getDescr());
		dataToPublish.setPolicyExpiresOn(entity.getValidToDate());
		dataToPublish.setPolicyId(entity.getId());
		dataToPublish.setPolicyName(entity.getName());
		dataToPublish.setPolicyStatus(entity.getIsActive() == true ? "ACTIVE" : "DEACTIVE");
		return dataToPublish;
	}
	
	/**
	 * 
	 * @param entity
	 * @param certData
	 * @return
	 */
	public static PartnerDataPublishDto mapDataToPublishDto(Partner entity, String partnerCert) {
		PartnerDataPublishDto dataToPublish= new PartnerDataPublishDto();		
		dataToPublish.setPartnerId(entity.getId());
		dataToPublish.setPartnerName(entity.getName());
		dataToPublish.setPartnerStatus(entity.getIsActive() == true? "ACTIVE" : "DEACTIVE");
		dataToPublish.setCertificateData(partnerCert);
		return dataToPublish;
	}
	
	/**
	 * 
	 * @param entity
	 * @return
	 */
	public static APIKeyDataPublishDto mapKeyDataToPublishDto(PartnerPolicy entity) {
		APIKeyDataPublishDto dataToPublish = new APIKeyDataPublishDto();
		dataToPublish.setApiKeyCommenceOn(toISOFormat(entity.getValidFromDatetime().toLocalDateTime()));
		dataToPublish.setApiKeyExpiresOn(toISOFormat(entity.getValidToDatetime().toLocalDateTime()));
		dataToPublish.setApiKeyId(entity.getPolicyApiKey());
		dataToPublish.setApiKeyStatus(entity.getIsActive() == true ? "ACTIVE" : "DEACTIVE");
		return dataToPublish;
	}
	
	/**
	 * Data to publish websub on changes of misp license
	 * @param entity
	 * @return
	 */
	public static MISPDataPublishDto mapDataToPublishDto(MISPLicenseEntity entity) {
		MISPDataPublishDto dataToPublish = new MISPDataPublishDto();
		dataToPublish.setLicenseKey(entity.getLicenseKey());
		dataToPublish.setMispCommenceOn(entity.getValidFromDate());
		dataToPublish.setMispExpiresOn(entity.getValidToDate());
		dataToPublish.setMispId(entity.getMispId());
		dataToPublish.setMispStatus(entity.getIsActive() == true ? ACTIVE: NOTACTIVE);
		return dataToPublish;
	}
	
	private static LocalDateTime toISOFormat(LocalDateTime localDateTime) {
		ZonedDateTime zonedtime = localDateTime.atZone(ZoneId.systemDefault());
		ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneOffset.UTC);
		return converted.toLocalDateTime();
	}
}
