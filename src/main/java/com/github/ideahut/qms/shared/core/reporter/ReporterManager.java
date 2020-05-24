package com.github.ideahut.qms.shared.core.reporter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReporterManager<T> {

	private final Map<String, Reporter<T>> reporters = Collections.synchronizedMap(new HashMap<String, Reporter<T>>());
	
	public Reporter<T> create(String id) {
		Reporter<T> reporter = new Reporter<T>() {
			private T value;
			private long createdTimeMillis = System.currentTimeMillis();
			@Override
			public void setValue(T value) {
				this.value = value;
			}			
			@Override
			public T getValue() {
				return value;
			}
			@Override
			public long getCreatedTimeMillis() {
				return createdTimeMillis;
			}
		};
		reporters.put(id, reporter);
		return reporter;
	}
	
	public void remove(String id) {
		reporters.remove(id);
	}
	
	public boolean isEmpty() {
		return reporters.isEmpty();
	}
	
	public Set<String> ids() {
		return reporters.keySet();
	}
	
	public void report(String id, T value) {
		Reporter<T> reporter = reporters.get(id);
		if (reporter == null) {
			return;
		}
		reporter.setValue(value);
		reporters.remove(id);
	}
	
}
