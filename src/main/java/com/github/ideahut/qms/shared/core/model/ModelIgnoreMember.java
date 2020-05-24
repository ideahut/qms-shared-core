package com.github.ideahut.qms.shared.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModelIgnoreMember {
	
	public static final String CONTEXT_ATTRIBUTE = ModelIgnoreMember.class.getName();
	
	private final Map<Class<?>, Set<String>> ignoreMembers = new HashMap<Class<?>, Set<String>>();
	
	public boolean hasIgnoredType(Class<?> type) {
		return ignoreMembers.containsKey(type);
	}
	
	public ModelIgnoreMember add(Class<?> type, String name) {
		Set<String> names = ignoreMembers.get(type);
		if (names == null) {
			names = new HashSet<String>();
			ignoreMembers.put(type, names);
		}
		names.add(name);
		return this;
	}
	
	public boolean isIgnored(Class<?> type, String name) {
		Set<String> names = ignoreMembers.get(type);
		if (names == null) {
			return false;
		}
		return names.contains(name);
	}

	public ModelIgnoreMember remove(Class<?> type, String name) {
		Set<String> names = ignoreMembers.get(type);
		if (names != null) {
			names.remove(name);
		}
		return this;
	}
	public ModelIgnoreMember add(FieldInfo fieldInfo) {
		Class<?> type = fieldInfo.getModelInfo().getModelClass();
		String name = fieldInfo.getName();
		return add(type, name);
	}
	
	public boolean isIgnored(FieldInfo fieldInfo) {
		Class<?> type = fieldInfo.getModelInfo().getModelClass();
		String name = fieldInfo.getName();
		return isIgnored(type, name);
	}	
	
}
