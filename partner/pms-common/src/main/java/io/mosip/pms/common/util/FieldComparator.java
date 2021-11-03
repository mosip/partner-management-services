package io.mosip.pms.common.util;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;

import io.mosip.pms.common.constant.SearchErrorCode;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.exception.RequestException;
import lombok.AllArgsConstructor;

/**
 * {@link FieldComparator} used to compare two object fields based on the passed
 * {@link SearchSort}
 * 
 * @author Abhishek Kumar
 * @since 1.0.0
 */
@AllArgsConstructor
public class FieldComparator<T> implements Comparator<T> {

	private Field field;
	private SearchSort sort;

	@Override
	public int compare(T o1, T o2) {
		try {
			field.setAccessible(true);
			Class<?> type = field.getType();
			Object value1 = field.get(o1);
			Object value2 = field.get(o2);
			if ("DESC".equalsIgnoreCase(sort.getSortType())) {
				return compare(type, value2, value1);
			} else if ("ASC".equalsIgnoreCase(sort.getSortType())) {
				return compare(type, value1, value2);
			} else {
				throw new RequestException(SearchErrorCode.INVALID_SORT_TYPE.getErrorCode(), String
						.format(SearchErrorCode.INVALID_SORT_TYPE.getErrorMessage(), sort.getSortType()));
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RequestException(SearchErrorCode.ERROR_OCCURED_WHILE_SORTING.getErrorCode(),
					String
					.format(SearchErrorCode.ERROR_OCCURED_WHILE_SORTING.getErrorMessage(), e.getMessage()));
		}

	}

	public int compare(Class<?> type, Object obj1, Object obj2) {
		if (type.equals(String.class)) {
			String value1 = (String) obj1;
			String value2 = (String) obj2;
			return value1.compareTo(value2);
		}
		if ("boolean".equals(type.toString()) || type.equals(Boolean.class)) {
			Boolean value1 = Boolean.class.cast(obj1);
			Boolean value2 = Boolean.class.cast(obj2);
			return value1.compareTo(value2);
		}
		if ("int".equals(type.toString()) || type.equals(Integer.class)) {
			Integer value1 = (Integer) obj1;
			Integer value2 = (Integer) obj2;
			return value1.compareTo(value2);
		}

		if ("double".equals(type.toString()) || type.equals(Double.class)) {
			Double value1 = (Double) obj1;
			Double value2 = (Double) obj2;
			return value1.compareTo(value2);
		}

		if ("long".equals(type.toString()) || type.equals(Long.class)) {
			Long value1 = (Long) obj1;
			Long value2 = (Long) obj2;
			return value1.compareTo(value2);
		}

		if ("float".equals(type.toString()) || type.equals(Float.class)) {
			Float value1 = (Float) obj1;
			Float value2 = (Float) obj2;
			return value1.compareTo(value2);
		}

		if ("short".equals(type.toString()) || type.equals(Short.class)) {
			Short value1 = (Short) obj1;
			Short value2 = (Short) obj2;
			return value1.compareTo(value2);
		}

		if (type.equals(LocalDateTime.class)) {
			LocalDateTime value1 = (LocalDateTime) obj1;
			LocalDateTime value2 = (LocalDateTime) obj2;
			if (value1 == null ^ value2 == null) {
				return (value1 == null) ? -1 : 1;
			}

			if (value1 == null && value2 == null) {
				return 0;
			}

			return value1.compareTo(value2);
		}

		if (type.equals(LocalDate.class)) {
			LocalDate value1 = (LocalDate) obj1;
			LocalDate value2 = (LocalDate) obj2;
			if (value1 == null ^ value2 == null) {
				return (value1 == null) ? -1 : 1;
			}

			if (value1 == null && value2 == null) {
				return 0;
			}

			return value1.compareTo(value2);
		}

		if (type.equals(Date.class)) {
			Date value1 = (Date) obj1;
			Date value2 = (Date) obj2;
			return value1.compareTo(value2);
		}

		if ("float".equals(type.toString()) || type.equals(Float.class)) {
			Float value1 = (Float) obj1;
			Float value2 = (Float) obj2;
			return value1.compareTo(value2);
		}

		return obj1.toString().compareTo(obj2.toString());
	}

}
