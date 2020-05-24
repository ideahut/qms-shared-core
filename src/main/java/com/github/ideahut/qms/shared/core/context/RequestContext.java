package com.github.ideahut.qms.shared.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

public class RequestContext {
	
	private static final ThreadLocal<RequestContext> holder = new InheritableThreadLocal<RequestContext>();
	
	public static RequestContext currentContext() {
		RequestContext context = holder.get();
		if (context == null) {
			context = new RequestContext();
			holder.set(context);
		}
		return context;
	}
	
	public static void destroy() {
		holder.remove();
	}
	
	private final Long id;
	
	private final Map<String, Object> attributes = new HashMap<String, Object>();
	
	
	private HttpServletRequest request;
	
	private HttpServletResponse response;	
	
	private String accept;
	
	private String language;
	
	private RequestContext() {
		this.id = System.nanoTime();
	}
	
	public Long getId() {
		return id;
	}
	
	public Set<String> getAttributeNames() {
		return attributes.keySet();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name, T defaultValue) {
		T t = (T)attributes.get(name);
		return t != null ? t : defaultValue;
	}
	
	public <T> T getAttribute(String name) {
		return getAttribute(name, null);
	}

	public <T> RequestContext setAttribute(String name, T value) {
		attributes.put(name, value);
		return this;
	}
	
	public RequestContext removeAttribute(String name) {
		attributes.remove(name);
		return this;
	}
	
	public HttpServletRequest getRequest() {
		return request;
	}

	public RequestContext setRequest(HttpServletRequest request) {
		this.request = request;
		this.accept = request.getHeader(HttpHeaders.ACCEPT);
		this.language = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
		return this;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public RequestContext setResponse(HttpServletResponse response) {
		this.response = response;
		return this;
	}
	
	public String getAccept() {
		return accept;
	}

	public void setAccept(String accept) {
		this.accept = accept;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
}
