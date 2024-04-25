package com.developi.utils.collections;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.ibm.xsp.model.DataObject;

public abstract class AbstractMap implements DataObject, Serializable {
	
	private static final long serialVersionUID = 1L;

	private Map<String, Object> values;

	@Override
	public Class<?> getType(Object key) {
		String keyStr = convertKey(key);
		Class<?> result = null;

		if (getValues().containsKey(keyStr)) {
			Object value = getValues().get(keyStr);
			if (value != null) {
				result = value.getClass();
			}
		}
		
		return result;
	}

	@Override
	public Object getValue(Object key) {
		String keyStr = convertKey(key);

		return getValues().get(keyStr);
	}

	@Override
	public boolean isReadOnly(Object key) {
		return false;
	}

	@Override
	public void setValue(Object key, Object value) {
		// Do nothing
	}

	private String convertKey(Object key) {
		if(null==key) {
			return "";
		}
		
		return StringUtils.lowerCase(key.toString(), Locale.ENGLISH);
	}
	
	protected Map<String, Object> getValues() {
		if(null == this.values) {
			this.values = new ConcurrentHashMap<String, Object>();
		}
		
		return this.values;
	}

	
	
	
}
