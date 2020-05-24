package com.github.ideahut.qms.shared.core.queue;

public interface QueueReceiver<V> {
	
	public void onMessageReceive(QueueMessage<V> message);
	
}
