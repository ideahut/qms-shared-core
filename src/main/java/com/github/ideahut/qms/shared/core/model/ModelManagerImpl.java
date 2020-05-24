package com.github.ideahut.qms.shared.core.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.github.ideahut.qms.shared.client.object.Grid;
import com.github.ideahut.qms.shared.client.object.Page;
import com.github.ideahut.qms.shared.client.type.IdType;
import com.github.ideahut.qms.shared.core.annotation.Admin;
import com.github.ideahut.qms.shared.core.bean.InitializationBean;
import com.github.ideahut.qms.shared.core.context.RequestContext;
import com.github.ideahut.qms.shared.core.grid.GridHelper;
import com.github.ideahut.qms.shared.core.hibernate.MetadataIntegrator;

public class ModelManagerImpl implements ModelManager, InitializationBean {

	private boolean initialized = false;	
	private EntityManager entityManager;
	private MetadataIntegrator metadataIntegrator;
	
	private Map<String, Grid> mapModelGrid = new HashMap<String, Grid>();	
	private Map<Class<?>, ModelInfo> mapModelInfo = new HashMap<Class<?>, ModelInfo>();	
	private Map<String, ModelInfo> mapModelTable = new HashMap<String, ModelInfo>();	
	private Map<String, ModelInfo> mapModelAdmin = new HashMap<String, ModelInfo>();
	
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public ModelManagerImpl() {}
	
	public ModelManagerImpl(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void doInitialization() throws Exception {
		mapModelGrid.clear();
		mapModelInfo.clear();
		mapModelAdmin.clear();		
		metadataIntegrator = new MetadataIntegrator(entityManager.getEntityManagerFactory().getProperties());
		Set<EntityType<?>> entityTypes = entityManager.getMetamodel().getEntities();
		for (EntityType<?> entityType : entityTypes) {
			Class<?> classType = entityType.getJavaType();			
			metadataIntegrator.addAnnotatedClass(classType);			
			ModelInfo modelInfo = new ModelInfo(this, classType);
			
			// ModelInfo by class
			mapModelInfo.put(classType, modelInfo);
			if (IdType.embedded.equals(modelInfo.getIdInfo().getType())) {
				ModelInfo embeddedInfo = modelInfo.getIdInfo().getEmbeddedIdInfo();
				mapModelInfo.put(embeddedInfo.getModelClass(), embeddedInfo);
			}
			
			// ModelInfo by database table
			mapModelTable.put(modelInfo.getTableSchema() + "_" + modelInfo.getTableName(), modelInfo);
			
			// ModelInfo by @Admin name
			Admin admin = classType.getAnnotation(Admin.class);
			if (admin != null) {
				String name = admin.name().trim();
				if (name.isEmpty()) {
					throw new RuntimeException("@Admin name is required, for: " + classType.getName());
				}
				if (mapModelAdmin.containsKey(name)) {
					throw new RuntimeException("Duplicate @Admin name, for: " + classType.getName() );
				}
				mapModelAdmin.put(name, modelInfo);
			}
		}
		metadataIntegrator.prepare();
		initialized = true;
		for (ModelInfo modelInfo : mapModelInfo.values()) {
			modelInfo.prepare();
			
			// Grid
			GridHelper.populateGrid(mapModelGrid, mapModelInfo, modelInfo);
		}
	}

	@Override
	public Set<String> getModelGridNames() {
		initialized();
		return mapModelGrid.keySet();
	}

	@Override
	public Grid getModelGrid(String name) {
		initialized();
		return mapModelGrid.get(name);
	}

	@Override
	public ModelInfo getModelInfo(Class<?> modelClass) {
		initialized();
		return mapModelInfo.get(modelClass);
	}

	@Override
	public ModelInfo getAdminModelInfo(String adminName) {
		initialized();
		return mapModelAdmin.get(adminName);
	}

	@Override
	public ModelInfo getTableModelInfo(String tableName) {
		initialized();
		return mapModelTable.get(tableName);
	}

	@Override
	public EntityManager getEntityManager() {
		return entityManager;
	}

	@Override
	public MetadataIntegrator getMetadataIntegrator() {
		initialized();
		return metadataIntegrator;
	}

	@Override
	public ModelIgnoreMember getModelIgnoreMember() {
		ModelIgnoreMember modelIgnoreMember = RequestContext.currentContext().getAttribute(ModelIgnoreMember.CONTEXT_ATTRIBUTE);
		if (modelIgnoreMember == null) {
			modelIgnoreMember = new ModelIgnoreMember();
			RequestContext.currentContext().setAttribute(ModelIgnoreMember.CONTEXT_ATTRIBUTE, modelIgnoreMember);
		}
		return modelIgnoreMember;
	}

	@Override
	public <T> T transaction(boolean readOnly, Callable<T> callable) {
		initialized();
		Transaction transaction = null;
		Session session = null;
    	try {
    		session = metadataIntegrator.getSessionFactory().openSession();
    		transaction = session.beginTransaction();
    		T result = callable.call();
    		if (!readOnly) {
    			transaction.commit();
    		}
    		return result;
		} catch (Exception e) {
			if (!readOnly && transaction != null) {
				transaction.rollback();
			}
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
	
	@Override
	public <T> T transaction(Callable<T> callable) {
		return transaction(false, callable);
	}
	
	@Override
	public <T> T transaction(boolean readOnly, SessionCallable<T> callable) {
		initialized();
		Transaction transaction = null;
		Session session = null;
    	try {
    		session = metadataIntegrator.getSessionFactory().openSession();
    		transaction = session.beginTransaction();
    		T result = callable.call(session);
    		if (!readOnly) {
    			transaction.commit();
    		}
    		return result;
		} catch (Exception e) {
			if (!readOnly && transaction != null) {
				transaction.rollback();
			}
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@Override
	public <T> T transaction(SessionCallable<T> callable) {
		return transaction(false, callable);
	}
	

	@Override
	public void ignoreLazyMember(ModelInfo modelInfo, String... exceptFields) {
		if (modelInfo == null) return;
		List<String> exceptList = Arrays.asList(exceptFields);
		ModelIgnoreMember modelIgnoreMember = getModelIgnoreMember();
		for (FieldInfo fieldInfo : modelInfo.getLazyObjectFields().values()) {
			if (exceptList.contains(fieldInfo.getName())) {
				continue;
			}
			modelIgnoreMember.add(fieldInfo);
		}
		for (FieldInfo fieldInfo : modelInfo.getLazyCollectionFields().values()) {
			if (exceptList.contains(fieldInfo.getName())) {
				continue;
			}
			modelIgnoreMember.add(fieldInfo);
		}
	}

	@Override
	public void ignoreLazyMember(Class<?> type, String... exceptFields) {
		initialized();
		ModelInfo modelInfo = mapModelInfo.get(type);
		ignoreLazyMember(modelInfo, exceptFields);
	}

	@Override
	public <T> void loadLazy(T modelObject, ModelInfo modelInfo, List<String> fields) {
		String[] args = fields != null ? fields.toArray(new String[0]) : new String[0];
		loadLazy(modelObject, modelInfo, args);		
	}

	@Override
	public <T> void loadLazy(T modelObject, ModelInfo modelInfo, String... fields) {
		if (modelObject == null) {
			return;
		}
		ignoreLazyMember(modelInfo, fields);
		try {
			_loadLazy(modelObject, modelInfo, fields);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initialized() {
		if (!initialized) {			
			throw new RuntimeException("Model manager not initialized; call doInitialization() before using it");
		}
	}
	
	@SuppressWarnings("rawtypes")
	private <T> void _loadLazy(T modelObject, ModelInfo modelInfo, String...fields) throws Exception {
		if (modelObject instanceof Collection) {
			Iterator iter = ((Collection)modelObject).iterator();
			while (iter.hasNext()) {
				_loadLazy(iter.next(), modelInfo, fields);
			}
			return;
		} else if (modelObject instanceof Page) {
			_loadLazy(((Page)modelObject).getData(), modelInfo, fields);
			return;
		} else if (modelObject instanceof Map) {
			_loadLazy(((Map)modelObject).values(), modelInfo, fields);
			return;
		}
		ModelIgnoreMember ignoreMember = getModelIgnoreMember();
		for (String fieldName : modelInfo.getFieldInfoNames()) {
			FieldInfo fieldInfo = modelInfo.getFieldInfo(fieldName);
			if (fieldInfo.isLazyObject()) {
				if (!ignoreMember.isIgnored(fieldInfo)) {
					Object lazyFieldObject = fieldInfo.getValue(modelObject);
					if (lazyFieldObject != null) {
						if (!Hibernate.isInitialized(lazyFieldObject)) {
							Hibernate.initialize(lazyFieldObject);
						}
						ModelInfo lazyModelInfo = getModelInfo(fieldInfo.getType());
						if (lazyModelInfo != null && !ignoreMember.hasIgnoredType(lazyModelInfo.getModelClass())) {
							ignoreLazyMember(lazyModelInfo);
						}
					}
				} else {
					// Tampilkan ID jika tidak null
					ModelInfo lazyModelInfo = getModelInfo(fieldInfo.getType());
					if (lazyModelInfo != null) {
						Object lazyFieldObject = fieldInfo.getValue(modelObject);
						if (lazyFieldObject != null) {
							IdInfo lazyIdInfo = lazyModelInfo.getIdInfo();
							Object newLazyFieldObject = lazyModelInfo.getModelClass().newInstance();
							for (String lazyIdField : lazyIdInfo.getFields()) {
								FieldInfo lazyIdFieldInfo = lazyModelInfo.getFieldInfo(lazyIdField);
								lazyIdFieldInfo.setValue(newLazyFieldObject, lazyIdFieldInfo.getValue(lazyFieldObject));								
							}
							fieldInfo.setValue(modelObject, newLazyFieldObject);
							ignoreMember.remove(fieldInfo.getModelInfo().getModelClass(), fieldInfo.getName());
						}						
					}
				}
			}
			//else if (fieldInfo.isLazyCollection()) {
				// TODO: Perlu dicari solusi untuk collection, karena RequestContext tidak terbaca di DataMapper pada saat load lazy (di luar orm session)
			//}			
		}
	}	
	
}
