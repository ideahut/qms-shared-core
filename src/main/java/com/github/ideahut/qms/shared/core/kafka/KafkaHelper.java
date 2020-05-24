package com.github.ideahut.qms.shared.core.kafka;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;

import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;
import io.vertx.kafka.admin.TopicDescription;

public final class KafkaHelper {

	private static final Map<Class<?>, String> mapDefaultSerializer, mapDefaultDeserializer;
	static {
		Map<Class<?>, String> _mapDefaultSerializer = new HashMap<Class<?>, String>();
		_mapDefaultSerializer.put(Bytes.class, "org.apache.kafka.common.serialization.BytesSerializer");
		_mapDefaultSerializer.put(byte[].class, "org.apache.kafka.common.serialization.ByteArraySerializer");
		_mapDefaultSerializer.put(ByteBuffer.class, "org.apache.kafka.common.serialization.ByteBufferSerializer");
		_mapDefaultSerializer.put(Double.class, "org.apache.kafka.common.serialization.DoubleSerializer");
		_mapDefaultSerializer.put(Float.class, "org.apache.kafka.common.serialization.FloatSerializer");
		_mapDefaultSerializer.put(Integer.class, "org.apache.kafka.common.serialization.IntegerSerializer");
		_mapDefaultSerializer.put(Long.class, "org.apache.kafka.common.serialization.LongSerializer");
		_mapDefaultSerializer.put(Short.class, "org.apache.kafka.common.serialization.ShortSerializer");
		_mapDefaultSerializer.put(String.class, "org.apache.kafka.common.serialization.StringSerializer");
		mapDefaultSerializer = Collections.unmodifiableMap(_mapDefaultSerializer);
		
		Map<Class<?>, String> _mapDefaultDeserializer = new HashMap<Class<?>, String>();
		_mapDefaultDeserializer.put(Bytes.class, "org.apache.kafka.common.serialization.BytesDeserializer");
		_mapDefaultDeserializer.put(byte[].class, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
		_mapDefaultDeserializer.put(ByteBuffer.class, "org.apache.kafka.common.serialization.ByteBufferDeserializer");
		_mapDefaultDeserializer.put(Double.class, "org.apache.kafka.common.serialization.DoubleDeserializer");
		_mapDefaultDeserializer.put(Float.class, "org.apache.kafka.common.serialization.FloatDeserializer");
		_mapDefaultDeserializer.put(Integer.class, "org.apache.kafka.common.serialization.IntegerDeserializer");
		_mapDefaultDeserializer.put(Long.class, "org.apache.kafka.common.serialization.LongDeserializer");
		_mapDefaultDeserializer.put(Short.class, "org.apache.kafka.common.serialization.ShortDeserializer");
		_mapDefaultDeserializer.put(String.class, "org.apache.kafka.common.serialization.StringDeserializer");
		mapDefaultDeserializer = Collections.unmodifiableMap(_mapDefaultDeserializer);
	}
	
	private KafkaHelper() {}
	
	public static String getDefaultSerializer(Class<?> type) {
		return mapDefaultSerializer.get(type);
	}
	
	public static String getDefaultDeserializer(Class<?> type) {
		return mapDefaultDeserializer.get(type);
	}
	
	public static void addTopic(Logger logger, KafkaAdminClient admin, List<NewTopic> topics) {
		admin.createTopics(topics, ct -> {
			if (ct.succeeded()) {
				logger.debug("### Topic {} created.", topics.get(0).getName());
			} else {
				logger.debug("### Failed to create Topic {}, caused {}.", topics.get(0).getName(), ct.cause().getMessage());
			}
		});
	}
	
	public static void setupTopic(Logger logger, KafkaAdminClient admin, List<NewTopic> topics) {
		List<String> topicNames = Arrays.asList(topics.get(0).getName());
		admin.describeTopics(topicNames, ar -> {
			if (ar.succeeded()) {
				logger.debug("Topic \"{}\" already exist", topicNames.get(0));
				logger.debug("### Checking Partition ");
				TopicDescription topicDescription = ar.result().get(topicNames.get(0));
				int partitionSize = topicDescription.getPartitions().size();
				logger.debug("### Partition Size : {} ", partitionSize);
				if (topics.get(0).getNumPartitions() != partitionSize) {
					logger.debug("### Delete Old Topic {} ", topicNames.get(0));
					admin.deleteTopics(topicNames, dt -> {
						if (dt.succeeded()) {
							logger.debug("### Topic {} is successfuly deleted. ", topicNames.get(0));
							addTopic(logger, admin, topics);
						} else {
							logger.debug("### Failed to delete Topic {}, caused: {} ", topicNames.get(0),	dt.cause().getMessage());
							addTopic(logger, admin, topics);
						}
					});
				}
			} else {
				logger.debug("Topic \"{}\" not exist", topicNames.get(0));
				addTopic(logger, admin, topics);
			}
		});
	}
	
}
