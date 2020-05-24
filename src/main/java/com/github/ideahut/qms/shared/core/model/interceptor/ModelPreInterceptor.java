package com.github.ideahut.qms.shared.core.model.interceptor;

import com.github.ideahut.qms.shared.core.model.entity.BaseModel;

public interface ModelPreInterceptor extends ModelInterceptor {

	public void onPrePersist(BaseModel model);

	public void onPreUpdate(BaseModel model);

	public void onPreRemove(BaseModel model);
	
}
