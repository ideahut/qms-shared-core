package com.github.ideahut.qms.shared.core.reporter;

public interface Reporter<T> {

	public T getValue();
	
	public void setValue(T value);
	
	public long getCreatedTimeMillis();
	
}
