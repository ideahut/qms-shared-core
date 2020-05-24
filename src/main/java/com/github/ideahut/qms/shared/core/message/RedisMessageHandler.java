package com.github.ideahut.qms.shared.core.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.github.ideahut.qms.shared.client.object.CodeMessage;
import com.github.ideahut.qms.shared.core.bean.InitializationBean;
import com.github.ideahut.qms.shared.core.context.RequestContext;
import com.github.ideahut.qms.shared.core.mapper.DataMapper;
import com.github.ideahut.qms.shared.core.mapper.DataMapperImpl;

public class RedisMessageHandler implements MessageHandler, MessagePushListener, InitializationBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RedisMessageHandler.class);
	
	private static final String CODES 	= "LANGUAGE_CODES_";
	private static final String LOADER 	= "LANGUAGE_LOADER";
	
	private boolean initialized = false;
	
	private String loaderId;	
	private DataMapper dataMapper;	
	private RedisTemplate<String, String> redisTemplate;	
	private List<String> availableLanguages;	
	private String primaryLanguage;	
	private MessageLoader messageLoader;	
	private Boolean loadOnStartup = Boolean.FALSE;		
	
	public void setLoaderId(String loaderId) {
		this.loaderId = loaderId;
	}

	public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void setMessageLoader(MessageLoader messageLoader) {
		this.messageLoader = messageLoader;
	}
	
	public void setLoadOnStartup(Boolean loadOnStartup) {
		this.loadOnStartup = loadOnStartup;
	}

	@Override
	public void doInitialization() throws Exception {
		if (redisTemplate == null) {
			throw new Exception("redisTemplate is required");
		}
		if (messageLoader == null) {
			throw new Exception("messageLoader is required");
		}
		availableLanguages = messageLoader.availableLanguages();
		if (availableLanguages == null || availableLanguages.isEmpty()) {
			throw new Exception("availableLanguages is required");
		}
		availableLanguages = Collections.unmodifiableList(availableLanguages);
		primaryLanguage = messageLoader.primaryLanguage();
		if (primaryLanguage == null || primaryLanguage.isEmpty()) {
			primaryLanguage = availableLanguages.get(0);
		}
		if (dataMapper == null) {
			dataMapper = new DataMapperImpl();
		}
		if (loaderId == null) {
			loaderId = RedisMessageHandler.class.getSimpleName() + "@" + ConfigProvider.getConfig().getValue("quarkus.http.port", Integer.class);
		}
		redisTemplate.afterPropertiesSet();
		messageLoader.setMessagePushListener(this);
		initialized = true;
		
		if (loadOnStartup != null && Boolean.TRUE.equals(loadOnStartup)) {
			loadMessage();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadMessage() {
		initialized();
		ValueOperations<String, String> operations = redisTemplate.opsForValue();
		String loader = operations.get(LOADER);
		if (loader == null) {
			operations.set(LOADER, loaderId);
			try {
				for (String language : availableLanguages) {
					LOGGER.info("Loading message for language: {}", language);
					String data = operations.get(CODES + language);
					if (data != null) {
						List<String> codes = dataMapper.readData(data, List.class);
						redisTemplate.delete(codes);				
					}
					messageLoader.doLoadMessages(language);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				redisTemplate.delete(LOADER);
			}
		} else {
			LOGGER.info("Language messages is loading from instance: " + loader);
		}
	}

	@Override
	public List<String> getAvailableLanguages() {
		return availableLanguages;
	}

	@Override
	public String getPrimaryLanguage() {
		return primaryLanguage;
	}
	
	public String getString(String code, boolean throwException) {
		initialized();
		String language = language();
		ValueOperations<String, String> operations = redisTemplate.opsForValue();
		String value = operations.get(language + "." + code);
		if (value == null) {
			if (throwException) {
				throw new RuntimeException("Message is not found for code: " + code);
			} else {
				return code;
			}
		}
		return value;
	}
	
	public String getString(String code) {
		return getString(code, false);
	}
	
	@Override
	public String getMessage(String code, boolean checkArgs, String... args) {
		String word = getString(code, false);
		if (!checkArgs) {
			return replace(word, args);
		}
		String[] newArgs = new String[args.length];
		for (int i = 0; i < newArgs.length; i++) {
			newArgs[i] = getString(args[i], false);
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
		Map<String, String> dictionary = new LinkedHashMap<String, String>();
		for (String code : codes) {
			String value = getString(code, false);
			dictionary.put(code, value);
		}
		return dictionary;
	}
	
	
	private void initialized() {
		if (!initialized) {			
			throw new RuntimeException("Redis message handler not initialized; call doInitialization() before using it");
		}
	}
	
	private String replace(String value, String...args) {
		String result = new String(value);
		for (int i = 0; i < args.length; i++) {
			result = result.replace("{" + i + "}", args[i]);
		}
		return result;
	}
	
	private String language() {
		initialized();
		String language = RequestContext.currentContext().getLanguage();
		if (language == null || language.isEmpty()) {
			language = primaryLanguage;
		}
		return language;
	}

	@Override
	public void onMessagePush(String language, String code, String text) {
		if (availableLanguages.indexOf(language) == -1) {
			return;
		}
		ValueOperations<String, String> operations = redisTemplate.opsForValue();
		operations.set(language + "." + code, text);
	}

	@Override
	public void onMessageCodes(String language, List<String> codes) {
		if (availableLanguages.indexOf(language) == -1 || codes == null) {
			return;
		}
		List<String> keys = new ArrayList<String>();
		for (String code : codes) {
			keys.add(language + "." + code);
		}
		ValueOperations<String, String> operations = redisTemplate.opsForValue();
		String value = dataMapper.writeJsonAsString(codes);
		operations.set(CODES + language, value);
	}		

}
