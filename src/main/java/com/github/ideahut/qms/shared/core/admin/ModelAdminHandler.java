package com.github.ideahut.qms.shared.core.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.github.ideahut.qms.shared.client.object.AdminRequest;
import com.github.ideahut.qms.shared.client.object.Page;
import com.github.ideahut.qms.shared.client.type.ConditionType;
import com.github.ideahut.qms.shared.client.type.IdType;
import com.github.ideahut.qms.shared.client.type.LogicalType;
import com.github.ideahut.qms.shared.core.admin.AdminHelper.Builder;
import com.github.ideahut.qms.shared.core.admin.AdminHelper.Where;
import com.github.ideahut.qms.shared.core.mapper.DataMapper;
import com.github.ideahut.qms.shared.core.model.FieldInfo;
import com.github.ideahut.qms.shared.core.model.IdInfo;
import com.github.ideahut.qms.shared.core.model.ModelInfo;
import com.github.ideahut.qms.shared.core.model.ModelManager;
import com.github.ideahut.qms.shared.core.model.entity.SoftDeleteModel;

/*
 * Admin Handler menggunakan EntityManager
 * Harus di dalam method dengan annotation @Transactional
 */
public class ModelAdminHandler implements AdminHandler {
	
	private ModelManager modelManager;
	
	private DataMapper dataMapper;

	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}
	
	public void setDataMapper(DataMapper dataMapper) {
		this.dataMapper = dataMapper;
	}


	@Override
	public ModelInfo getModelInfo(String name) {
		return modelManager.getAdminModelInfo(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T execute(AdminAction action, ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		T result;
		switch (action) {
		case unique:
			result = single(modelInfo, adminRequest, true);
			modelManager.loadLazy(result, modelInfo, adminRequest.getLoadlazy());
			break;
		case single:
			result = single(modelInfo, adminRequest, false);
			modelManager.loadLazy(result, modelInfo, adminRequest.getLoadlazy());
			break;		
		case list:
			result = list(modelInfo, adminRequest);
			modelManager.loadLazy(result, modelInfo, adminRequest.getLoadlazy());
			break;		
		case save:
			result = save(modelInfo, adminRequest);
			modelManager.loadLazy(result, modelInfo, adminRequest.getLoadlazy());
			break;
		case saves:
			result = (T)saves(modelInfo, adminRequest);
			break;
		case delete:
			result = delete(modelInfo, adminRequest);
			modelManager.loadLazy(result, modelInfo, adminRequest.getLoadlazy());
			break;
		case deletes:
			result = (T)deletes(modelInfo, adminRequest);
		case map:
			result = map(modelInfo, adminRequest);
			modelManager.loadLazy(result, modelInfo, adminRequest.getLoadlazy());
			break;
		case insert:
			result = (T)(new Integer(insert(modelInfo, adminRequest)));
			break;
		case update:
			result = (T)(new Integer(update(modelInfo, adminRequest)));
			break;
		case remove:
			result = (T)(new Integer(remove(modelInfo, adminRequest)));
			break;
		default:
			throw new UnsupportedOperationException("Unsupported action: " + action);
		}
		return result;
	}
	
	/*
	 * LIST
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> T list(ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		EntityManager entityManager = modelManager.getEntityManager();
		Builder builder = AdminHelper.builder(modelManager, modelInfo, adminRequest, true);
		String hql = "";
		Where where = builder.where;
		hql += "from " + modelInfo.getModelClass().getSimpleName() + " " + where.alias;
		if (where.joins != null) {
			for (String[] ajoin : where.joins.values()) {
				hql += " join " + ajoin[1] + " " + ajoin[0];
			}
		}
		if (!where.query.isEmpty()) {
			hql += " " + where.query;
		}
		Integer start = adminRequest.getStart();
		Integer limit = adminRequest.getLimit();
		limit = limit != null && limit > 0 ? limit : Page.DEFAULT_PAGE_SIZE;
		Page<?> page = adminRequest.getPage();
		if (page != null) {
			if (Boolean.TRUE.equals(page.getCount())) {
				Query query = entityManager.createQuery("select count(" + where.alias + ") "+ hql);
				if (where.parameters != null) {
					for (int i = 0; i < where.parameters.size(); i++) {
						query.setParameter(i + 1, where.parameters.get(i));
					}				
				}
				Long records = (Long)query.getSingleResult();
				page.setRecords(records);
				if (records == 0) {
					return (T)page;
				}
			}
			start = (page.getIndex() - 1) * page.getSize();
			limit = page.getSize();
		}
		if (!builder.fieldQL.isEmpty()) {
			hql = "select " + builder.fieldQL + " " + hql; 
		}
		if (!builder.orderQL.isEmpty()) {
			hql += " order by " + builder.orderQL;
		}
		Query query = entityManager.createQuery(hql);
		if (where.parameters != null) {
			for (int i = 0; i < where.parameters.size(); i++) {
				query.setParameter(i + 1, where.parameters.get(i));
			}				
		}
		if (start != null && start >= 0) {
			query.setFirstResult(start);
		}
		query.setMaxResults(limit);
		List data = query.getResultList();
		if (page != null) {
			page.setData(data);
			return (T)page; // return page
		}		
		return (T)data; // return List
	}
	
	
	/*
	 * SINGLE
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> T single(ModelInfo modelInfo, AdminRequest adminRequest, boolean unique) throws Exception {
		EntityManager entityManager = modelManager.getEntityManager();
		T modelObject = null;
		Object id = adminRequest.getId();
		if (id != null) {
			IdInfo idInfo = modelInfo.getIdInfo();
			if (IdType.composite.equals(idInfo.getType())) {
				AdminRequest compositeAdminRequest = new AdminRequest();
				compositeAdminRequest.setModel(adminRequest.getModel());
				List<AdminRequest.Filter> filter = new ArrayList<AdminRequest.Filter>();
				Map map = (Map)id;
				for (Object key : map.keySet()) {
					String field = (String)key;
					String value = (String)map.get(key);
					filter.add(AdminRequest.Filter.NEW(LogicalType.and, field, ConditionType.EQUAL, value));
				}
				compositeAdminRequest.setFilter(filter);
				compositeAdminRequest.setLimit(2);
				List data = list(modelInfo, compositeAdminRequest);
				if (data != null && !data.isEmpty()) {
					if (unique && data.size() > 1) {
						throw new Exception("Multiple data result");
					}
					modelObject = (T)data.get(0);
				}
			} else {
				modelObject = (T)entityManager.find(modelInfo.getModelClass(), id);
				if (modelObject != null && SoftDeleteModel.class.isAssignableFrom(modelObject.getClass())) {
					if (SoftDeleteModel.FLAG_YES.equals(((SoftDeleteModel)modelObject).getIsDeleteFlag())) {
						modelObject = null;
					}
				}
			}
		} else {
			adminRequest.setPage(null);
			adminRequest.setStart(null);
			adminRequest.setLimit(2);
			List data = list(modelInfo, adminRequest);
			if (data != null && !data.isEmpty()) {
				if (unique && data.size() > 1) {
					throw new Exception("Multiple data result");
				}
				modelObject = (T)data.get(0);
			}
		}
				
		return modelObject;
	}
	
	/*
	 * SAVE
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> T save(ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		EntityManager entityManager = modelManager.getEntityManager();
		Object id = adminRequest.getId();
		T modelObject = null;
		if (id != null) { // update
			modelObject = single(modelInfo, adminRequest, true);
			if (modelObject != null) {
				modelObject = AdminHelper.invokeObject(dataMapper, modelInfo, modelObject, adminRequest.getValue(), true);
			}			
		} else { // create
			IdInfo idInfo = modelInfo.getIdInfo();
			Map<String, Object> value = adminRequest.getValue();
			if (SoftDeleteModel.class.isAssignableFrom(modelInfo.getModelClass())) {
				Object idvalue = null;
				switch (idInfo.getType()) {
				case embedded:
					idvalue = value.get(idInfo.getFields().iterator().next());
					idvalue = dataMapper.convertData(idvalue, idInfo.getEmbeddedIdInfo().getModelClass());
					break;
				case composite:
					Map mapId = new HashMap();
					for (String sid : idInfo.getFields()) {
						mapId.put(sid, value.get(sid));
					}
					idvalue = !mapId.isEmpty() ? mapId : null;
					break;
				case standard:
					FieldInfo idFieldInfo = modelInfo.getFieldInfo(idInfo.getFields().iterator().next());
					idvalue = idFieldInfo.convert(value.get(idFieldInfo.getName()) + "");
					break;					
				default:
					break;
				}
				if (idvalue != null) {
					AdminRequest searchAdminRequest = new AdminRequest();
					searchAdminRequest.setModel(adminRequest.getModel());
					searchAdminRequest.setId(idvalue);
					AdminHelper.IGNORE_SOFT_DELETE.set(Boolean.TRUE);
					modelObject = single(modelInfo, searchAdminRequest, true);
				}
			}
			if (modelObject != null) {
				// jika sudah pernah dibuat, maka flag delete di set False
				for (String idField : idInfo.getFields()) {
					value.remove(idField);
				}
				modelObject = AdminHelper.invokeObject(dataMapper, modelInfo, modelObject, value, true);
				((SoftDeleteModel)modelObject).setIsDeleteFlag(SoftDeleteModel.FLAG_NO);
				SoftDeleteModel.ACTION.set(SoftDeleteModel.RECREATE);
			} else {
				modelObject = AdminHelper.mapToObject(dataMapper, value, modelInfo.getModelClass());
				entityManager.persist(modelObject);
			}
		}
		return modelObject;
	}
	
	
	/*
	 * SAVES (BULK)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> List<T> saves(ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		List<T> result = null;
		EntityManager entityManager = modelManager.getEntityManager();
		List ids = adminRequest.getIds();
		if (ids != null) { // update
			Map<String, Object> value = adminRequest.getValue();
			if (value == null || value.isEmpty()) {
				throw new Exception("value is required");
			}
			List<Object> parameters = new ArrayList<Object>();
			String hql = "from " + modelInfo.getModelClass().getSimpleName();
			if (SoftDeleteModel.class.isAssignableFrom(modelInfo.getModelClass())) {
				hql += " where " + SoftDeleteModel.FIELD_NAME + "=?1";
				parameters.add(Boolean.FALSE);
			} else {
				hql += " where 1=1";
			}
			IdInfo idInfo = modelInfo.getIdInfo();
			if (IdType.composite.equals(idInfo.getType())) {
				hql += " and (";
				String wql = "", cql;
				int index = parameters.size() + 1;
				for (Object oid : ids) {
					wql = " or (";
					cql = "";
					Map<String, Object> mid = (Map<String, Object>)oid;
					for (String idField : mid.keySet()) {
						cql += " and " + idField + "=?" + index;
						FieldInfo fieldInfo = modelInfo.getFieldInfo(idField);
						Object fieldValue = fieldInfo.convert((String)mid.get(idField));
						parameters.add(fieldValue);
						index++;
					}
					cql = cql.substring(5); // buang bagian awal ' and '
					wql += ")";
				}
				wql = wql.substring(4); // buang bagian awal ' or '
				hql += wql + ")";
			} else {
				hql += " and " + idInfo.getFields().iterator().next() + " in(?2)";
				parameters.add(ids);
			}
			Query query = entityManager.createQuery(hql);
			for (int i = 0; i < parameters.size(); i++) {
				query.setParameter(i + 1, parameters.get(i));
			}
			result = query.getResultList();
			if (result == null) {
				return null;
			}			
		} else { // create
			result = new ArrayList<T>();
			List<Map<String, Object>> values = adminRequest.getValues();
			if (values == null || values.isEmpty()) {
				throw new Exception("values is required");
			}
			boolean isSoftDelete = SoftDeleteModel.class.isAssignableFrom(modelInfo.getModelClass());
			if (isSoftDelete) {
				IdInfo idInfo = modelInfo.getIdInfo();
				for(Map<String, Object> value : values) {
					List<Object> parameters = new ArrayList<Object>();
					String hql = "from " + modelInfo.getModelClass().getSimpleName() + " where 1=1";
					int index = 1;
					Object param;
					for (String idField : idInfo.getFields()) {
						hql = " and " + idField + "=?" + index;
						if (IdType.embedded.equals(idInfo.getType())) {
							param = dataMapper.convertData(value.get(idField), idInfo.getEmbeddedIdInfo().getModelClass());
						} else {
							param = modelInfo.getFieldInfo(idField).convert(value.get(idField) + "");
						}
						value.get(idField);
						parameters.add(param);
					}
					Query query = entityManager.createQuery(hql);
					for (int i = 0; i < parameters.size(); i++) {
						query.setParameter(i + 1, parameters.get(i));
					}
					T modelObject = (T) query.getSingleResult();
					if (modelObject != null) {
						modelObject = AdminHelper.invokeObject(dataMapper, modelInfo, modelObject, value, true);
						((SoftDeleteModel)modelObject).setIsDeleteFlag(SoftDeleteModel.FLAG_NO);
					} else {
						modelObject = AdminHelper.mapToObject(dataMapper, value, modelInfo.getModelClass());
						entityManager.persist(modelObject);
					}
					result.add(modelObject);
				}
			} else {
				for(Map<String, Object> value : values) {
					T modelObject = AdminHelper.mapToObject(dataMapper, value, modelInfo.getModelClass());
					entityManager.persist(modelObject);
					result.add(modelObject);
				}
			}			
			
		}
		return result;
	}
	
	/*
	 * DELETE
	 */
	private <T> T delete(ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		Object id = adminRequest.getId();
		if (id == null) {
			throw new Exception("id is required");
		}
		EntityManager entityManager = modelManager.getEntityManager();
		T model = single(modelInfo, adminRequest, true);
		if (model != null) {
			boolean isSoftDelete = SoftDeleteModel.class.isAssignableFrom(modelInfo.getModelClass());
			if (isSoftDelete) {
				((SoftDeleteModel)model).setIsDeleteFlag(SoftDeleteModel.FLAG_YES);
				SoftDeleteModel.ACTION.set(SoftDeleteModel.DELETE);
			} else {
				entityManager.remove(model);
			}
		}
		return model;
	}
	
	
	/*
	 * DELETES (BULK)
	 */
	@SuppressWarnings("rawtypes")
	private <T> List<T> deletes(ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		List ids = adminRequest.getIds();
		if (ids == null) {
			throw new Exception("ids is required");
		}
		List<T> result = new ArrayList<T>();
		for (Object id : ids) {
			AdminRequest delAdminRequest = new AdminRequest();
			delAdminRequest.setModel(adminRequest.getModel());
			delAdminRequest.setId(id);
			T modelObject = delete(modelInfo, delAdminRequest);
			if (modelObject != null) {
				result.add(modelObject);
			}
		}
		return result;
	}
	
	/*
	 * MAP
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> T map(ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		String mapkey = adminRequest.getMapkey();
		if (mapkey == null) {
			throw new Exception("mapkey is required");
		}
		List<String> listkey = new ArrayList<String>();
		String[] split = mapkey.split(",");
		for (String key : split) {
			key = key.trim();
			if (key.isEmpty()) {
				continue;
			}
			listkey.add(key);
		}
		if (listkey.isEmpty()) {
			throw new Exception("mapkey is empty");
		}
		adminRequest.setPage(null);
		List data = list(modelInfo, adminRequest);
		Map<Object, Object> result = new LinkedHashMap();
		if (data != null) {
			IdInfo idInfo = modelInfo.getIdInfo();
			for (Object object : data) {
				Object key;
				if (listkey.size() == 1) {
					if (IdType.embedded.equals(idInfo.getType()) && listkey.get(0).equals(idInfo.getFields().iterator().next())) {
						String skey = "";
						Object okey = modelInfo.getFieldInfo(listkey.get(0)).getValue(object);
						ModelInfo embeddedIdInfo = idInfo.getEmbeddedIdInfo();
						for (String fieldName : embeddedIdInfo.getFieldInfoNames()) {
							skey += AdminHelper.ITEM_SPLITTER + fieldName + AdminHelper.KEYVAL_SPLITTER + embeddedIdInfo.getFieldInfo(fieldName).getValue(okey);
						}
						skey = skey.substring(AdminHelper.ITEM_SPLITTER.length());
						key = skey;
					} else {
						key = modelInfo.getFieldInfo(listkey.get(0)).getValue(object);
					}
				} else {
					String skey = "";
					for (String fieldName : listkey) {
						skey += AdminHelper.ITEM_SPLITTER + fieldName + AdminHelper.KEYVAL_SPLITTER + modelInfo.getFieldInfo(fieldName).getValue(object);
					}
					skey = skey.substring(AdminHelper.ITEM_SPLITTER.length());
					key = skey;
				}
				if (result.containsKey(key)) {
					throw new Exception("Duplicate key");
				}
				result.put(key, object);
			}
		}
		return (T)result;		
	}
	
	
	/*
	 * INSERT
	 */
	@SuppressWarnings("unchecked")
	private int insert(ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		Map<String, Object> value = adminRequest.getValue();
		if (value == null || value.size() == 0) {
			throw new Exception("value is required");
		}
		IdInfo idInfo = modelInfo.getIdInfo();
		Object modelObject = AdminHelper.mapToObject(dataMapper, value, modelInfo.getModelClass());
		Map<String, Object> embeddedIdMap = null;
		Object embeddedIdObj = null;
		if (IdType.embedded.equals(idInfo.getType())) {
			String idField = idInfo.getFields().iterator().next();
			embeddedIdMap = (Map<String, Object>)value.remove(idField);
			embeddedIdObj = modelInfo.getFieldInfo(idField).getValue(modelObject);
		}		
		List<Object> parameters = new ArrayList<Object>();
		String hquery = "insert into " + modelInfo.getTableName();
		String fields = "(";
		String values = " values (";
		for (String name : value.keySet()) {
			FieldInfo fieldInfo = modelInfo.getFieldInfo(name);
			if (fieldInfo == null) {
				continue;
			}			
			fields += fieldInfo.getColumn() + ",";
			values += "?,";
			Object parameter = fieldInfo.getValue(modelObject);
			parameters.add(parameter);
		}
		if (embeddedIdMap != null) {
			ModelInfo embeddedIdInfo = idInfo.getEmbeddedIdInfo();
			for (String name : embeddedIdMap.keySet()) {
				FieldInfo fieldInfo = embeddedIdInfo.getFieldInfo(name);
				if (fieldInfo == null) {
					continue;
				}			
				fields += fieldInfo.getColumn() + ",";
				values += "?,";
				Object parameter = fieldInfo.getValue(embeddedIdObj);
				parameters.add(parameter);
			}
		}		
		fields = fields.substring(0, fields.length() - 1) + ")";
		values = values.substring(0, values.length() - 1) + ")";
		hquery = hquery + fields + values;
		
		EntityManager entityManager = modelManager.getEntityManager();
		Query query = entityManager.createNativeQuery(hquery); // pakai native karena tidak disupport di JTA
		for (int i = 0; i < parameters.size(); i++) {
			query.setParameter(i + 1, parameters.get(i));
		}
		int result = query.executeUpdate();
		return result;
	}
	
	
	/*
	 * UPDATE
	 */
	private int update(ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		Map<String, Object> value = adminRequest.getValue();
		if (value == null || value.size() == 0) {
			throw new Exception("value is required");
		}
		Object object = AdminHelper.mapToObject(dataMapper, value, modelInfo.getModelClass());
		Map<String, Object> parameters = new LinkedHashMap<String, Object>();
		String hquery = "update " + modelInfo.getModelClass().getName() + " set ";
		for (String name : value.keySet()) {
			FieldInfo fieldInfo = modelInfo.getFieldInfo(name);
			if (fieldInfo == null) {
				continue;
			}
			hquery += name + "=:" + name + ",";
			Object parameter = fieldInfo.getValue(object);
			parameters.put(name, parameter);
		}
		hquery = hquery.substring(0, hquery.length() - 1);
		Where where = AdminHelper.where(modelManager, modelInfo, adminRequest, false);
		hquery += " " + where.query;
		EntityManager entityManager = modelManager.getEntityManager();
		Query query = entityManager.createQuery(hquery);
		for (String name : parameters.keySet()) {
			query.setParameter(name, parameters.get(name));
		}
		for (int i = 0; i < where.parameters.size(); i++) {
			query.setParameter(i + 1, where.parameters.get(i));
		}
		int result = query.executeUpdate();
		return result;
	}
	
	
	/*
	 * REMOVE
	 */
	private int remove(ModelInfo modelInfo, AdminRequest adminRequest) throws Exception {
		String hquery = "delete from " + modelInfo.getModelClass().getName();
		Where where = AdminHelper.where(modelManager, modelInfo, adminRequest, false);
		hquery += " " + where.query;
		EntityManager entityManager = modelManager.getEntityManager();
		Query query = entityManager.createQuery(hquery);
		for (int i = 0; i < where.parameters.size(); i++) {
			query.setParameter(i + 1, where.parameters.get(i));
		}
		int result = query.executeUpdate();
		return result;
	}

}
