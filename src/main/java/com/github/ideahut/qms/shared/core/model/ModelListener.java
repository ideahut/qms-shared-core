package com.github.ideahut.qms.shared.core.model;

import java.util.List;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import com.github.ideahut.qms.shared.core.context.RequestContext;
import com.github.ideahut.qms.shared.core.model.entity.BaseModel;
import com.github.ideahut.qms.shared.core.model.interceptor.ModelInterceptor;
import com.github.ideahut.qms.shared.core.model.interceptor.ModelPostInterceptor;
import com.github.ideahut.qms.shared.core.model.interceptor.ModelPreInterceptor;

public class ModelListener {
	
	@PrePersist
	public void onPrePersist(BaseModel model) {
		List<ModelInterceptor> interceptors = getInterceptors();
		if (interceptors == null) {
			return;
		}
		for (ModelInterceptor interceptor : interceptors) {
			if (interceptor instanceof ModelPreInterceptor) {
				((ModelPreInterceptor)interceptor).onPrePersist(model);
			}
		}
	}

	@PreUpdate
	public void onPreUpdate(BaseModel model) {
		List<ModelInterceptor> interceptors = getInterceptors();
		if (interceptors == null) {
			return;
		}
		for (ModelInterceptor interceptor : interceptors) {
			if (interceptor instanceof ModelPreInterceptor) {
				((ModelPreInterceptor)interceptor).onPreUpdate(model);
			}
		}
	}

	@PreRemove
	public void onPreRemove(BaseModel model) {
		List<ModelInterceptor> interceptors = getInterceptors();
		if (interceptors == null) {
			return;
		}
		for (ModelInterceptor interceptor : interceptors) {
			if (interceptor instanceof ModelPreInterceptor) {
				((ModelPreInterceptor)interceptor).onPreRemove(model);
			}
		}
	}
	
	@PostPersist
	public void onPostPersist(BaseModel model) {
		List<ModelInterceptor> interceptors = getInterceptors();
		if (interceptors == null) {
			return;
		}
		for (ModelInterceptor interceptor : interceptors) {
			if (interceptor instanceof ModelPostInterceptor) {
				((ModelPostInterceptor)interceptor).onPostPersist(model);
			}
		}
	}

	@PostUpdate
	public void onPostUpdate(BaseModel model) {
		List<ModelInterceptor> interceptors = getInterceptors();
		if (interceptors == null) {
			return;
		}
		for (ModelInterceptor interceptor : interceptors) {
			if (interceptor instanceof ModelPostInterceptor) {
				((ModelPostInterceptor)interceptor).onPostUpdate(model);
			}
		}
	}

	@PostRemove
	public void onPostRemove(BaseModel model) {
		List<ModelInterceptor> interceptors = getInterceptors();
		if (interceptors == null) {
			return;
		}
		for (ModelInterceptor interceptor : interceptors) {
			if (interceptor instanceof ModelPostInterceptor) {
				((ModelPostInterceptor)interceptor).onPostRemove(model);
			}
		}
	}
	
	private List<ModelInterceptor> getInterceptors() {
		return RequestContext.currentContext().getAttribute(ModelInterceptor.CONTEXT_ATTRIBUTE);
	}
	
}
