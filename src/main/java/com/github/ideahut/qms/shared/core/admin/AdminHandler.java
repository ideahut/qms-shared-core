package com.github.ideahut.qms.shared.core.admin;

import com.github.ideahut.qms.shared.client.object.AdminRequest;
import com.github.ideahut.qms.shared.core.model.ModelInfo;

public interface AdminHandler {
	
	public ModelInfo getModelInfo(String name);
	
	public <T> T execute(AdminAction action, ModelInfo modelInfo, AdminRequest adminRequest) throws Exception;

}
