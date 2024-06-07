
package io.mosip.pms.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pms.common.dto.APIKeyDataPublishDto;
import io.mosip.pms.common.dto.ClientPublishDto;
import io.mosip.pms.common.dto.MISPDataPublishDto;
import io.mosip.pms.common.dto.PartnerDataPublishDto;
import io.mosip.pms.common.dto.PolicyPublishDto;
import io.mosip.pms.common.dto.SearchAuthPolicy;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.BaseEntity;
import io.mosip.pms.common.entity.ClientDetail;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;


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
@SuppressWarnings("unchecked")
public class MapperUtils {

	/*
	 * @Autowired private ObjectMapper mapper;
	 */

	private MapperUtils() {
		super();
	}

	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String SOURCE_NULL_MESSAGE = "source should not be null";
	private static final String DESTINATION_NULL_MESSAGE = "destination should not be null";
	
	public static final String ACTIVE = "ACTIVE";
	public static final String NOTACTIVE = "NOT_ACTIVE";

	/**
	 * This flag is used to restrict copy null values.
	 */
	private static Boolean mapNullValues = Boolean.TRUE;

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

	/*
	 * #############Public method used for mapping################################
	 */

	/**
	 * This method map the values from <code>source</code> to
	 * <code>destination</code> if name and type of the fields inside the given
	 * parameters are same.If any of the parameters are <code>null</code> this
	 * method return <code>null</code>.This method internally check whether the
	 * source or destinationClass is DTO or an Entity type and map accordingly. If
	 * any {@link Collection} type or Entity type field is their then only matched
	 * name fields value will be set but not the embedded IDs and super class
	 * values.
	 * 
	 * @param <S>           is a type parameter
	 * @param <D>           is a type parameter
	 * @param source        which value is going to be mapped
	 * @param destination   where values is going to be mapped
	 * @param mapNullValues by default marked as true so, it will map null values
	 *                      but if marked as false then null values will be ignored
	 * @return the <code>destination</code> object
	 * @throws NullPointerException if either <code>source</code> or
	 *                              <code>destination</code> is null
	 */
	public static <S, D> D map(final S source, D destination, Boolean mapNullValues) {
		MapperUtils.mapNullValues = mapNullValues;
		return map(source, destination);
	}

	/**
	 * This method map the values from <code>source</code> to
	 * <code>destination</code> if name and type of the fields inside the given
	 * parameters are same.If any of the parameters are <code>null</code> this
	 * method return <code>null</code>.This method internally check whether the
	 * source or destinationClass is DTO or an Entity type and map accordingly. If
	 * any {@link Collection} type or Entity type field is their then only matched
	 * name fields value will be set but not the embedded IDs and super class
	 * values.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @return the <code>destination</code> object
	 * @throws NullPointerException if either <code>source</code> or
	 *                              <code>destination</code> is null
	 */
	public static <S, D> D map(final S source, D destination) {
		Objects.requireNonNull(source, SOURCE_NULL_MESSAGE);
		Objects.requireNonNull(destination, DESTINATION_NULL_MESSAGE);
		try {
			mapValues(source, destination);
		} catch (IllegalAccessException | InstantiationException e) {
			throw new DataAccessLayerException("KER-MSD-991", "Exception in mapping vlaues from source : "
					+ source.getClass().getName() + " to destination : " + destination.getClass().getName(), e);
		}
		return destination;
	}

	/**
	 * This method takes <code>source</code> and <code>destinationClass</code>, take
	 * all values from source and create an object of <code>destinationClass</code>
	 * and map all the values from source to destination if field name and type is
	 * same.This method internally check whether the source or destinationClass is
	 * DTO or an Entity type and map accordingly.If any {@link Collection} type or
	 * Entity type field is their then only matched name fields value will be set
	 * but not the embedded IDs and super class values.
	 * 
	 * @param <S>              is a type parameter
	 * @param <D>              is a type parameter
	 * @param source           which value is going to be mapped
	 * @param destinationClass where values is going to be mapped
	 * @return the object of <code>destinationClass</code>
	 * @throws DataAccessLayerException if exception occur during creating of
	 *                                  <code>destinationClass</code> object
	 * @throws NullPointerException     if either <code>source</code> or
	 *                                  <code>destinationClass</code> is null
	 */
	public static <S, D> D map(final S source, Class<D> destinationClass) {
		Objects.requireNonNull(source, SOURCE_NULL_MESSAGE);
		Objects.requireNonNull(destinationClass, "destination class should not be null");
		Object destination = null;
		try {
			destination = destinationClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new DataAccessLayerException("KER-MSD-991", "Exception in mapping vlaues from source : "
					+ source.getClass().getName() + " to destination : " + destinationClass.getClass().getName(), e);
		}
		return (D) map(source, destination);
	}

	/**
	 * This method takes <code>sourceList</code> and <code>destinationClass</code>,
	 * take all values from source and create an object of
	 * <code>destinationClass</code> and map all the values from source to
	 * destination if field name and type is same.
	 * 
	 * @param <S>              is a type parameter
	 * 
	 * @param <D>              is a type parameter
	 * @param sourceList       which value is going to be mapped
	 * @param destinationClass where values is going to be mapped
	 * @return list of destinationClass objects
	 * @throws DataAccessLayerException if exception occur during creating of
	 *                                  <code>destinationClass</code> object
	 * @throws NullPointerException     if either <code>sourceList</code> or
	 *                                  <code>destinationClass</code> is null
	 */
	public static <S, D> List<D> mapAll(final Collection<S> sourceList, Class<D> destinationClass) {
		Objects.requireNonNull(sourceList, "sourceList should not be null");
		Objects.requireNonNull(destinationClass, "destinationClass should not be null");
		return sourceList.stream().map(entity -> map(entity, destinationClass)).collect(Collectors.toList());
	}

	/**
	 * This method map values of <code>source</code> object to
	 * <code>destination</code> object. It will map field values having same name
	 * and same type for the fields. It will not map any field which is static or
	 * final.It will simply ignore those values.
	 * 
	 * @param <S>         is a type parameter
	 * 
	 * @param <D>         is a type parameter
	 * @param source      is any object which should not be null and have data which
	 *                    is going to be copied
	 * @param destination is an object in which source field values is going to be
	 *                    matched
	 * 
	 * @throws DataAccessLayerException if error raised during mapping values
	 * @throws NullPointerException     if either <code>source</code> or
	 *                                  <code>destination</code> is null
	 */
	public static <S, D> void mapFieldValues(S source, D destination) {

		Objects.requireNonNull(source, SOURCE_NULL_MESSAGE);
		Objects.requireNonNull(destination, DESTINATION_NULL_MESSAGE);
		Field[] sourceFields = source.getClass().getDeclaredFields();
		Field[] destinationFields = destination.getClass().getDeclaredFields();

		mapFieldValues(source, destination, sourceFields, destinationFields);

	}

	/**
	 * Map values from {@link BaseEntity} class source object to destination or vice
	 * versa and this method will be used to map {@link BaseEntity} values from
	 * entity to entity. Like when both <code>source</code> and
	 * <code>destination</code> are object which extends {@link BaseEntity}.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 */
	public static <S, D> void setBaseFieldValue(S source, D destination) {
		Objects.requireNonNull(source, SOURCE_NULL_MESSAGE);
		Objects.requireNonNull(destination, DESTINATION_NULL_MESSAGE);
		String sourceSupername = source.getClass().getSuperclass().getName();// super class of source object
		String destinationSupername = destination.getClass().getSuperclass().getName();// super class of destination
		// object
		String baseEntityClassName = source.getClass().getName();// base entity fully qualified name
		String objectClassName = Object.class.getName();// object class fully qualified name

		String baseDtoClassName = source.getClass().getName();// base entity fully qualified name

		if (sourceSupername.equals(baseEntityClassName) && destinationSupername.equals(baseDtoClassName)) {
			Field[] sourceFields = source.getClass().getSuperclass().getDeclaredFields();
			Field[] destinationFields = destination.getClass().getSuperclass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
			sourceFields = source.getClass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
			return;
		}
		if (sourceSupername.equals(baseDtoClassName) && destinationSupername.equals(baseEntityClassName)) {
			Field[] sourceFields = source.getClass().getSuperclass().getDeclaredFields();
			Field[] destinationFields = destination.getClass().getSuperclass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
			destinationFields = destination.getClass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
			return;
		}

		// if source is an entity
		if (sourceSupername.equals(baseEntityClassName) && !destinationSupername.equals(baseEntityClassName)) {
			Field[] sourceFields = source.getClass().getSuperclass().getDeclaredFields();
			Field[] destinationFields = destination.getClass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
		} else if (destinationSupername.equals(baseEntityClassName) && !sourceSupername.equals(baseEntityClassName)) {
			// if destination is an entity
			Field[] sourceFields = source.getClass().getDeclaredFields();
			Field[] destinationFields = destination.getClass().getSuperclass().getDeclaredFields();
			mapFieldValues(source, destination, sourceFields, destinationFields);
		} else {
			if (!sourceSupername.equals(objectClassName) && !destinationSupername.equals(objectClassName)) {
				Field[] sourceFields = source.getClass().getSuperclass().getDeclaredFields();
				Field[] destinationFields = destination.getClass().getSuperclass().getDeclaredFields();
				mapFieldValues(source, destination, sourceFields, destinationFields);
			}
		}

	}

	/*
	 * #############Private method used for mapping################################
	 */

	/**
	 * Map values from source object to destination object.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @throws InstantiationException if not able to create instance of field having
	 *                                annotation {@link EmbeddedId}
	 * @throws IllegalAccessException if provided fields are not accessible
	 */
	private static <S, D> void mapValues(S source, D destination)
			throws IllegalAccessException, InstantiationException {

		mapFieldValues(source, destination);// this method simply map values if field name and type are same

		if (source.getClass().isAnnotationPresent(Entity.class)) {
			mapEntityToDto(source, destination);
		} else {
			mapDtoToEntity(source, destination);
		}
	}

	/**
	 * This method map source DTO to a class object which extends {@link BaseEntity}
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @throws InstantiationException if not able to create instance of field having
	 *                                annotation {@link EmbeddedId}
	 * @throws IllegalAccessException if provided fields are not accessible
	 */
	private static <S, D> void mapDtoToEntity(S source, D destination)
			throws InstantiationException, IllegalAccessException {
		Field[] fields = destination.getClass().getDeclaredFields();
		setBaseFieldValue(source, destination);// map super class values
		for (Field field : fields) {
			/**
			 * Map DTO matching field values to super class field values
			 */
			if (field.isAnnotationPresent(EmbeddedId.class)) {
				Object id = field.getType().newInstance();
				mapFieldValues(source, id);
				field.setAccessible(true);
				field.set(destination, id);
				field.setAccessible(false);
				break;
			}
		}
	}

	/**
	 * Map source which extends {@link BaseEntity} to a DTO object.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @throws IllegalAccessException if provided fields are not accessible
	 */
	private static <S, D> void mapEntityToDto(S source, D destination) throws IllegalAccessException {
		Field[] sourceFields = source.getClass().getDeclaredFields();
		/*
		 * Here source is a Entity so we need to take values from Entity object and set
		 * the matching fields in the destination object mostly an DTO.
		 */
		boolean isIdMapped = false;// a flag to check if there any composite key is present and is mapped
		boolean isSuperMapped = false;// a flag to check is class extends the BaseEntity and is mapped
		for (Field sfield : sourceFields) {
			sfield.setAccessible(true);// mark accessible true because fields my be private, for safety
			if (!isIdMapped && sfield.isAnnotationPresent(EmbeddedId.class)) {
				/**
				 * Map the composite key values from source to destination if field name is same
				 */
				/**
				 * Take the field and get the composite key object and map all values to
				 * destination object
				 */
				mapFieldValues(sfield.get(source), destination);
				sfield.setAccessible(false);
				isIdMapped = true;// set flag so no need to check and map again
			} else if (!isSuperMapped) {
				setBaseFieldValue(source, destination);// this method check whether source is entity or destination
														// and maps values accordingly
				isSuperMapped = true;
			}
		}
	}

	/**
	 * Map values from source field to destination.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @param sf          source fields
	 * @param dtf         destination fields
	 */
	private static <D, S> void mapFieldValues(S source, D destination, Field[] sourceFields,
			Field[] destinationFields) {
		try {
			for (Field sfield : sourceFields) {
				// Do not set values either static or final
				if (Modifier.isStatic(sfield.getModifiers()) || Modifier.isFinal(sfield.getModifiers())) {
					continue;
				}

				// make field accessible possibly private
				sfield.setAccessible(true);

				for (Field dfield : destinationFields) {

					Class<?> sourceType = sfield.getType();
					Class<?> destinationType = dfield.getType();

					// map only those field whose name and type is same
					if (sfield.getName().equals(dfield.getName()) && sourceType.equals(destinationType)) {

						// for normal field values
						dfield.setAccessible(true);
						setFieldValue(source, destination, sfield, dfield);
						break;
					}
				}
			}
		} catch (IllegalAccessException e) {

			throw new DataAccessLayerException("KER-MSD-993", "Exception raised while mapping values form "
					+ source.getClass().getName() + " to " + destination.getClass().getName(), e);
		}
	}

	/**
	 * Take value from source field and insert value into destination field.
	 * 
	 * @param <S>         is a type parameter
	 * @param <D>         is a type parameter
	 * @param source      which value is going to be mapped
	 * @param destination where values is going to be mapped
	 * @param sf          source fields
	 * @param dtf         destination fields
	 * @throws IllegalAccessException if provided fields are not accessible
	 */
	private static <S, D> void setFieldValue(S source, D destination, Field sf, Field dtf)
			throws IllegalAccessException {
		// check whether user wants to map null values into destination object or not
		if (!mapNullValues && EmptyCheckUtils.isNullEmpty(sf.get(source))) {
			return;
		}
		dtf.set(destination, sf.get(source));
		dtf.setAccessible(false);
		sf.setAccessible(false);
	}
	
	
	public static List<SearchAuthPolicy> mapAuthPolicySearch(List<AuthPolicy> authPolicies){
		Objects.requireNonNull(authPolicies);
		List<SearchAuthPolicy> authPoliciesList=new ArrayList<>();
		authPolicies.forEach(authPolicy -> {
			SearchAuthPolicy searchAuthPolicy=new SearchAuthPolicy();
			searchAuthPolicy.setCrBy(authPolicy.getCrBy());
			searchAuthPolicy.setCrDtimes(authPolicy.getCrDtimes());
			searchAuthPolicy.setDelDtimes(authPolicy.getDelDtimes());
			searchAuthPolicy.setDesc(authPolicy.getDescr());
			searchAuthPolicy.setId(authPolicy.getId());
			searchAuthPolicy.setIsActive(authPolicy.getIsActive());
			searchAuthPolicy.setIsDeleted(authPolicy.getIsDeleted());
			searchAuthPolicy.setName(authPolicy.getName());
			searchAuthPolicy.setPolicies(authPolicy.getPolicyFileId());
			searchAuthPolicy.setPolicyGroupId(authPolicy.getPolicyGroup().getId());
			searchAuthPolicy.setPolicyGroupName(authPolicy.getPolicyGroup().getName());
			searchAuthPolicy.setPolicyType(authPolicy.getPolicy_type());
			searchAuthPolicy.setSchema(authPolicy.getPolicySchema());
			searchAuthPolicy.setUpdBy(authPolicy.getUpdBy());
			searchAuthPolicy.setUpdDtimes(authPolicy.getUpdDtimes());
			searchAuthPolicy.setValidFromDate(authPolicy.getValidFromDate());
			searchAuthPolicy.setValidToDate(authPolicy.getValidToDate());
			searchAuthPolicy.setVersion(authPolicy.getVersion());
			authPoliciesList.add(searchAuthPolicy);
		});
		return authPoliciesList;
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
	 * 
	 * @param clientData
	 * @return
	 */
	public static ClientPublishDto mapClientDataToPublishDto(ClientDetail clientData) {
		ClientPublishDto dataToPublish = new ClientPublishDto();
		dataToPublish.setClientId(clientData.getId());
		dataToPublish.setClientName(clientData.getName());
		dataToPublish.setAuthContextRefs(convertStringToList(clientData.getAcrValues()));
		dataToPublish.setClientAuthMethods(convertStringToList(clientData.getClientAuthMethods()));
		dataToPublish.setUserClaims(convertStringToList(clientData.getClaims()));
		dataToPublish.setClientStatus(clientData.getStatus());
		return dataToPublish;
	}
	
	/**
	 * 
	 * @param commaSeparatedString
	 * @return
	 */
	private static List<String> convertStringToList(String commaSeparatedString){
		return Arrays.asList(commaSeparatedString.split(","));
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
