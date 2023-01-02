package io.mosip.pms.common.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.constant.FilterTypeEnum;
import io.mosip.pms.common.constant.OrderEnum;
import io.mosip.pms.common.constant.SearchErrorCode;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.exception.RequestException;
import io.mosip.pms.common.util.UserDetailUtil;

/**
 * Generating dynamic query for partnerManagementData based on the search filters.
 * 
 * @author Tabish Khan
 * @since 1.0.0
 */
@Repository
@Transactional(readOnly = true)
public class SearchHelper {

	private static final String ENTITY_IS_NULL = "entity is null";
	private static final String WILD_CARD_CHARACTER = "%";
	private static final String DECOMISSION = "isDeleted";

	private static final String IS_ACTIVE_COLUMN_NAME = "isActive";
	
	@Value("${mosip.pms.partneradmin.role:PARTNER_ADMIN}")	
	private String partnerAdminRole;  

	/**
	 * Field for interface used to interact with the persistence context.
	 */
	@PersistenceContext
	private EntityManager entityManager;
	
	public <E> Page<E> search(Class<E> entity, SearchDto searchDto, String partnerIdColumn) {		
		if (partnerIdColumn != null) {
			addPartnerFilter(searchDto, partnerIdColumn);
		}
		return search(entity,searchDto);
	}

	/**
	 * Method to search and sort the partnerManagementData.
	 * 
	 * @param entity          the entity class for which search will be applied
	 * @param searchDto       which contains the list of filters, sort and
	 *                        pagination
	 * @param optionalFilters filters to be considered as 'or' statements
	 * 
	 * @return {@link Page} of entity
	 */
	public <E> Page<E> search(Class<E> entity, SearchDto searchDto) {
		long rows = 0l;
		List<E> result;		
		Objects.requireNonNull(entity, ENTITY_IS_NULL);
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> selectQuery = criteriaBuilder.createQuery(entity);
		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

		// root Query
		Root<E> rootQuery = selectQuery.from(entity);
		// count query
		countQuery.select(criteriaBuilder.count(countQuery.from(entity)));
		// applying filters
		if (!searchDto.getFilters().isEmpty())
			filterQuery(criteriaBuilder, rootQuery, selectQuery, countQuery, searchDto.getFilters());

		// applying sorting
		if(!searchDto.getSort().isEmpty())
		sortQuery(criteriaBuilder, rootQuery, selectQuery, searchDto.getSort());

		try {
			// creating executable query from select criteria query
			TypedQuery<E> executableQuery = entityManager.createQuery(selectQuery);
			// creating executable query from count criteria query
			TypedQuery<Long> countExecutableQuery = entityManager.createQuery(countQuery);
			// getting the rows count
			rows = countExecutableQuery.getSingleResult();
			// adding pagination
			paginationQuery(executableQuery, searchDto.getPagination());
			// executing query and returning data
			result = executableQuery.getResultList();
		} catch (Exception hibernateException) {
			if(hibernateException instanceof RequestException) {
				throw new RequestException(((RequestException) hibernateException).getErrors());
			}
			throw new RequestException("PMS-MSD-394",
					String.format(hibernateException.getMessage(), hibernateException.getLocalizedMessage()));
		}
		return new PageImpl<>(result,
				PageRequest.of(searchDto.getPagination().getPageStart(), searchDto.getPagination().getPageFetch()),
				rows);
	}

	/**
	 * Method to add the filters to the criteria query
	 * 
	 * @param builder     used to construct criteria queries
	 * @param root        root type in the from clause,always refers entity
	 * @param selectQuery criteria select query
	 * @param countQuery  criteria count query
	 * @param filters     list of {@link SearchFilter}
	 */
	private <E> void filterQuery(CriteriaBuilder builder, Root<E> root, CriteriaQuery<E> selectQuery,
			CriteriaQuery<Long> countQuery, List<SearchFilter> filters) {
		final List<Predicate> predicates = new ArrayList<>();
		if (filters != null && !filters.isEmpty()) {
			filters.stream().filter(this::validateFilters).map(i -> buildFilters(builder, root, i))
					.filter(Objects::nonNull).collect(Collectors.toCollection(() -> predicates));
		}

		Predicate isDeletedTrue = builder.equal(root.get(DECOMISSION), Boolean.FALSE);
		Predicate isDeletedNull = builder.isNull(root.get(DECOMISSION));
		Predicate isDeleted = builder.or(isDeletedTrue, isDeletedNull);
		predicates.add(isDeleted);
		if (!predicates.isEmpty()) {
			Predicate whereClause = builder.and(predicates.toArray(new Predicate[predicates.size()]));
			selectQuery.where(whereClause);
			countQuery.where(whereClause);
		}

	}



	/**
	 * Method to build {@link Predicate} out the {@link SearchFilter}
	 * 
	 * @param builder used to construct criteria queries
	 * @param root    root type in the from clause,always refers entity
	 * @param filter  search filter
	 * @return {@link Predicate}
	 */
	protected <E> Predicate buildFilters(CriteriaBuilder builder, Root<E> root, SearchFilter filter) {
		String columnName = filter.getColumnName();
		String value = filter.getValue();
		String filterType = filter.getType();
		if(filter.getValue() != null && !filter.getValue().isEmpty() && (filter.getValues() != null) &&!filter.getValues().isEmpty()) {
			throw new RequestException(SearchErrorCode.INVALID_VALUE_VALUES.getErrorCode(),
					SearchErrorCode.INVALID_VALUE_VALUES.getErrorMessage());			
		}
		if (FilterTypeEnum.CONTAINS.name().equalsIgnoreCase(filterType)) {
			Expression<String> lowerCase=null;
			try {
				lowerCase = builder.lower(root.get(columnName));
			} catch (Exception e) {
				throw new RequestException(SearchErrorCode.INVALID_COLUMN.getErrorCode(),
						String.format(SearchErrorCode.INVALID_COLUMN.getErrorMessage(), columnName));
			}
			
			if (value.startsWith("*") && value.endsWith("*")) {
				String replacedValue = (value.substring(1)).substring(0, value.length() - 2);
				return builder.like(lowerCase,
						builder.lower(builder.literal(WILD_CARD_CHARACTER + replacedValue + WILD_CARD_CHARACTER)));
			} else if (value.startsWith("*")) {
				String replacedValue = value.substring(1);
				return builder.like(lowerCase, builder.lower(builder.literal(WILD_CARD_CHARACTER + replacedValue)));
			} else {
				return builder.like(lowerCase,
						builder.lower(builder.literal(WILD_CARD_CHARACTER + value + WILD_CARD_CHARACTER)));
			}
		}
		if (FilterTypeEnum.EQUALS.name().equalsIgnoreCase(filterType)) {
			return buildPredicate(builder, root, columnName, value);
		}
		if (FilterTypeEnum.IN.name().equalsIgnoreCase(filterType)) {
			return buildPredicate(builder, root, columnName, filter.getValues());
		}		
		if (FilterTypeEnum.STARTSWITH.name().equalsIgnoreCase(filterType)) {
			if (value.endsWith("*")) {
				value = value.substring(0, value.length() - 1);
			}
			Expression<String> lowerCase = null;
			try {
				 lowerCase = builder.lower(root.get(columnName));
			} catch (Exception e) {
				throw new RequestException(SearchErrorCode.INVALID_COLUMN.getErrorCode(),
						String.format(SearchErrorCode.INVALID_COLUMN.getErrorMessage(), columnName));
			}
			return builder.like(lowerCase, builder.lower(builder.literal(value + WILD_CARD_CHARACTER)));
		}
		if (FilterTypeEnum.BETWEEN.name().equalsIgnoreCase(filterType)) {
			return setBetweenValue(builder, root, filter);
		}
		return null;
	}

	/**
	 * Method to add sorting statement in criteria query
	 * 
	 * @param builder       used to construct criteria query
	 * @param root          root type in the from clause,always refers entity
	 * @param criteriaQuery query in which sorting to be added
	 * @param sortFilter    by the query to be sorted
	 */
	private <E> void sortQuery(CriteriaBuilder builder, Root<E> root, CriteriaQuery<E> criteriaQuery,
			List<SearchSort> sortFilter) {
		if (sortFilter != null && !sortFilter.isEmpty()) {
			List<Order> orders = sortFilter.stream().filter(this::validateSort).map(i -> {
				Path<Object> path = null;
				try {
					path = root.get(i.getSortField());
				} catch (IllegalArgumentException | IllegalStateException e) {
					throw new RequestException(SearchErrorCode.INVALID_SORT_FIELD.getErrorCode(),
							String.format(SearchErrorCode.INVALID_SORT_FIELD.getErrorMessage(), i.getSortField()));
				}
				if (path != null) {
					if (OrderEnum.asc.name().equalsIgnoreCase(i.getSortType()))
						return builder.asc(root.get(i.getSortField()));
					else if (OrderEnum.desc.name().equalsIgnoreCase(i.getSortType()))
						return builder.desc(root.get(i.getSortField()));
					else {
						throw new RequestException(SearchErrorCode.INVALID_SORT_TYPE.getErrorCode(),
								String.format(SearchErrorCode.INVALID_SORT_TYPE.getErrorMessage(), i.getSortType()));
					}
				}
				return null;

			}).filter(Objects::nonNull).collect(Collectors.toList());
			criteriaQuery.orderBy(orders);
		}
	}

	/**
	 * Method to add pagination in criteria query
	 * 
	 * @param query to be added with pagination
	 * @param page  contains the pagination details
	 */	
	private void paginationQuery(Query query, Pagination page) {
		if (page != null) {
			if (page.getPageStart() < 0 || page.getPageFetch() < 1) {
				throw new RequestException(SearchErrorCode.INVALID_PAGINATION_VALUE.getErrorCode(),
						String.format(SearchErrorCode.INVALID_PAGINATION_VALUE.getErrorMessage(), page.getPageStart(),
								page.getPageFetch()));
			} 
//			else {
//				query.setFirstResult(page.getPageStart() * page.getPageFetch());
//				query.setMaxResults(page.getPageFetch());
//			}
		}
	}

	/**
	 * Method to handle type safe between {@link Predicate}
	 * 
	 * @param builder use to construct the criteria query
	 * @param root    type in the from clause,always refers entity
	 * @param filter  search filter with the between type.
	 * @return {@link Predicate}
	 */
	private <E> Predicate setBetweenValue(CriteriaBuilder builder, Root<E> root, SearchFilter filter) {
		try {
			String columnName = filter.getColumnName();
			Path<Object> path = root.get(columnName);
			Class<? extends Object> type = path.getJavaType();
			String fieldType = type.getTypeName();
			String toValue = filter.getToValue();
			String fromValue = filter.getFromValue();
			if (LocalDateTime.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), DateUtils.parseToLocalDateTime(fromValue),
						DateUtils.convertUTCToLocalDateTime(toValue));
			}
			if (LocalDate.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), LocalDate.parse(fromValue), LocalDate.parse(toValue));
			}
			if (Long.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), Long.parseLong(fromValue), Long.parseLong(toValue));
			}
			if (Integer.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), Integer.parseInt(fromValue), Integer.parseInt(toValue));
			}
			if (Float.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), Float.parseFloat(fromValue), Float.parseFloat(toValue));
			}
			if (Double.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), Double.parseDouble(fromValue),
						Double.parseDouble(toValue));
			}
			if (String.class.getName().equals(fieldType)) {
				return builder.between(root.get(columnName), fromValue, toValue);
			}
		} catch (IllegalArgumentException | IllegalStateException | InvalidDataAccessApiUsageException e) {
			throw new RequestException(SearchErrorCode.INVALID_COLUMN.getErrorCode(),
					String.format(SearchErrorCode.INVALID_COLUMN.getErrorMessage(), filter.getColumnName()));
		}
		return null;
	}

	/**
	 * Method to cast the data into the column type data type
	 * 
	 * @param root   type in the from clause,always refers entity
	 * @param column name of the column
	 * @param value  value to be cast based on the column data type
	 * @return the value
	 */
	private <E> Object parseDataType(Root<E> root, String column, String value) {
		Path<Object> path = root.get(column);
		if (path != null) {
			Class<? extends Object> type = path.getJavaType();
			String fieldType = type.getTypeName();
			if (LocalDateTime.class.getName().equals(fieldType)) {
				return DateUtils.parseToLocalDateTime(value);
			}
			if (LocalDate.class.getName().equals(fieldType)) {
				return LocalDate.parse(value);
			}
			if (Long.class.getName().equals(fieldType)) {
				return Long.parseLong(value);
			}
			if (Integer.class.getName().equals(fieldType)) {
				return Integer.parseInt(value);
			}
			if (Float.class.getName().equals(fieldType)) {
				return Float.parseFloat(value);
			}
			if (Double.class.getName().equals(fieldType)) {
				return Double.parseDouble(value);
			}
			if (Boolean.class.getName().equals(fieldType)) {
				return Boolean.valueOf(value);
			}
			if (Short.class.getName().equals(fieldType)) {
				return Short.valueOf(value);
			}
		}
		return value;
	}

	/**
	 * Method to create the predicate
	 * 
	 * @param builder used to construct criteria query
	 * @param root    type in the from clause,always refers entity
	 * @param column  name of the column
	 * @param value   column value
	 * @return {@link Predicate}
	 */
	private <E> Predicate buildPredicate(CriteriaBuilder builder, Root<E> root, String column, String value) {
		Predicate predicate = null;		
		Path<Object> path = root.get(column);
		if (path != null) {
			Class<? extends Object> type = path.getJavaType();
			String fieldType = type.getTypeName();
			if (LocalDateTime.class.getName().equals(fieldType)) {
				LocalDateTime start = DateUtils.parseToLocalDateTime(value);
				predicate = builder.between(root.get(column), start, start.plusNanos(1000000l));
			} else if (String.class.getName().equals(fieldType)) {
				predicate = builder.equal(builder.lower(root.get(column)), builder.lower(builder.literal(value)));
			} else {
				predicate = builder.equal(root.get(column), parseDataType(root, column, value));
			}
		}
		return predicate;
	}
	
	/**
	 * Method to create the predicate
	 * 
	 * @param builder used to construct criteria query
	 * @param root    type in the from clause,always refers entity
	 * @param column  name of the column
	 * @param value   column value
	 * @return {@link Predicate}
	 */
	private <E> Predicate buildPredicate(CriteriaBuilder builder, Root<E> root, String column, List<String> values) {
		Predicate predicate = null;
		Path<Object> path = root.get(column);
		if (path != null) {	
			predicate = root.get(column).in(values);
		}
		return predicate;
	}

	/**
	 * Validate the filter column and values
	 * 
	 * @param filter search filter to be validated
	 * @return true if valid false otherwise
	 */
	private boolean validateFilters(SearchFilter filter) {
		if (filter != null) {
			if (filter.getColumnName() != null && !filter.getColumnName().trim().isEmpty()) {
				return FilterTypes(filter);
			} else {
				throw new RequestException(SearchErrorCode.MISSING_FILTER_COLUMN.getErrorCode(),
						SearchErrorCode.MISSING_FILTER_COLUMN.getErrorMessage());
			}
		}
		return false;
	}

	private boolean FilterTypes(SearchFilter filter) {
		if (filter.getType() != null && !filter.getType().trim().isEmpty()) {
			if (validateFilter(filter)) {
				return true;
			}
		} else {
			throw new RequestException(SearchErrorCode.FILTER_TYPE_NOT_AVAILABLE.getErrorCode(),
					String.format(SearchErrorCode.FILTER_TYPE_NOT_AVAILABLE.getErrorMessage(), filter.getColumnName()));
		}
		return false;
	}

	/**
	 * Method to validate the individual filter
	 * 
	 * @param filter input filter to be validated
	 * @return true if valid false otherwise
	 */
	private boolean validateFilter(SearchFilter filter) {
		boolean flag = false;
		if (filter.getValue() != null && !filter.getValue().isEmpty() && (filter.getValues() != null) && !filter.getValues().isEmpty()) {
			throw new RequestException(SearchErrorCode.INVALID_VALUE_VALUES.getErrorCode(),
					SearchErrorCode.INVALID_VALUE_VALUES.getErrorMessage());			
		}
		if (FilterTypeEnum.EQUALS.name().equalsIgnoreCase(filter.getType())
				&& filter.getColumnName().equalsIgnoreCase(IS_ACTIVE_COLUMN_NAME)) {
			String value = filter.getValue();
			if (value != null && !value.trim().isEmpty()
					&& (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
				flag = true;
			} else {
				throw new RequestException(SearchErrorCode.INVALID_VALUE.getErrorCode(),
						SearchErrorCode.INVALID_VALUE.getErrorMessage());
			}

		} else if(FilterTypeEnum.IN.name().equalsIgnoreCase(filter.getType())) {
			if(filter.getValues() != null && !filter.getValues().isEmpty() ) {
				flag = true;
			} else {
				throw new RequestException(SearchErrorCode.INVALID_VALUES.getErrorCode(),
						SearchErrorCode.INVALID_VALUES.getErrorMessage());
			}			
		} else if (!FilterTypeEnum.BETWEEN.name().equalsIgnoreCase(filter.getType())) {
			String value = filter.getValue();
			if (value != null && !value.trim().isEmpty()) {
				flag = true;
			} else {
				throw new RequestException(SearchErrorCode.INVALID_VALUE.getErrorCode(),
						SearchErrorCode.INVALID_VALUE.getErrorMessage());
			}
		} else {
			String fromValue = filter.getFromValue();
			String toValue = filter.getToValue();
			if (fromValue != null && !fromValue.trim().isEmpty() && toValue != null && !toValue.trim().isEmpty()) {
				flag = true;
			} else {
				throw new RequestException(SearchErrorCode.INVALID_BETWEEN_VALUES.getErrorCode(), String
						.format(SearchErrorCode.INVALID_BETWEEN_VALUES.getErrorMessage(), filter.getColumnName()));
			}
		}
		return flag;
	}

	/**
	 * Method to validate the Sort Filter
	 * 
	 * @param sort sort filter to be validated
	 * @return true if valid false otherwise
	 */
	private boolean validateSort(SearchSort sort) {
		if (sort != null) {
			String field = sort.getSortField();
			String type = sort.getSortType();
			if (field != null && !field.isEmpty() && type != null && !type.isEmpty()) {
				return true;
			} else {
				throw new RequestException(SearchErrorCode.INVALID_SORT_INPUT.getErrorCode(),
						SearchErrorCode.INVALID_SORT_INPUT.getErrorMessage());
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	private SearchDto addPartnerFilter(SearchDto searchDto, String partnerIdColumn) {
		AuthUserDetails loggedInUserDetails = UserDetailUtil.getLoggedInUserDetails();
		if (isLoggedInUserFilterRequired()) {
			SearchFilter partnerIdSearchFilter = new SearchFilter();
			partnerIdSearchFilter.setColumnName(partnerIdColumn);
			partnerIdSearchFilter.setType("equals");
			partnerIdSearchFilter.setValue(loggedInUserDetails.getUserId());
			searchDto.getFilters().add(partnerIdSearchFilter);
			return searchDto;
		}
		return searchDto;
	}
	
	public boolean isLoggedInUserFilterRequired() {
		return !(UserDetailUtil.getLoggedInUserDetails().getAuthorities().stream()
				.anyMatch(r -> r.getAuthority().equalsIgnoreCase("ROLE_" + partnerAdminRole)));
	}
}
