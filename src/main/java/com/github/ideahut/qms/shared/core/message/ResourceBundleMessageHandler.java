package com.github.ideahut.qms.shared.core.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.github.ideahut.qms.shared.client.object.CodeMessage;
import com.github.ideahut.qms.shared.core.bean.InitializationBean;
import com.github.ideahut.qms.shared.core.context.RequestContext;

public class ResourceBundleMessageHandler implements MessageHandler, InitializationBean {
	
	private final Map<String, ResourceBundle> resourceBundles = new HashMap<String, ResourceBundle>();
	
	private ClassLoader classLoader;
	
	private ResourceBundleMessageProperties properties;
	
	private List<String> availableLanguages;
	
	private String primaryLanguage;
	
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ResourceBundleMessageProperties getProperties() {
		return properties;
	}

	public void setProperties(ResourceBundleMessageProperties properties) {
		this.properties = properties;
	}

	public void setAvailableLanguages(List<String> availableLanguages) {
		this.availableLanguages = availableLanguages;
	}

	public void setPrimaryLanguage(String primaryLanguage) {
		this.primaryLanguage = primaryLanguage;
	}

	public ResourceBundleMessageHandler() {	}
	
	public ResourceBundleMessageHandler(ClassLoader classLoader) {	
		this.classLoader = classLoader;
	}
	
	@Override
	public void doInitialization() throws Exception {
		if (properties == null) {
			throw new Exception("properties is required");
		}
		
		String basename = properties.basename != null ? properties.basename.trim() : "";
		if (basename.isEmpty()) {
			throw new Exception("properties.basename is required");
		}
		
		ResourceBundleMessageProperties.Language rbmLanguage = properties.language;
		
		availableLanguages = new ArrayList<String>();
		if (rbmLanguage.available != null) {			
			for (String language : rbmLanguage.available) {
				language = language.trim();
				if (language.isEmpty()) {
					continue;
				}
				availableLanguages.add(language);
			}
		}
		if (availableLanguages.isEmpty()) {
			throw new Exception("properties.availableLanguages is required");
		}
		availableLanguages = Collections.unmodifiableList(availableLanguages);
		
		primaryLanguage = rbmLanguage.primary.orElse("").trim();
		if (primaryLanguage.isEmpty()) {
			primaryLanguage = availableLanguages.get(0);
		}
		if (!availableLanguages.contains(primaryLanguage)) {
			throw new Exception("primaryLanguage '" + primaryLanguage + "' is unavailable");
		} 
		
		resourceBundles.clear();
		for (String language :  availableLanguages) {
			Locale locale = new Locale(language);
			ResourceBundle bundle = ResourceBundle.getBundle(basename, locale, classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader());
			resourceBundles.put(language, bundle);
		}
		availableLanguages = Collections.unmodifiableList(availableLanguages);
	}	
	
	public ResourceBundle getResourceBundle() {
		String language = RequestContext.currentContext().getLanguage();
		return getResourceBundle(language);
	}
	
	public ResourceBundle getResourceBundle(String language) {
		ResourceBundle resourceBundle = resourceBundles.get(language);
		if (resourceBundle == null) {
			resourceBundle = resourceBundles.get(primaryLanguage);
		}
		return resourceBundle;		
	}
	
	public String getString(ResourceBundle resourceBundle, boolean throwException, String code) {
		try {
			return resourceBundle.getString(code);
		} catch (Exception e) {
			if (throwException) {
				throw new RuntimeException(e);
			}
			return code;
		}
	}
	
	private String replace(String value, String...args) {
		String result = new String(value);
		for (int i = 0; i < args.length; i++) {
			result = result.replace("{" + i + "}", args[i]);
		}
		return result;
	}
	
	
	@Override
	public List<String> getAvailableLanguages() {
		return availableLanguages;
	}

	@Override
	public String getPrimaryLanguage() {
		return primaryLanguage;
	}
	
	@Override
	public String getMessage(String code, boolean checkArgs, String... args) {
		ResourceBundle resourceBundle = getResourceBundle();
		String word = getString(resourceBundle, false, code);
		if (!checkArgs) {
			return replace(word, args);
		}
		String[] newArgs = new String[args.length];
		for (int i = 0; i < newArgs.length; i++) {
			newArgs[i] = getString(resourceBundle, false, args[i]);
		}
		return replace(word, newArgs);
	}

	@Override
	public String getMessage(String code, String... args) {
		return getMessage(code, false, args);
	}

	@Override
	public CodeMessage getCodeMessage(String code, boolean checkArgs, String... args) {
		String message = getMessage(code, checkArgs, args); 
		return new CodeMessage(code, message);
	}

	@Override
	public CodeMessage getCodeMessage(String code, String... args) {
		return getCodeMessage(code, false, args);
	}

	@Override
	public Map<String, String> getMessages(String... codes) {
		ResourceBundle resourceBundle = getResourceBundle();
		Map<String, String> dictionary = new LinkedHashMap<String, String>();
		for (String code : codes) {
			String value = getString(resourceBundle, false, code);
			dictionary.put(code, value);
		}
		return dictionary;
	}
	
}
