package com.github.ideahut.qms.shared.core.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public interface TaskHandler {
	
	public <T> Map<String, T> concurrent(Map<String, Callable<T>> callables);	
	public <T> List<T> concurrent(List<Callable<T>> callables);
	
	public void execute(Runnable task);
	
}
