package com.github.ideahut.qms.shared.core.grid;

import java.util.HashMap;
import java.util.Map;

import com.github.ideahut.qms.shared.client.object.Grid;
import com.github.ideahut.qms.shared.client.object.Result;
import com.github.ideahut.qms.shared.core.bean.InitializationBean;
import com.github.ideahut.qms.shared.core.context.RequestContext;
import com.github.ideahut.qms.shared.core.message.MessageHandler;
import com.github.ideahut.qms.shared.core.model.ModelManager;
import com.github.ideahut.qms.shared.core.support.ResultAssert;

public class ModelGridHandler implements GridHandler, InitializationBean {
	
	private final Map<String, Map<String, Grid>> mapGridLanguage = new HashMap<String, Map<String,Grid>>();
	
	private ModelManager modelManager;
	
	private MessageHandler messageHandler;
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	@Override
	public void doInitialization() throws Exception {
		mapGridLanguage.clear();
		for (String name : modelManager.getModelGridNames()) {
			Grid grid = modelManager.getModelGrid(name);
			Map<String, Grid> mapLangGrid = new HashMap<String, Grid>();
			for (String language : messageHandler.getAvailableLanguages()) {
				Grid langGrid = GridHelper.cloneGrid(grid, messageHandler, language);
				mapLangGrid.put(language, langGrid);
			}
			mapGridLanguage.put(name, mapLangGrid);
		}
	} 
	
	@Override
	public Grid getGrid(String name) {
		Map<String, Grid> mapLangGrid = mapGridLanguage.get(name);
		ResultAssert.notNull(Result.ERROR(messageHandler.getMessage("error.02", "Grid")), mapLangGrid);
		String language = RequestContext.currentContext().getLanguage();
		language = language != null ? language.trim() : "";
		if (language.isEmpty()) {
			language = messageHandler.getPrimaryLanguage();
		}
		return mapLangGrid.get(language);
	}

}
