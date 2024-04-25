package com.developi.utils.collections;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.developi.utils.ListUtils;

/**
 * Inspired from Howyabean from Tim Tripcony.
 * 
 * Extends AbstractMap in a way that if it looks for properties of the class before the internal map.
 * Also adds typed gets
 * 
 * @author sbasegmez
 *
 */
public abstract class AbstractSmartMap extends AbstractMap {

	private static final long serialVersionUID = 1L;

	@Override
	public Class<?> getType(final Object key) {
		Class<?> result = null;
		try {
			PropertyUtils.getPropertyType(this, key.toString());
		} catch (final Throwable t) {
			result = super.getType(key);
		}
		return result;
	}	
	
	@Override
	public Object getValue(final Object key) {
		Object result = null;
		try {
			result = PropertyUtils.getProperty(this, key.toString());
		} catch (final Throwable t) {
			result = super.getValue(key);
		}
		return result;
	}

	public <E> E getTypedValue(final String key, final Class<E> valueType) {
		return getTypedValue(key, valueType, null);
	}
	
	public <E> E getTypedValue(final String key, final Class<E> valueType, E defaultValue) {
		Object value = getValue(key);
		
		if(null!=value && value.getClass().equals(valueType)) {
			return valueType.cast(value);
		} 

		return defaultValue;
	}
	
	public String getValueString(final String key) {
		return getValueString(key, null);
	}
	
	public String getValueString(final String key, final String defaultValue) {
		Object value = getValue(key);
		
		// For string, we will try to convert it to string.
		if(null == value) {
			return defaultValue;
		} else {
			return String.valueOf(value);
		}
	}
	
	public List<String> getValueStringList(final String key) {
		return getValueStringList(key, Collections.emptyList());
	}
	
	public List<String> getValueStringList(final String key, final List<String> defaultValue) {
		List<?> value = getTypedValue(key, List.class);
		
		return (null == value) ? defaultValue : ListUtils.toStringList((List<?>)value);
	}

	public Calendar getValueCalendar(final String key) {
		return getValueCalendar(key, null);
	}
	
	public Calendar getValueCalendar(final String key, final Calendar defaultValue) {
		return getTypedValue(key, Calendar.class, defaultValue);
	}

	public Date getValueDate(final String key) {
		return getValueDate(key, null);
	}
	
	public Date getValueDate(final String key, final Date defaultValue) {
		return getTypedValue(key, Date.class, defaultValue);
	}
	
	public LocalDate getValueLocalDate(final String key) {
		return getValueLocalDate(key, null);
	}
	
	public LocalDate getValueLocalDate(final String key, final LocalDate defaultValue) {
		return getTypedValue(key, LocalDate.class, defaultValue);
	}
	
	public boolean getValueBoolean(final String key, final boolean defaultValue) {
		Object value = getValue(key);

		if(value==null) {
			return defaultValue;
		} else if(value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		} 
		
		return BooleanUtils.toBoolean(String.valueOf(value));
	}

	public int getValueInteger(final String key, final int defaultValue) {
		Object value = getValue(key);
		
		if(null == value) {
			return defaultValue;
		} else if(value instanceof Number) {
			return ((Number) value).intValue();
		} else {
			if(StringUtils.isNumeric(value.toString())) {
				try {
					return Integer.parseInt(value.toString());
				} catch(NumberFormatException nfe) { }
			}
		}
		
		return defaultValue;
	}

	
}
