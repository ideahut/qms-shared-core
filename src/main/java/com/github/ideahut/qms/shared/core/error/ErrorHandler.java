package com.github.ideahut.qms.shared.core.error;

import javax.ws.rs.core.Response;

public interface ErrorHandler {
	
	public static final String CONTEXT_ATTRIBUTE = ErrorHandler.class.getName();
	
	public Response onError(Throwable ex);
	
}
