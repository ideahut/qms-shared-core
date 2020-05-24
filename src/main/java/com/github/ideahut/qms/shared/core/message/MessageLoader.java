package com.github.ideahut.qms.shared.core.message;

import java.util.List;

public interface MessageLoader {

	public List<String> availableLanguages();
	
	public String primaryLanguage();
	
	public void doLoadMessages(String language);
	
	public void setMessagePushListener(MessagePushListener messagePushListener);
	
}
