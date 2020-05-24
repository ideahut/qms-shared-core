package com.github.ideahut.qms.shared.core.model.interceptor;

import com.github.ideahut.qms.shared.core.model.entity.BaseModel;

public interface ModelPostInterceptor extends ModelInterceptor {

	public void onPostPersist(BaseModel model);

	public void onPostUpdate(BaseModel model);

	public void onPostRemove(BaseModel model);
	
}
