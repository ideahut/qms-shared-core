package com.github.ideahut.qms.shared.core.queue;

import java.io.Serializable;

@SuppressWarnings("serial")
public class QueueMessage<V> implements Serializable {

	private QueueHeader header;
	
	private V body;
	
	public QueueMessage() {}
	
	public QueueMessage(QueueHeader header, V body) {
		this.header = header;
		this.body = body;
	}

	public QueueHeader getHeader() {
		return header;
	}

	public void setHeader(QueueHeader header) {
		this.header = header;
	}

	public V getBody() {
		return body;
	}

	public void setBody(V body) {
		this.body = body;
	}	
	
}
