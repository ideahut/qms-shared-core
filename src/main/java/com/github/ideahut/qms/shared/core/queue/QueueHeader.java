package com.github.ideahut.qms.shared.core.queue;

import java.util.HashMap;

@SuppressWarnings("serial")
public class QueueHeader extends HashMap<String, String> {
	
	private String topicName;
	
	private String groupId;
	
	private Integer index;

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}	
		
}
