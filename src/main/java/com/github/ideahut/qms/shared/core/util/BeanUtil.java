package com.github.ideahut.qms.shared.core.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.microprofile.config.ConfigProvider;

public final class BeanUtil {

	private static final Map<String, Object> singletons = Collections.synchronizedMap(new HashMap<String, Object>());
	
	private BeanUtil() {}
	
	@SuppressWarnings("unchecked")
	public static <T> T singleton(String name, Callable<T> callable) {
		T bean = (T)singletons.get(name);
		if (bean != null) {
			return bean;
		}		
		if (callable != null) {
			try {
				bean = callable.call();
				if (bean == null) {
					throw new Exception("Bean is null for name: " + name);
				}
				singletons.put(name, bean);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}		
		return bean;		
	}
	
	public static <T> T singleton(String name) {
		return singleton(name, null);
	}
	
	
	public static <T> T getConfigValue(Class<T> type, String name, T defaultValue) {
		try {
			T value = ConfigProvider.getConfig().getValue(name, type);
			if (value == null) {
				return defaultValue;
			}
			return value;
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public static <T> T getConfigValue(Class<T> type, String name) {
		return getConfigValue(type, name, null);
	}
	
	public static Map<String, String> getConfigAsMap() {
		Map<String, String> configMap = new HashMap<String, String>();
		Iterator<String> iter = ConfigProvider.getConfig().getPropertyNames().iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			String value = ConfigProvider.getConfig().getValue(name, String.class);
			configMap.put(name, value);
		}
		return configMap;
	}
	
}
