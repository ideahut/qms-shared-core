package com.github.ideahut.qms.shared.core.task;

import java.util.Optional;

public class TaskProperties {

	public Executor executor = new Executor();
	
	public static class Executor {		
		public Optional<Boolean> allowCoreThreadTimeOut = Optional.empty();	
		public Optional<Integer> awaitTerminationSeconds = Optional.empty();
		public Optional<Integer> corePoolSize = Optional.empty();
		public Optional<Boolean> daemon = Optional.empty();
		public Optional<Integer> keepAliveSeconds = Optional.empty();	
		public Optional<Integer> maxPoolSize = Optional.empty();
		public Optional<Integer> queueCapacity = Optional.empty();
		public Optional<String> threadNamePrefix = Optional.empty();
		public Optional<Integer> threadPriority = Optional.empty();
		public Optional<Boolean> waitForJobsToCompleteOnShutdown = Optional.empty();
	}
}
