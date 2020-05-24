package com.github.ideahut.qms.shared.core.model;

import java.util.Set;

import com.github.ideahut.qms.shared.client.type.IdType;

public class IdInfo {
	
	private IdType type;
	
	private Set<String> fields;
	
	private ModelInfo embeddedIdInfo; // Info class embedded id

	public IdType getType() {
		return type;
	}

	public void setType(IdType type) {
		this.type = type;
	}

	public Set<String> getFields() {
		return fields;
	}

	public void setFields(Set<String> fields) {
		this.fields = fields;
	}

	public ModelInfo getEmbeddedIdInfo() {
		return embeddedIdInfo;
	}

	public void setEmbeddedIdInfo(ModelInfo embeddedIdInfo) {
		this.embeddedIdInfo = embeddedIdInfo;
	}
	
}
