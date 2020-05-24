package com.github.ideahut.qms.shared.core.bean;

import java.util.List;

import com.github.ideahut.qms.shared.client.object.KeyValue;

public interface OptionsBean {
	
	public List<KeyValue<String, String>> getKeyValueOptions();
	
}
