package com.github.ideahut.qms.shared.core.kafka;

import java.util.Map;

@SuppressWarnings("rawtypes")
public class KafkaSenderProperties {

	private KafkaTopicProperties topicProperties;
	
	private Class type;
	
	private Map<String, String> config;

	public KafkaTopicProperties getTopicProperties() {
		return topicProperties;
	}

	public void setTopicProperties(KafkaTopicProperties topicProperties) {
		this.topicProperties = topicProperties;
	}

	public Class getType() {
		return type;
	}

	public void setType(Class type) {
		this.type = type;
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public void setConfig(Map<String, String> config) {
		this.config = config;
	}
	
}
