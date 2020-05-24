package com.github.ideahut.qms.shared.core.queue;

public interface QueueSender<V> {
	
	public void sendMessage(QueueMessage<V> message);
	
	public void sendMessage(QueueMessage<V> message, boolean async);
	
}
