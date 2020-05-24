package com.github.ideahut.qms.shared.core.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.github.ideahut.qms.shared.core.bean.InitializationBean;
import com.github.ideahut.qms.shared.core.mapper.DataMapper;
import com.github.ideahut.qms.shared.core.mapper.DataMapperImpl;

public class RedisCacheHandler implements CacheHandler, InitializationBean {

	private static final String NULL = "##__NULL__##";

	private boolean initialized = false;	
	private RedisTemplate<String, String> redisTemplate;
	private DataMapper dataMapper;	

	public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void setDataMapper(DataMapper dataMapper) {
		this.dataMapper = dataMapper;
	}

	@Override
	public void doInitialization() throws Exception {
		if (redisTemplate == null) {
			throw new Exception("redisTemplate is required");
		}
		if(dataMapper == null) {
			dataMapper = new DataMapperImpl(true);
		}
		redisTemplate.afterPropertiesSet();
		initialized = true;
	}

	@Override
	public <T> T get(Class<T> type, String key, long expiry, boolean nullable, Callable<T> callable) {
		initialized();
		ValueOperations<String, String> operations = redisTemplate.opsForValue();
		String value = operations.get(key);
		if (value == null) {
			if (callable != null) {
				try {
					T t = callable.call();
					return set(type, key, t, expiry, nullable);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return null;
		}
		if (NULL.equals(value)) {
			return null;
		}
		return dataMapper.readData(value, type);
	}

	@Override
	public <T> T get(Class<T> type, String key) {
		return get(type, key, 0, false, null);
	}

	@Override
	public <T> T set(Class<T> type, String key, T value, long expiry, boolean nullable) {
		initialized();
		if (!nullable && value == null) {
			return value;
		}
		ValueOperations<String, String> operations = redisTemplate.opsForValue();
		String str = value != null ? dataMapper.writeJsonAsString(value) : NULL;
		operations.set(key, str);
		if (expiry > 0) {
			expire(key, expiry);
		}
		return value;
	}

	@Override
	public <T> T set(Class<T> type, String key, T value, long expiry) {
		return set(type, key, value, expiry, false);
	}

	@Override
	public <T> T set(Class<T> type, String key, T value) {
		return set(type, key, value, 0, false);
	}

	@Override
	public void expire(String key, long expiry) {
		initialized();
		redisTemplate.expire(key, expiry, TimeUnit.SECONDS);
	}

	@Override
	public void delete(String key) {
		initialized();
		redisTemplate.delete(key);
	}
	
	
	private void initialized() {
		if (!initialized) {			
			throw new RuntimeException("Redis cache handler not initialized; call doInitialization() before using it");
		}
	}
	
}
