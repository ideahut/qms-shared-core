package com.github.ideahut.qms.shared.core.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ideahut.qms.shared.core.queue.QueueHeader;
import com.github.ideahut.qms.shared.core.queue.QueueMessage;
import com.github.ideahut.qms.shared.core.queue.QueueReceiver;

import io.vertx.core.Vertx;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaHeader;

class KafkaSubscriber<V> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSubscriber.class);
	
	private final List<KafkaConsumer<String, V>> consumers = new ArrayList<KafkaConsumer<String, V>>();
	
	private final Class<V> type;
	
	private final Vertx vertx;
	
	private final Map<String, String> config;
	
	private final KafkaSubscriberProperties properties;
	
	private final KafkaAdminClient admin;
	
	public KafkaSubscriber(Class<V> type, KafkaAdminClient admin, Vertx vertx, Map<String, String> config, KafkaSubscriberProperties properties) {
		this.type = type;
		this.admin = admin;
		this.vertx = vertx;
		this.config = config;
		this.properties = properties;
	}
	
	@SuppressWarnings("unchecked")
	public void subscribe() {
		String deserializer = KafkaHelper.getDefaultDeserializer(type);
		if (deserializer == null) {
			throw new RuntimeException("Unsupported deserializer type: " + type + ", topic: " + properties.getTopicProperties().getName());
		}
		
		unsubscribe();
		consumers.clear();
	
		// create topic leader
		KafkaHelper.addTopic(LOGGER, admin, properties.getTopicProperties().createNewTopics());
		
		Integer numConsumers = properties.getConsumers();
		if (numConsumers == null || numConsumers < 1) {
			numConsumers = 1;
		}		
		QueueReceiver<V> receiver = properties.getReceiver();
		Map<String, String> kconfig = new HashMap<String, String>(config);
		if (properties.getConfig() != null) {
			for (String key : properties.getConfig().keySet()) {
				String value = properties.getConfig().getOrDefault(key, null);
				if (value != null) {
					kconfig.put(key, value);
				}
			}
		}
		kconfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KafkaHelper.getDefaultDeserializer(String.class));
		kconfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
		kconfig.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getGroupId());
		for (int i = 0; i < numConsumers; i++) {
			consumers.add(new MessageConsumer(i, kconfig, receiver).getConsumer());
		}		
	}
	
	public void unsubscribe() {
		for (KafkaConsumer<String, V> consumer : consumers) {
			consumer.unsubscribe();
		}
	}	
	
	
	private class MessageConsumer {		
		
		private final KafkaConsumer<String, V> consumer;
		
		public MessageConsumer(Integer index, Map<String, String> config, QueueReceiver<V> receiver) {
			consumer = KafkaConsumer.create(vertx, config);
			consumer.handler(ls -> {
				if (receiver != null) {
					LOGGER.debug("{}-{}-Receiver-{}: {}", properties.getTopicProperties().getName(), properties.getGroupId(), index, ls.value() + "");
					QueueMessage<V> message = new QueueMessage<V>();
					message.setBody(ls.value());
					List<KafkaHeader> headers = ls.headers();
					QueueHeader qheader = new QueueHeader();
					qheader.setTopicName(properties.getTopicProperties().getName());
					qheader.setGroupId(properties.getGroupId());
					qheader.setIndex(index);
					for (KafkaHeader header : headers) {
						qheader.put(header.key(), header.value().toString("utf-8"));
					}
					message.setHeader(qheader);
					receiver.onMessageReceive(message);
				}
			});			
			consumer.subscribe(properties.getTopicProperties().getName());
			consumer.commit();
			LOGGER.debug("Commit {}-{}-Consumer-{}", properties.getTopicProperties().getName(), properties.getGroupId(), index);
		}
		public KafkaConsumer<String, V> getConsumer() {
			return consumer;
		}		
	}
	
}
