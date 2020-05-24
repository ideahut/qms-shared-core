package com.github.ideahut.qms.shared.core.cache;

import java.util.concurrent.Callable;

public interface CacheHandler {

	public <T> T get(Class<T> type, String key, long expiry, boolean nullable, Callable<T> callable);
	
	public <T> T get(Class<T> type, String key);
	
	public <T> T set(Class<T> type, String key, T value, long expiry, boolean nullable);
	
	public <T> T set(Class<T> type, String key, T value, long expiry);
	
	public <T> T set(Class<T> type, String key, T value);
	
	public void expire(String key, long expiry);
	
	public void delete(String key);
	
	
	
}
