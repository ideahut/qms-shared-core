package com.github.ideahut.qms.shared.core.cache;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public final class RedisHelper {

	private RedisHelper() {}
	
	public static RedisConnectionFactory createRedisConnectionFactory(RedisProperties properties) {
		if (properties == null) {
			throw new RuntimeException("properties is required");
		}
		String host = properties.host != null ? properties.host.trim() : "";
		if (host.isEmpty()) {
			throw new RuntimeException("properties.host is required");
		}
		Integer port = properties.port;
		if (port == null || port <= 0) {
			throw new RuntimeException("properties.port is required or invalid value");
		}
		String password = properties.password.orElse("");
		Integer database = properties.database.orElse(0);
		
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
		configuration.setHostName(host);
		configuration.setPort(port.intValue());
		if (database > 0) {
			configuration.setDatabase(database.intValue());
		}
		if (!password.isEmpty()) {
			configuration.setPassword(password);
		}
		return new JedisConnectionFactory(configuration);
	}
	
	
	public static <K, V> RedisTemplate<K, V> createRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<K, V> template = new RedisTemplate<K, V>();
		template.setConnectionFactory(connectionFactory);
		return template;
	}
	
	
	public static <K, V> RedisTemplate<K, V> createRedisTemplate(RedisProperties properties) {
		RedisConnectionFactory connectionFactory = createRedisConnectionFactory(properties);
		return createRedisTemplate(connectionFactory);
	}
	
}
