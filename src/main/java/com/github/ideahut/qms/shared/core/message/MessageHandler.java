package com.github.ideahut.qms.shared.core.message;

import java.util.List;
import java.util.Map;

import com.github.ideahut.qms.shared.client.object.CodeMessage;

public interface MessageHandler {
	
	public static final String CONTEXT_ATTRIBUTE = MessageHandler.class.getName();
	
	
	public List<String> getAvailableLanguages();
	
	public String getPrimaryLanguage();
	

	public String getMessage(String code, boolean checkArgs, String...args);
	
	public String getMessage(String code, String...args);
	
	public CodeMessage getCodeMessage(String code, boolean checkArgs, String...args);
	
	public CodeMessage getCodeMessage(String code, String...args);
	
	public Map<String, String> getMessages(String...codes);
	
}
