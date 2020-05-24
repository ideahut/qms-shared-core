package com.github.ideahut.qms.shared.core.model;

import org.hibernate.Session;

public interface SessionCallable<V> {
	
	public V call(Session session) throws Exception;
	
}
