package com.github.ideahut.qms.shared.core.kafka;

import java.util.Map;

import com.github.ideahut.qms.shared.core.queue.QueueReceiver;

@SuppressWarnings("rawtypes")
public class KafkaSubscriberProperties {

	private KafkaTopicProperties topicProperties;
	
	private String groupId;
	
	private Integer consumers = 1; // default 1 consumer
	
	private QueueReceiver receiver;
	
	private Map<String, String> config;

	
	public KafkaTopicProperties getTopicProperties() {
		return topicProperties;
	}

	public void setTopicProperties(KafkaTopicProperties topicProperties) {
		this.topicProperties = topicProperties;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Integer getConsumers() {
		return consumers;
	}

	public void setConsumers(Integer consumers) {
		this.consumers = consumers;
	}

	public QueueReceiver getReceiver() {
		return receiver;
	}
	
	public void setReceiver(QueueReceiver receiver) {
		this.receiver = receiver;
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}
	
}
