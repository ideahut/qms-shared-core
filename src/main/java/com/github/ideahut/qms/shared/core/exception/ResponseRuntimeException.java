package com.github.ideahut.qms.shared.core.exception;

import javax.ws.rs.core.Response;

@SuppressWarnings("serial")
public class ResponseRuntimeException extends RuntimeException {

	private final Response response;
	
	public ResponseRuntimeException(Response response) {
		this.response = response;
	}

	public Response getResponse() {
		return response;
	}	
	
}
