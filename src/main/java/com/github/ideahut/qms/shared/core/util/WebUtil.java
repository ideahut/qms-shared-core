package com.github.ideahut.qms.shared.core.util;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jboss.resteasy.core.ResourceMethodInvoker;

import com.github.ideahut.qms.shared.core.annotation.Login;
import com.github.ideahut.qms.shared.core.annotation.Public;

public final class WebUtil {

	private WebUtil() {}
	
	/*
	 * IS PUBLIC
	 */
	public static boolean isPublic(ResourceMethodInvoker methodInvoker) {
		Method method = methodInvoker.getMethod();
		Public annotPublic = method.getAnnotation(Public.class);
		if (annotPublic == null) {
			annotPublic = method.getDeclaringClass().getAnnotation(Public.class);
			if (annotPublic  == null) {
				return false;
			}
		}
		return annotPublic.value();
	}
	
	
	/*
	 * IS LOGIN
	 */
	public static boolean isLogin(ResourceMethodInvoker methodInvoker) {
		Method method = methodInvoker.getMethod();
		Login annotLogin = method.getAnnotation(Login.class);
		if (annotLogin == null) {
			annotLogin = method.getDeclaringClass().getAnnotation(Login.class);
			if (annotLogin  == null) {
				return false;
			}
		}
		return annotLogin.value();
	}
	
	
	/*
	 * GET HEADER
	 */
	public static String getHeader(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getHeader(name);
		if (value == null) {
			value = request.getHeader(name.toLowerCase());
		}
		return value != null ? value : defaultValue;
	}
	
	public static String getHeader(HttpServletRequest request, String name) {
		return getHeader(request, name, null);
	}
	
	
	/*
	 * GET REMOTE ADDR 
	 */
	public static String getRemoteAddr(HttpServletRequest request) {
		String remoteAddr = getHeader(request, "X-Forwarded-For");
		if (remoteAddr == null || remoteAddr.isEmpty()) {
			remoteAddr = request.getRemoteAddr();
		}
		return remoteAddr.split(",")[0].trim();
	}
	
	
	/*
	 * GET TRIM PARAMETER
	 */
	public static String getTrimParameter(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		return value != null ? value.trim() : "";
	}
	
	
	/*
	 * REQUEST TO MAP
	 */
	public static Map<String, String> requestToMap(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> en = request.getParameterNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			map.put(name, request.getParameter(name));
		}
		return map;
	}
		
}
