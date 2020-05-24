package com.github.ideahut.qms.shared.core.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ideahut.qms.shared.core.queue.QueueHeader;
import com.github.ideahut.qms.shared.core.queue.QueueMessage;
import com.github.ideahut.qms.shared.core.queue.QueueSender;
import com.github.ideahut.qms.shared.core.task.TaskHandler;

import io.vertx.core.Vertx;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;

class KafkaSender<V> implements QueueSender<V> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSender.class);
	
	private final KafkaAdminClient admin;	
	private final TaskHandler taskHandler;	
	private final KafkaProducer<String, V> producer;	
	private final List<NewTopic> topics;
	
	
	public KafkaSender(Class<V> type, KafkaAdminClient admin, Vertx vertx, Map<String, String> config, TaskHandler taskHandler, KafkaSenderProperties properties) {
		String serializer = KafkaHelper.getDefaultSerializer(type);
		if (serializer == null) {
			throw new RuntimeException("Unsupported serializer type: " + type + ", topic: " + properties.getTopicProperties().getName());
		}
		Map<String, String> kconfig = new HashMap<String, String>(config);
		if (properties.getConfig() != null) {
			for (String key : properties.getConfig().keySet()) {
				String value = properties.getConfig().getOrDefault(key, null);
				if (value != null) {
					kconfig.put(key, value);
				}
			}
		}
		kconfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KafkaHelper.getDefaultSerializer(String.class));
		kconfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer);		
		
	    this.topics = properties.getTopicProperties().createNewTopics();
	    this.admin = admin;
	    this.taskHandler = taskHandler;
		this.producer = KafkaProducer.create(vertx, kconfig);
	}
	
	@Override
	public void sendMessage(QueueMessage<V> message) {
		KafkaHelper.setupTopic(LOGGER, admin, topics);
		KafkaProducerRecord<String, V> record = KafkaProducerRecord.create(topics.get(0).getName(), message.getBody());
		QueueHeader header = message.getHeader();
		if (header != null) {
			for (String name : header.keySet()) {
				String value = header.get(name);
				if (value != null) {
					record.addHeader(name, header.get(name));
				}
			}
		}
		this.producer.send(record, done -> {
			if (done.succeeded()) {
				RecordMetadata recordMetadata = done.result();
				LOGGER.debug("### Message " + record.value() + " written on topic=" + recordMetadata.getTopic()
						+ ", partition=" + recordMetadata.getPartition() + ", offset=" + recordMetadata.getOffset());
			}
		});
	}

	@Override
	public void sendMessage(QueueMessage<V> message, boolean async) {
		if (async && taskHandler != null) {
			taskHandler.execute(new Runnable() {
				@Override
				public void run() {
					sendMessage(message);
				}
			});
		} else {
			sendMessage(message);
		}
	}

}
