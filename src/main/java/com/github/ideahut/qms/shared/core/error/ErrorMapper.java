package com.github.ideahut.qms.shared.core.error;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.github.ideahut.qms.shared.client.exception.ResultException;
import com.github.ideahut.qms.shared.client.exception.ResultRuntimeException;
import com.github.ideahut.qms.shared.client.object.CodeMessage;
import com.github.ideahut.qms.shared.client.object.Result;
import com.github.ideahut.qms.shared.core.context.RequestContext;
import com.github.ideahut.qms.shared.core.exception.ResponseException;
import com.github.ideahut.qms.shared.core.exception.ResponseRuntimeException;
import com.github.ideahut.qms.shared.core.model.ModelIgnoreMember;

@Provider
public class ErrorMapper implements ExceptionMapper<Throwable> {
	
	private static final int ERROR_DEEP = 6;

	@Override
	public Response toResponse(Throwable exception) {
		RequestContext.currentContext().removeAttribute(ModelIgnoreMember.CONTEXT_ATTRIBUTE);
		
		Throwable ex = exception;
    	if (ex instanceof UndeclaredThrowableException) {
    		ex = ex.getCause();
    	}
    	Response response = null;
		if (ex instanceof ResponseException) {
    		response = ((ResponseException)ex).getResponse();
    	}
    	else if (ex instanceof ResponseRuntimeException) {
    		response = ((ResponseRuntimeException)ex).getResponse();
    	}
    	else if (ex instanceof ResultException) {
    		Result result = ((ResultException)ex).getResult();
    		response = Response.ok(result).build();
    	} 
    	else if (ex instanceof ResultRuntimeException) {
    		Result result = ((ResultRuntimeException)ex).getResult();
    		response = Response.ok(result).build();
    	}
    	else {
    		ErrorHandler errorHandler = RequestContext.currentContext().getAttribute(ErrorHandler.CONTEXT_ATTRIBUTE);
    		if (errorHandler != null) {
    			response = errorHandler.onError(ex);
    		} else {
    			response = Response.ok(Result.ERROR(getErrors(ex))).build();
    		}
    	}
		return response;
	}
	
	public static <T> T handle(Throwable exception) {
		Throwable ex = exception;
		if (ex instanceof UndeclaredThrowableException) {
    		ex = ex.getCause();
    	}
		if (ex instanceof ResponseException) {
    		throw new ResponseRuntimeException(((ResponseException)ex).getResponse());
    	}
    	else if (ex instanceof ResponseRuntimeException) {
    		throw new ResponseRuntimeException(((ResponseRuntimeException)ex).getResponse());
    	}
    	else if (ex instanceof ResultException) {
    		throw new ResultRuntimeException(((ResultException)ex).getResult());
    	} 
    	else if (ex instanceof ResultRuntimeException) {
    		throw new ResultRuntimeException(((ResultRuntimeException)ex).getResult());
    	}
    	else {
    		throw new ResponseRuntimeException(Response.ok(Result.ERROR(getErrors(ex))).build());
    	}		
	}
	
	public static List<CodeMessage> getErrors(Throwable ex) {
		List<CodeMessage> errors = new ArrayList<CodeMessage>();
		Throwable throwable = ex;
		for (int i = 0; i < ERROR_DEEP; i++) {
			if (throwable == null) {
				break;
			}
			errors.add(new CodeMessage("ERR-" + i, throwable + ""));
			throwable = throwable.getCause();
		}
		return errors;
	}
}
