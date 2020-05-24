package com.github.ideahut.qms.shared.core.cache;

import java.util.List;
import java.util.concurrent.Callable;

public interface CacheGroupHandler {

	public <T> T get(Class<T> type, String group, String key, Callable<T> callable);
	
	public <T> T get(Class<T> type, String group, String key);
	
	public <T> T set(Class<T> type, String group, String key, T value);
	
	public void expire(String group, String key, long expiry);
	
	public void delete(String group, String key);
	
	public void clear(String group);
	
	public List<String> keys(String group);
	
	
	public CacheHandler handler();
	
}
