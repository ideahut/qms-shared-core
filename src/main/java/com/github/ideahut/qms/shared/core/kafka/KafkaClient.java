package com.github.ideahut.qms.shared.core.kafka;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import com.github.ideahut.qms.shared.core.queue.QueueSender;
import com.github.ideahut.qms.shared.core.task.TaskHandler;

import io.vertx.core.Vertx;
import io.vertx.kafka.admin.KafkaAdminClient;

@SuppressWarnings("rawtypes")
public class KafkaClient {

	private final Map<String, SubscriberTopic> subscribers = new HashMap<String, SubscriberTopic>();	
	private final Vertx vertx;	
	private final KafkaAdminClient admin;	
	private final Map<String, String> cfgProducer;	
	private final Map<String, String> cfgConsumer;	
	private final TaskHandler taskHandler;
	
	
	public KafkaClient(Vertx vertx, Map<String, String> config, TaskHandler taskHandler) throws Exception {
		this.vertx = vertx != null ? vertx : Vertx.vertx();
		Map<String, String> kconfig = config != null ? new HashMap<String, String>(config) : new HashMap<String, String>();
		
		// producer
		kconfig.remove(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
		kconfig.remove(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);		
		// consumer
		kconfig.remove(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
		kconfig.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
		kconfig.remove(ConsumerConfig.GROUP_ID_CONFIG);
		
		this.cfgProducer = getConfig(ProducerConfig.class, kconfig);
		this.cfgConsumer = getConfig(ConsumerConfig.class, kconfig);
		
		this.admin = KafkaAdminClient.create(vertx, kconfig);
		this.taskHandler = taskHandler;
		
	}
	
	public KafkaClient(Map<String, String> config, TaskHandler taskHandler) throws Exception {
		this(null, config, taskHandler);
	}
	
	public KafkaClient(Map<String, String> config) throws Exception {
		this(null, config, null);
	}

	
	public <V> void subscribe(Class<V> type, KafkaSubscriberProperties...propertieses) {
		if (propertieses.length == 0) {
			throw new RuntimeException("Cannot subscribe an empty properties");
		}
		for (KafkaSubscriberProperties properties : propertieses) {
			KafkaSubscriber<V> kafkaSubscriber = new KafkaSubscriber<V>(type, admin, vertx, cfgConsumer, properties);
			kafkaSubscriber.subscribe();
			SubscriberTopic subscriberTopic = subscribers.get(properties.getTopicProperties().getName());
			if (subscriberTopic == null) {
				subscriberTopic = new SubscriberTopic();
				subscriberTopic.topic = properties.getTopicProperties().getName();
				subscriberTopic.groups = new HashMap<String, SubscriberGroup>();
			}
			SubscriberGroup subscriberGroup = subscriberTopic.groups.get(properties.getGroupId());
			if (subscriberGroup == null) {
				subscriberGroup = new SubscriberGroup();
				subscriberGroup.groupId = properties.getGroupId();
				subscriberGroup.kafkaSubscribers = new ArrayList<KafkaSubscriber>();
				subscriberTopic.groups.put(subscriberGroup.groupId, subscriberGroup);
			}
			subscriberGroup.kafkaSubscribers.add(kafkaSubscriber);
			subscribers.put(subscriberTopic.topic, subscriberTopic);
		}
	}
	
	public void unsubscribe(String topic, String groupId) {
		if (topic != null) {
			SubscriberTopic subscriberTopic = subscribers.get(topic);
			if (subscriberTopic == null)  {
				return;
			}
			if (groupId != null) {
				SubscriberGroup subscriberGroup = subscriberTopic.groups.get(groupId);
				for (KafkaSubscriber kafkaSubscriber : subscriberGroup.kafkaSubscribers) {
					kafkaSubscriber.unsubscribe();
				}
				subscriberTopic.groups.remove(groupId);
			} else {
				for (String gId : subscriberTopic.groups.keySet()) {
					SubscriberGroup subscriberGroup = subscriberTopic.groups.get(gId);
					for (KafkaSubscriber kafkaSubscriber : subscriberGroup.kafkaSubscribers) {
						kafkaSubscriber.unsubscribe();
					}
				}
				subscriberTopic.groups.clear();
			}
		} else {
			for (String gtopic : subscribers.keySet()) {
				SubscriberTopic subscriberTopic = subscribers.get(gtopic);
				for (SubscriberGroup subscriberGroup : subscriberTopic.groups.values()) {
					for (KafkaSubscriber kafkaSubscriber : subscriberGroup.kafkaSubscribers) {
						kafkaSubscriber.unsubscribe();
					}
				}
			}
			subscribers.clear();
		}
	}
	
	public void unsubscribe(String topic) {
		unsubscribe(topic, null);
	}
	
	public void unsubscribe() {
		unsubscribe(null);
	}
	
	@SuppressWarnings("unchecked")
	public <V> QueueSender<V> createSender(KafkaSenderProperties properties) {
		return new KafkaSender<V>(properties.getType(), admin, vertx, cfgProducer, taskHandler, properties);
	}
	
	private Map<String, String> getConfig(Class<?> configClass, Map<String, String> map) throws Exception {
		Map<String, String> configMap = new HashMap<String, String>();
		for(Field field : configClass.getFields()) {
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && field.getName().endsWith("_CONFIG")) {
				String key = (String)field.get(null);
				if (map.containsKey(key)) {
					configMap.put(key, map.get(key));
				}
			}
		}
		return configMap;
	}
	
	
	private class SubscriberTopic {
		private String topic;
		private Map<String, SubscriberGroup> groups = new HashMap<String, SubscriberGroup>();
	}
	private class SubscriberGroup {
		private String groupId;
		private List<KafkaSubscriber> kafkaSubscribers = new ArrayList<KafkaSubscriber>();
		
	}

}