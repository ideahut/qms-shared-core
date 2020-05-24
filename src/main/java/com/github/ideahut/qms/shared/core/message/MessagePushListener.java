package com.github.ideahut.qms.shared.core.message;

import java.util.List;

public interface MessagePushListener {

	public void onMessagePush(String language, String code, String text);
	
	public void onMessageCodes(String language, List<String> codes);
	
}
