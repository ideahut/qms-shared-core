package com.github.ideahut.qms.shared.core.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.data.redis.core.RedisTemplate;

import com.github.ideahut.qms.shared.core.bean.InitializationBean;
import com.github.ideahut.qms.shared.core.mapper.DataMapper;
import com.github.ideahut.qms.shared.core.mapper.DataMapperImpl;
import com.github.ideahut.qms.shared.core.task.TaskHandler;

public class RedisCacheGroupHandler implements CacheGroupHandler, InitializationBean {
	
	private static final String PREFIX = "##__KEYS__##";
	
	private boolean initialized = false; 
	
	private Map<String, CacheGroupProperties> mapGroups;	
	private RedisCacheHandler redisCacheHandler;	
	private RedisTemplate<String, String> redisTemplate;	
	private TaskHandler taskHandler;
	private DataMapper dataMapper;	
	private List<CacheGroupProperties> groups;	
	
	public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void setTaskHandler(TaskHandler taskHandler) {
		this.taskHandler = taskHandler;
	}

	public void setDataMapper(DataMapper dataMapper) {
		this.dataMapper = dataMapper;
	}

	public void setGroups(List<CacheGroupProperties> groups) {
		this.groups = groups;
	}

	@Override
	public void doInitialization() throws Exception {
		if (groups == null || groups.isEmpty()) {
			throw new Exception("Cahce groups is required");
		}
		if (redisTemplate == null) {
			throw new Exception("redisTemplate is required");
		}
		if (taskHandler == null) {
			throw new Exception("taskHandler is required");
		}
		if (dataMapper == null) {
			dataMapper = new DataMapperImpl(false);
		}		
		redisCacheHandler = new RedisCacheHandler();
		redisCacheHandler.setDataMapper(dataMapper);
		redisCacheHandler.setRedisTemplate(redisTemplate);		
		redisCacheHandler.doInitialization();		
		mapGroups = new HashMap<String, CacheGroupProperties>();
		for (CacheGroupProperties group : groups) {
			String name = group.name != null ? group.name.trim() : "";
			if (name.isEmpty()) {
				throw new Exception("Cahce group name is required");
			}
			if (mapGroups.containsKey(name)) {
				throw new Exception("Duplicate cache group name: " + name);
			}
			mapGroups.put(name, group);
			registerGroup(name);
		}		
		initialized = true;
	}

	@Override
	public <T> T get(Class<T> type, String group, String key, Callable<T> callable) {
		GroupParams params = getParams(group, key);
		Integer expiry = params.properties.expiry.orElse(0);
		Boolean nullable = params.properties.nullable.orElse(Boolean.FALSE);		
		return redisCacheHandler.get(type, params.key, expiry, nullable, callable);
	}

	@Override
	public <T> T get(Class<T> type, String group, String key) {
		return get(type, group, key, null);
	}

	@Override
	public <T> T set(Class<T> type, String group, String key, T value) {
		GroupParams params = getParams(group, key);
		Integer expiry = params.properties.expiry.orElse(0);
		Boolean nullable = params.properties.nullable.orElse(Boolean.FALSE);
		T result = redisCacheHandler.set(type, params.key, value, expiry, nullable);
		Integer limit = params.properties.limit.orElse(0);
		if (limit > 0l) {
			taskHandler.execute(new Runnable() {
				@Override
				public void run() {
					String name = params.properties.name;
					putGroupKey(name, key);
					checkGroupLimit(name, limit);			
				}
			});
		}
		return result;
	}

	@Override
	public void expire(String group, String key, long expiry) {
		GroupParams params = getParams(group, key);
		redisCacheHandler.expire(params.key, expiry);
	}

	@Override
	public void delete(String group, String key) {
		GroupParams params = getParams(group, key);
		redisCacheHandler.delete(params.key);
		taskHandler.execute(new Runnable() {
			@Override
			public void run() {
				removeGroupKey(params.properties.name, key);
			}			
		});
	}

	@Override
	public void clear(String group) {
		CacheGroupProperties properties = getProperties(group);
		taskHandler.execute(new Runnable() {
			@Override
			public void run() {
				List<String> gkeys = keys(properties.name);
				while (!gkeys.isEmpty()) {
					String gkey = gkeys.remove(0);
					redisCacheHandler.delete(gkey);
				}
				redisCacheHandler.set(List.class, PREFIX + properties.name, gkeys);
			}			
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> keys(String group) {
		CacheGroupProperties properties = getProperties(group);
		return redisCacheHandler.get(List.class, PREFIX + properties.name);
	}
	
	@Override
	public CacheHandler handler() {
		return redisCacheHandler;
	}
	
	
	private CacheGroupProperties getProperties(String group) {
		initialized();
		CacheGroupProperties properties = mapGroups.get(group);
		if (properties == null) {
			throw new RuntimeException("Cache group is not registered, for: " + group);
		}
		return properties;
	}
	
	private GroupParams getParams(String group, String key) {
		CacheGroupProperties properties = getProperties(group);
		GroupParams groupParams = new GroupParams();
		groupParams.key = key + "@" + properties.name;
		groupParams.properties = properties;
		return groupParams;
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void checkGroupLimit(String name, int limit) {
		List<String> groupKeys = redisCacheHandler.get(List.class, PREFIX + name);
		if (groupKeys == null) {
			groupKeys = new ArrayList<String>();
		}
		int size = groupKeys.size();
		int diff = size - limit;
		if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				String groupKey = groupKeys.remove(0); // FIFO
				redisCacheHandler.delete(groupKey);
			}
			redisCacheHandler.set(List.class, PREFIX + name, groupKeys);
		}
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void removeGroupKey(String name, String key) {
		List<String> groupKeys = redisCacheHandler.get(List.class, PREFIX + name);
		if (groupKeys == null) {
			groupKeys = new ArrayList<String>();
		}
		groupKeys.remove(key + "@" + name);
		redisCacheHandler.set(List.class, PREFIX + name, groupKeys);
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void registerGroup(String name) {
		List<String> groupKeys = redisCacheHandler.get(List.class, PREFIX + name);
		if (groupKeys == null) {
			redisCacheHandler.set(List.class, PREFIX + name, new ArrayList<String>());
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private synchronized void putGroupKey(String name, String key) {
		List<String> groupKeys = redisCacheHandler.get(List.class, PREFIX + name);
		if (groupKeys == null) {
			groupKeys = new ArrayList<String>();
		}
		groupKeys.add(key + "@" + name);
		redisCacheHandler.set(List.class, PREFIX + name, groupKeys);
	}
	
	
	private class GroupParams {
		String key;
		CacheGroupProperties properties;
	}
	
	private void initialized() {
		if (!initialized) {			
			throw new RuntimeException("Redis cache handler not initialized; call doInitialization() before using it");
		}
	}	
}
