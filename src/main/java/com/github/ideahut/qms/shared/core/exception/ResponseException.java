package com.github.ideahut.qms.shared.core.exception;

import javax.ws.rs.core.Response;

@SuppressWarnings("serial")
public class ResponseException extends Exception {

	private final Response response;
	
	public ResponseException(Response response) {
		this.response = response;
	}

	public Response getResponse() {
		return response;
	}	
	
}
