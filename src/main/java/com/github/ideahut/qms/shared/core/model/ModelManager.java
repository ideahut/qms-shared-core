package com.github.ideahut.qms.shared.core.model;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;

import com.github.ideahut.qms.shared.client.object.Grid;
import com.github.ideahut.qms.shared.core.hibernate.MetadataIntegrator;

public interface ModelManager {	
	public static final String CONTEXT_ATTRIBUTE = ModelManager.class.getName();	

	public Set<String> getModelGridNames();	
	public Grid getModelGrid(String name);
	
	public ModelInfo getModelInfo(Class<?> modelClass);
	public ModelInfo getAdminModelInfo(String adminName);
	public ModelInfo getTableModelInfo(String tableName);

	public EntityManager getEntityManager();
	public MetadataIntegrator getMetadataIntegrator();
	public ModelIgnoreMember getModelIgnoreMember();
	
	public <T> T transaction(boolean readOnly, Callable<T> callable);
	public <T> T transaction(Callable<T> callable);
	public <T> T transaction(boolean readOnly, SessionCallable<T> callable);
	public <T> T transaction(SessionCallable<T> callable);
	
	public void ignoreLazyMember(ModelInfo modelInfo, String...exceptFields);	
	public void ignoreLazyMember(Class<?> type, String...exceptFields);
	
	public <T> void loadLazy(T modelObject, ModelInfo modelInfo, List<String> fields);	
	public <T> void loadLazy(T modelObject, ModelInfo modelInfo, String...fields);
	
}
