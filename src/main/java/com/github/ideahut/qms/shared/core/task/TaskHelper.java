package com.github.ideahut.qms.shared.core.task;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public final class TaskHelper {
	
	private TaskHelper() {}
	
	public static TaskExecutor createTaskExecutor(TaskProperties properties) {
		if (properties == null) {
			throw new RuntimeException("properties is required");
		}
		TaskProperties.Executor executorProps = properties.executor;		
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setAllowCoreThreadTimeOut(executorProps.allowCoreThreadTimeOut.orElse(false));
		taskExecutor.setAwaitTerminationSeconds(executorProps.awaitTerminationSeconds.orElse(0));
		taskExecutor.setCorePoolSize(executorProps.corePoolSize.orElse(1));
		taskExecutor.setDaemon(executorProps.daemon.orElse(false));
		taskExecutor.setKeepAliveSeconds(executorProps.keepAliveSeconds.orElse(60));
		taskExecutor.setMaxPoolSize(executorProps.maxPoolSize.orElse(Integer.MAX_VALUE));
		taskExecutor.setQueueCapacity(executorProps.queueCapacity.orElse(Integer.MAX_VALUE));
		taskExecutor.setThreadNamePrefix(executorProps.threadNamePrefix.orElse("TaskExecutor-" + System.currentTimeMillis() + "-"));
		taskExecutor.setThreadPriority(executorProps.threadPriority.orElse(Thread.NORM_PRIORITY));
		taskExecutor.setWaitForTasksToCompleteOnShutdown(executorProps.waitForJobsToCompleteOnShutdown.orElse(false));
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

}
