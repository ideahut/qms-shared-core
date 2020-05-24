package com.github.ideahut.qms.shared.core.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import com.github.ideahut.qms.shared.core.bean.InitializationBean;

public class TaskHandlerImpl implements TaskHandler, InitializationBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskHandler.class);
	
	private boolean initialized = false;	
	private TaskExecutor taskExecutor;	
	private TaskProperties taskProperties;
	
	public void setTaskProperties(TaskProperties taskProperties) {
		this.taskProperties = taskProperties;
	}

	@Override
	public void doInitialization() throws Exception {
		if (taskProperties == null) {
			taskProperties = new TaskProperties();
		}
		taskExecutor = TaskHelper.createTaskExecutor(taskProperties);
		initialized = true;
	}
	
	@Override
	public <T> Map<String, T> concurrent(Map<String, Callable<T>> callables) {
		initialized();
		ExecutorService executor = Executors.newFixedThreadPool(callables.size());
		Map<String, FutureTask<T>> tasks = new HashMap<String, FutureTask<T>>();
		for (String name : callables.keySet()) {
			FutureTask<T> task = new FutureTask<T>(callables.get(name));
			executor.execute(task);
			tasks.put(name, task);
		}		
		Map<String, T> result = new HashMap<String, T>();
		for (String name : tasks.keySet()) {
			T value;
			try {
				value = tasks.get(name).get();				
			} catch (Exception e) {
				LOGGER.error("TaskService-concurrent", e);
				value = null;
			}
			result.put(name, value);
		}
		executor.shutdown();
		tasks.clear();
		return result;
	}
	
	@Override
	public <T> List<T> concurrent(List<Callable<T>> callables) {
		initialized();
		ExecutorService executor = Executors.newFixedThreadPool(callables.size());
		List<FutureTask<T>> tasks = new ArrayList<FutureTask<T>>();
		for (Callable<T> callable : callables) {
			FutureTask<T> task = new FutureTask<T>(callable);
			executor.execute(task);
			tasks.add(task);
		}		
		List<T> result = new ArrayList<T>();
		for (FutureTask<T> task : tasks) {
			T value;
			try {
				value = task.get();				
			} catch (Exception e) {
				LOGGER.error("TaskService-concurrent", e);
				value = null;
			}
			result.add(value);
		}
		executor.shutdown();
		tasks.clear();
		return result;
	}

	@Override
	public void execute(Runnable task) {
		initialized();
		taskExecutor.execute(task);		
	}
	
	private void initialized() {
		if (!initialized) {			
			throw new RuntimeException("Task service not initialized; call doInitialization() before using it");
		}
	}
	
}
