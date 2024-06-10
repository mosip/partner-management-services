package io.mosip.pms.common.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import io.mosip.pms.common.constant.ValidationErrorCode;
import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.exception.RequestException;



@Repository
public class FilterHelper  {

	private static List<Class<?>> classes = null;

	@Autowired
	private SearchHelper masterdataSearchHelper;
	
	/**
	 * Field for interface used to interact with the persistence context.
	 */
	@PersistenceContext
	private EntityManager entityManager;


	@PostConstruct
	private static void init() {
		classes = new ArrayList<>();
		classes.add(LocalDateTime.class);
		classes.add(LocalDate.class);
		classes.add(LocalTime.class);
		classes.add(Short.class);
		classes.add(Integer.class);
		classes.add(Double.class);
		classes.add(Float.class);
	}

	private static final String MAP_STATUS_COLUMN_NAME = "mapStatus";
	private static final String FILTER_VALUE_UNIQUE = "unique";
	private static final String FILTER_VALUE_ALL = "all";
	private static final String WILD_CARD_CHARACTER = "%";
	private static final String FILTER_VALUE_EMPTY = "";

	@Value("${mosip.pms.filtervalue.max_columns:500}")
	int filterValueMaxColumns;

	@SuppressWarnings("unchecked")
	public <E, T> List<T> filterValues(Class<E> entity, FilterDto filterDto, FilterValueDto filterValueDto) {
		String columnName = filterDto.getColumnName();
		String columnType = filterDto.getType();
		List<Predicate> predicates = new ArrayList<>();
		Predicate caseSensitivePredicate = null;
		if (checkColNameAndType(columnName, columnType)) {
			return (List<T>) valuesForMapStatusColumn();
		}
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<String> criteriaQueryByString = criteriaBuilder.createQuery(String.class);
		Root<E> root = criteriaQueryByString.from(entity);
		Path<Object> path = root.get(filterDto.getColumnName());
		List<T> results;

		CriteriaQuery<T> criteriaQueryByType = criteriaBuilder.createQuery((Class<T>) path.getJavaType());
		Root<E> rootType = criteriaQueryByType.from(entity);

		caseSensitivePredicate = criteriaBuilder.and(criteriaBuilder
				.like(criteriaBuilder.lower(rootType.get(filterDto.getColumnName())), criteriaBuilder.lower(
						criteriaBuilder.literal(WILD_CARD_CHARACTER + filterDto.getText() + WILD_CARD_CHARACTER))));
		if (!(rootType.get(columnName).getJavaType().equals(Boolean.class))) {
			predicates.add(caseSensitivePredicate);
		}
		criteriaQueryByType.select(rootType.get(columnName));
		buildOptionalFilter(criteriaBuilder, rootType, filterValueDto.getOptionalFilters(), predicates);
		columnTypeValidator(rootType, columnName);

		Predicate filterPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
		criteriaQueryByType.where(filterPredicate);
		criteriaQueryByType.orderBy(criteriaBuilder.asc(rootType.get(columnName)));

		// check if column type is boolean then return true/false
		if (checkColNameTypeAndRootType(columnName, columnType, rootType)) {
			return (List<T>) valuesForStatusColumn();
		}

		if (columnType.equals(FILTER_VALUE_UNIQUE) || columnType.equals(FILTER_VALUE_EMPTY)) {
			criteriaQueryByType.distinct(true);
		} else if (columnType.equals(FILTER_VALUE_ALL)) {
			criteriaQueryByType.distinct(false);
		}
		 
		TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQueryByType);
		results = typedQuery.setMaxResults(filterValueMaxColumns).getResultList();
		return results;

	}

	private boolean checkColNameAndType(String columnName, String columnType) {
		return columnName.equals(MAP_STATUS_COLUMN_NAME)
				&& (columnType.equals(FILTER_VALUE_UNIQUE) || columnType.equals(FILTER_VALUE_ALL));
	}

	private <E> boolean checkColNameTypeAndRootType(String columnName, String columnType, Root<E> rootType) {
		return rootType.get(columnName).getJavaType().equals(Boolean.class) && (columnType.equals(FILTER_VALUE_UNIQUE)
				|| columnType.equals(FILTER_VALUE_ALL) || columnType.equals(FILTER_VALUE_EMPTY));
	}

	public <E> List<FilterData> filterValuesWithCode(Class<E> entity, FilterDto filterDto,
			FilterValueDto filterValueDto, String fieldCodeColumnName) {

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		String[] columnNames = filterDto.getColumnName().split(",");
		String columnName = filterDto.getColumnName().split(",")[0];
		String columnType = filterDto.getType();
		Predicate caseSensitivePredicate = null;
		List<FilterData> results;
		List<Predicate> predicates = new ArrayList<>();
		CriteriaQuery<FilterData> criteriaQueryByType = criteriaBuilder.createQuery(FilterData.class);
		Root<E> rootType = criteriaQueryByType.from(entity);

		if(!filterDto.getText().isEmpty()){
			caseSensitivePredicate = criteriaBuilder.and(criteriaBuilder
					.like(criteriaBuilder.lower(rootType.get(columnName)), criteriaBuilder.lower(
							criteriaBuilder.literal(WILD_CARD_CHARACTER + filterDto.getText() + WILD_CARD_CHARACTER))));
		}

		if (columnNames.length > 1) {
			criteriaQueryByType.multiselect(rootType.get(fieldCodeColumnName), rootType.get(columnNames[0]),
					rootType.get(columnNames[1]), rootType.get(columnNames[2]));
		} else {
			criteriaQueryByType.multiselect(rootType.get(fieldCodeColumnName), rootType.get(columnName));
		}

		columnTypeValidator(rootType, columnName);
		if (!(rootType.get(columnName).getJavaType().equals(Boolean.class))) {
			if(caseSensitivePredicate!=null){
				predicates.add(caseSensitivePredicate);
			}
		}
		buildOptionalFilter(criteriaBuilder, rootType, filterValueDto.getOptionalFilters(), predicates);
		Predicate filterPredicate = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
		criteriaQueryByType.where(filterPredicate);
		criteriaQueryByType.orderBy(criteriaBuilder.asc(rootType.get(columnName)));

		// if column type is Boolean then add value as true or false
		if (rootType.get(columnName).getJavaType().equals(Boolean.class) && (columnType.equals(FILTER_VALUE_UNIQUE)
				|| columnType.equals(FILTER_VALUE_ALL) || columnType.equals(FILTER_VALUE_EMPTY))) {
			return valuesForStatusColumnCode();
		}

		// if column type is other than Boolean
		if (columnType.equals(FILTER_VALUE_UNIQUE) || columnType.equals(FILTER_VALUE_EMPTY)) {
			criteriaQueryByType.distinct(true);
		} else if (columnType.equals(FILTER_VALUE_ALL)) {
			criteriaQueryByType.distinct(false);
		}		
		
		TypedQuery<FilterData> typedQuery = entityManager.createQuery(criteriaQueryByType);
		results = typedQuery.setMaxResults(filterValueMaxColumns).getResultList();
		return results;

	}
	
	private <E> void columnTypeValidator(Root<E> root, String columnName) {
		if (classes.contains(root.get(columnName).getJavaType())) {
			throw new RequestException(ValidationErrorCode.FILTER_COLUMN_NOT_SUPPORTED.getErrorCode(),
					ValidationErrorCode.FILTER_COLUMN_NOT_SUPPORTED.getErrorMessage());

		}
	}

	private List<FilterData> valuesForStatusColumnCode() {
		FilterData trueFilterData = new FilterData("", "true");
		FilterData falseFilterData = new FilterData("", "false");
		List<FilterData> filterDataList = new ArrayList<>();
		filterDataList.add(trueFilterData);
		filterDataList.add(falseFilterData);
		return filterDataList;
	}

	private List<String> valuesForStatusColumn() {
		List<String> filterDataList = new ArrayList<>();
		filterDataList.add("true");
		filterDataList.add("false");
		return filterDataList;
	}

	private List<String> valuesForMapStatusColumn() {
		List<String> filterDataList = new ArrayList<>();
		filterDataList.add("Assigned");
		filterDataList.add("Unassigned");
		return filterDataList;
	}

	private <E> void buildOptionalFilter(CriteriaBuilder builder, Root<E> root,
			final List<SearchFilter> optionalFilters, List<Predicate> predicates) {
		if (optionalFilters != null && !optionalFilters.isEmpty()) {
			List<Predicate> optionalPredicates = optionalFilters.stream().map(i -> masterdataSearchHelper.buildFilters(builder, root, i))
					.filter(Objects::nonNull).collect(Collectors.toList());
			if (!optionalPredicates.isEmpty()) {
				Predicate andPredicate = builder
						.and(optionalPredicates.toArray(new Predicate[optionalPredicates.size()]));
				predicates.add(andPredicate);
			}
		}
	}
}
