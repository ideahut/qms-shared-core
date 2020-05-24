package com.github.ideahut.qms.shared.core.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.kafka.admin.NewTopic;

public class KafkaTopicProperties {
	
	private String name;
	
	private Integer partitions;
	
	private Integer replicationFactor;
	
	private Long deleteRetentionMillis;

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPartitions() {
		return partitions;
	}

	public void setPartitions(Integer partitions) {
		this.partitions = partitions;
	}

	public Integer getReplicationFactor() {
		return replicationFactor;
	}

	public void setReplicationFactor(Integer replicationFactor) {
		this.replicationFactor = replicationFactor;
	}

	public Long getDeleteRetentionMillis() {
		return deleteRetentionMillis;
	}

	public void setDeleteRetentionMillis(Long deleteRetentionMillis) {
		this.deleteRetentionMillis = deleteRetentionMillis;
	}
	
	public List<NewTopic> createNewTopics() {
		NewTopic topic = new NewTopic();
	    topic.setName(name);
	    topic.setNumPartitions(partitions != null && partitions > 0 ? partitions : 1);
	    topic.setReplicationFactor(replicationFactor != null && replicationFactor > 0 ? replicationFactor.shortValue() : 1);
	    if (deleteRetentionMillis != null && deleteRetentionMillis > 0) {
	    	Map<String, String> config = new HashMap<>();
	    	config.put("delete.retention.ms", deleteRetentionMillis.toString());
		    topic.setConfig(config);
	    }
	    List<NewTopic> topics = new ArrayList<NewTopic>();
	    topics.add(topic);
	    return topics;
	}
	
}
