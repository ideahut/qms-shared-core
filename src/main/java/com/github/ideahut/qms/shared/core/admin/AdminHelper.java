package com.github.ideahut.qms.shared.core.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ideahut.qms.shared.client.exception.ResultRuntimeException;
import com.github.ideahut.qms.shared.client.object.AdminRequest;
import com.github.ideahut.qms.shared.client.object.Page;
import com.github.ideahut.qms.shared.client.object.Result;
import com.github.ideahut.qms.shared.client.type.ConditionType;
import com.github.ideahut.qms.shared.client.type.IdType;
import com.github.ideahut.qms.shared.client.type.LogicalType;
import com.github.ideahut.qms.shared.core.context.RequestContext;
import com.github.ideahut.qms.shared.core.converter.TypeConverter;
import com.github.ideahut.qms.shared.core.mapper.DataMapper;
import com.github.ideahut.qms.shared.core.model.FieldInfo;
import com.github.ideahut.qms.shared.core.model.IdInfo;
import com.github.ideahut.qms.shared.core.model.ModelInfo;
import com.github.ideahut.qms.shared.core.model.ModelManager;
import com.github.ideahut.qms.shared.core.model.entity.SoftDeleteModel;
import com.github.ideahut.qms.shared.core.support.ResultAssert;

public final class AdminHelper {
	
	private AdminHelper() {}
	
	public static final String ITEM_SPLITTER 			= "~";
	public static final String KEYVAL_SPLITTER 			= ":";
	public static final String NAME_SPLITTER 			= "\\.";
	public static final String FILTER_SPLITTER 			= "--";
	public static final String STRING_ARRAY_SPLITTER	= ",";
	
	public static final ThreadLocal<Boolean> IGNORE_SOFT_DELETE = new InheritableThreadLocal<Boolean>();
	
	
	/*
	 * ADMIN REQUEST (BODY)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static AdminRequest getAdminRequest(DataMapper dataMapper, ModelManager modelManager, byte[] data) {
		AdminRequest adminRequest = new AdminRequest();
		JsonNode root = dataMapper.readData(data, JsonNode.class);
		JsonNode node = null;
				
		// model
		node = root.get("model");		
		ResultAssert.notNull(Result.ERROR("ERR-0", "model is null"), node);
		String model = node.asText().trim();
		ModelInfo modelInfo = modelManager.getAdminModelInfo(model);
		ResultAssert.notNull(Result.ERROR("ERR-1", "ModelInfo is not found"), modelInfo);
		adminRequest.setModel(model);
		
		// manager
		node = root.get("manager");
		if (node != null) {
			adminRequest.setManager(node.asText().trim());
		}
		
		// id
		node = root.get("id");
		if (node != null) {
			Object id = getIdObject(dataMapper, modelInfo, node);
			adminRequest.setId(id);
		}
		
		// ids
		node = root.get("ids");
		if (node != null && node.isArray()) {
			List ids = new ArrayList();
			for (JsonNode item : node) {
				ids.add(getIdObject(dataMapper, modelInfo, item));
			}
			adminRequest.setIds(ids);
		}
		
		// mapkey
		node = root.get("mapkey");
		if (node != null) {
			adminRequest.setMapkey(node.asText().trim());
		}
		
		// page
		node = root.get("page");
		if (node != null) {
			Page<?> page = dataMapper.convertData(node, Page.class);
			adminRequest.setPage(page);
		}
		
		// start
		node = root.get("start");
		if (node != null) {
			String start = node.asText().trim();
			if (!start.isEmpty()) {
				adminRequest.setStart(new Integer(start));
			}
		}
		
		// limit
		node = root.get("limit");
		if (node != null) {
			String limit = node.asText().trim();
			if (!limit.isEmpty()) {
				adminRequest.setLimit(new Integer(limit));
			}
		}
		
		// filter
		node = root.get("filter");
		if (node != null) {			
			adminRequest.setFilter(nodeToFilter(node));
		}
		
		// order
		node = root.get("order");
		if (node != null) {
			List<String> order = new ArrayList<String>();
			Iterator<JsonNode> iter = node.iterator();
			while (iter.hasNext()) {
				JsonNode item = iter.next();
				order.add(item.asText().trim());
			}
			adminRequest.setOrder(order);
		}
		
		// group
		node = root.get("group");
		if (node != null) {
			List<String> group = new ArrayList<String>();
			Iterator<JsonNode> iter = node.iterator();
			while (iter.hasNext()) {
				JsonNode item = iter.next();
				group.add(item.asText().trim());
			}
			adminRequest.setGroup(group);
		}
		
		// field
		node = root.get("field");
		if (node != null) {
			List<String> field = new ArrayList<String>();
			Iterator<JsonNode> iter = node.iterator();
			while (iter.hasNext()) {
				JsonNode item = iter.next();
				field.add(item.asText().trim());
			}
			adminRequest.setField(field);
		}
		
		// loadlazy
		node = root.get("loadlazy");
		if (node != null) {
			List<String> loadlazy = new ArrayList<String>();
			Iterator<JsonNode> iter = node.iterator();
			while (iter.hasNext()) {
				JsonNode item = iter.next();
				loadlazy.add(item.asText().trim());
			}
			adminRequest.setLoadlazy(loadlazy);
		}
		
		// value
		node = root.get("value");
		if (node != null && node.isObject()) {			
			adminRequest.setValue(nodeToValue(node));
		}
		
		// values
		node = root.get("values");
		if (node != null && node.isArray()) {
			List<Map<String, Object>> values = new ArrayList<Map<String,Object>>();
			for (JsonNode item : node) {
				values.add(nodeToValue(item));
			}
			adminRequest.setValues(values);
		}
		
		return adminRequest;
	}
	
	
	
	/*
	 * ADMIN REQUEST (PARAMETER)
	 * bulk (ids dan values) tidak diimplementasi untuk request parameter 
	 */
	public static AdminRequest getAdminRequest(DataMapper dataMapper, ModelManager modelManager) {
		HttpServletRequest request = RequestContext.currentContext().getRequest();
		if (request == null) {
			return null;
		}
		AdminRequest adminRequest = new AdminRequest();
		
		// model
		String model = request.getParameter("model");
		ResultAssert.notEmpty(Result.ERROR("ERR-2", "model is required"), model);
		ModelInfo modelInfo = modelManager.getAdminModelInfo(model);
		ResultAssert.notNull(Result.ERROR("ERR-3", "ModelInfo is not found"), modelInfo);
		adminRequest.setModel(model);
		
		// manager
		String manager = request.getParameter("manager");
		if (manager != null) {
			adminRequest.setManager(manager.trim());
		}
		
		// id
		String id = request.getParameter("id");
		if (id != null) {
			Object idval = getIdObject(dataMapper, modelInfo, id);
			adminRequest.setId(idval);
		}
		
		// mapkey
		String mapkey = request.getParameter("mapkey");
		if (mapkey != null) {
			adminRequest.setMapkey(mapkey.trim());
		}
		
		// page
		List<String> lpage = stringToList(request.getParameter("page")); // page=1,20,[count] => count bisa diisi 1/0 atau true/false
		if (lpage != null && !lpage.isEmpty()) {
			Page<?> page = Page.create(new Integer(lpage.get(0)));
			if (lpage.size() > 1) {
				page.setSize(new Integer(lpage.get(1).trim()));
			}
			if (lpage.size() > 2) {
				String count = lpage.get(2).trim().toLowerCase();
				page.setCount("1".equals(count) || "true".equals(count));
			}
			adminRequest.setPage(page);
		}
		
		// start
		String start = request.getParameter("start");
		if (start != null) {
			start = start.trim();
			if (!start.isEmpty()) {
				adminRequest.setStart(new Integer(start));
			}
		}
		
		// limit
		String limit = request.getParameter("limit");
		if(limit != null) {
			limit = limit.trim();
			if (!limit.isEmpty()) {
				adminRequest.setLimit(new Integer(limit));
			}
		}
		
		// filter
		adminRequest.setFilter(stringToFilter(request.getParameter("filter")));
		
		// order
		adminRequest.setOrder(stringToList(request.getParameter("order")));
		
		// group
		adminRequest.setGroup(stringToList(request.getParameter("group")));
		
		// field
		adminRequest.setField(stringToList(request.getParameter("field")));
		
		// loadlazy
		adminRequest.setLoadlazy(stringToList(request.getParameter("loadlazy")));
		
		// value
		adminRequest.setValue(stringToMap(request.getParameter("value")));		
		
		return adminRequest;
	}	
	
	
	
	/*
	 * INVOKE OBJECT
	 */	
	public static <T> T invokeObject(DataMapper dataMapper, ModelInfo modelInfo, T model, Map<String, Object> value) throws Exception {
		return invokeObject(dataMapper, modelInfo, model, value, false, null);
	}
	
	public static <T> T invokeObject(DataMapper dataMapper, ModelInfo modelInfo, T model, Map<String, Object> value, boolean isRefreshObject) throws Exception {
		return invokeObject(dataMapper, modelInfo, model, value, isRefreshObject, null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T invokeObject(DataMapper dataMapper, ModelInfo modelInfo, T model, Map<String, Object> value, boolean isRefreshObject, Session session) throws Exception {
		/*
		 * isRefreshObject digunakan untuk kasus update, 
		 * Dimana field yang juga model harus diambil fresh object dari db berdasarkan id-nya
		 * >> Lihat: OneToMany && ManyToOne
		 */
		ModelManager modelManager = modelInfo.getModelManager();
		Object valueObject = dataMapper.convertData(value, modelInfo.getModelClass());
		
		for (String key : value.keySet()) {
			FieldInfo fieldInfo = modelInfo.getFieldInfo(key);
			if (fieldInfo != null) {
				Object mval = value.get(key);
				if (mval != null) {
					if (mval instanceof Map) {
						ModelInfo nextModelInfo = modelManager.getModelInfo(fieldInfo.getType());
						if (isRefreshObject) {
							IdInfo nextIdInfo = nextModelInfo.getIdInfo();
							if (IdType.composite.equals(nextIdInfo.getType())) 
							{
								boolean isSetToNull = true;
								for (String nextIdField : nextIdInfo.getFields()) {
									if (!((Map)mval).containsKey(nextIdField)) {
										continue;
									}
									Object mapIdValue = ((Map)mval).get(nextIdField);
									isSetToNull = isSetToNull && mapIdValue == null || "".equals(mapIdValue + ""); 
								}
								if (isSetToNull) {
									fieldInfo.setValue(model, null);
									continue;
								}
								Object newModelObject = dataMapper.convertData(mval, nextModelInfo.getModelClass());
								Object[] arrIdValue = new Object[nextIdInfo.getFields().size()];
								Object oldModelObject = fieldInfo.getValue(model);
								int i = 0;
								boolean isContinue = true;
								for (String nextIdField : nextIdInfo.getFields()) {
									FieldInfo idFieldInfo = nextModelInfo.getFieldInfo(nextIdField);
									Object nidValue = idFieldInfo.getValue(newModelObject);
									arrIdValue[i] = nidValue;
									Object oidValue = oldModelObject != null ? idFieldInfo.getValue(oldModelObject) : null;
									isContinue = isContinue && (oidValue != null && oidValue.equals(nidValue));
									i++;
								}
								if (isContinue) {
									continue;
								}
								
								// Ambil fresh object dari database untuk diset ke model
								String hql = "from " + nextModelInfo.getModelClass().getSimpleName() + " a where 1=1";
								List<Object> parameters = new ArrayList<Object>();
								i = 1;
								for (String nextIdField : nextIdInfo.getFields()) {
									hql += " and a." + nextIdField + "=?" + i;
									parameters.add(arrIdValue[i - 1]);
									i++;
								}
								
								Object freshModelObject;
								Query query;
								if (session != null) {
									query = session.createQuery(hql);									
								} else {
									query = modelManager.getEntityManager().createQuery(hql);
								}
								for (int j = 0; i < parameters.size(); j++) {
									query.setParameter(j + 1, parameters.get(j));
								}
								query.setMaxResults(1);
								freshModelObject = query.getSingleResult();
								if (freshModelObject == null) { // model tidak tersedia jadi di-skip
									continue;
								}
								fieldInfo.setValue(model, freshModelObject);
							} 
							else 
							{
								String nextIdField = nextIdInfo.getFields().iterator().next();
								if (!((Map)mval).containsKey(nextIdField)) {
									continue;
								}
								Object idValue = ((Map)mval).get(nextIdField);
								if (idValue == null || "".equals(idValue + "")) {
									fieldInfo.setValue(model, null);
									continue;
								}							
								Object newModelObject = dataMapper.convertData(mval, nextModelInfo.getModelClass());
								
								FieldInfo idFieldInfo = nextModelInfo.getFieldInfo(nextIdField);
								Object newIdValue = idFieldInfo.getValue(newModelObject);
								if (newIdValue != null) {
									Object oldModelObject = fieldInfo.getValue(model);
									Object oldIdValue = oldModelObject != null ? idFieldInfo.getValue(oldModelObject) : null;
									if (oldIdValue != null && oldIdValue.equals(newIdValue)) {
										// re-initialize
										if (fieldInfo.isLazyObject()) {
											Hibernate.initialize(oldModelObject);
										}
										continue;
									}
									
									// Ambil fresh object dari database untuk diset ke model
									Object freshModelObject;
									if (session != null) {
										freshModelObject = session.get(nextModelInfo.getModelClass(), (Serializable)newIdValue);
									} else {
										freshModelObject = modelManager.getEntityManager().find(nextModelInfo.getModelClass(), newIdValue);
									}
									if (freshModelObject == null) { // model tidak tersedia jadi di-skip
										continue;
									}
									fieldInfo.setValue(model, freshModelObject);
								}
							}
						} else {
							Object fieldValue = fieldInfo.getType().newInstance();
							fieldValue = invokeObject(dataMapper, nextModelInfo, fieldValue, (Map<String, Object>)mval, isRefreshObject, session);
							fieldInfo.setValue(model, fieldValue);
						}
					} 
					else if (fieldInfo.getType().equals(mval.getClass())) {
						fieldInfo.setValue(model, mval);
					} 
					else {
						fieldInfo.setValue(model, fieldInfo.getValue(valueObject));
					}
				} else {
					fieldInfo.setValue(model, null);
				}
			}
		}		
		return model;
	}
	
	
	
	/*
	 * NODE TO VALUE
	 */
	public static Map<String, Object> nodeToValue(JsonNode node) {
		Map<String, Object> value = new LinkedHashMap<String, Object>();
		Iterator<String> iter = node.fieldNames();
		while (iter.hasNext()) {
			String name = iter.next();
			JsonNode item = node.get(name);
			if (item.isContainerNode()) {
				value.put(name, nodeToMap(item));
			} else {
				value = mapNameValue(value, name, item.asText());
			}		
		}
		return value;
	}
	
	
	
	/*
	 * NODE TO MAP
	 */
	public static Map<String, Object> nodeToMap(JsonNode node) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		Iterator<String> iter = node.fieldNames();
		while(iter.hasNext()) {
			String name = iter.next();
			JsonNode item = node.get(name);
			if (item.isContainerNode()) {
				map.put(name, nodeToMap(item));
			} else {
				map.put(name, item.asText());
			}
		}
		return map;
	}
	
	
	
	/*
	 * NAME VALUE TO MAP
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> mapNameValue(Map<String, Object> map, String name, Object value) {
		String[] names = name.split(NAME_SPLITTER);
		Map<String, Object> temp = map;
		for (int i = 0; i < names.length - 1; i++) {
			Map<String, Object> nmap = (Map<String, Object>)temp.get(names[i]);
			if (nmap == null) {
				nmap = new LinkedHashMap<String, Object>();
				temp.put(names[i], nmap);
			}
			temp = nmap;
		}
		temp.put(names[names.length - 1], value);
		return map;
	}
	
	
	
	/*
	 * STRING TO MAP
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> stringToMap(String string) {
		if (string == null) {
			return null;
		}
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		String[] items = string.split(ITEM_SPLITTER);
		for (String item : items) {
			String[] keyvals = item.split(KEYVAL_SPLITTER);
			if (keyvals.length != 2) {
				continue;
			}
			String[] names = keyvals[0].split(NAME_SPLITTER);
			Map<String, Object> temp = result;
			for (int i = 0; i < names.length - 1; i++) {
				Map<String, Object> nmap = (Map<String, Object>)temp.get(names[i]);
				if (nmap == null) {
					nmap = new LinkedHashMap<String, Object>();
					temp.put(names[i], nmap);
				}
				temp = nmap;
			}
			temp.put(names[names.length - 1], keyvals[1]);
		}
		return result;
	}
	
	
	
	/*
	 * MAP TO OBJECT
	 */
	@SuppressWarnings("unchecked")
	public static <T> T mapToObject(DataMapper dataMapper, Map<String, Object> map, Class<?> type) {
		if (map == null) {
			return null;
		}
		return (T)dataMapper.convertData(map, type);
	}
	
	
	/*
	 * BYTE ARRAY TO MAP
	 */
	public static Map<String, Object> byteArrayToValue(DataMapper dataMapper, byte[] data) {
		JsonNode node = dataMapper.readData(data, JsonNode.class);
		Map<String, Object> map = nodeToValue(node);
		return map;
	}
	
	
	
	/*
	 * STRING TO OBJECT
	 */
	public static <T> T stringToObject(DataMapper dataMapper, String value, Class<T> type) {
		Map<String, Object> map = stringToMap(value);
		if (map == null) {
			return null;
		}
		T object = dataMapper.convertData(map, type);
		return object;
	}
	
	
	
	/*
	 * STRING TO LIST
	 */
	public static List<String> stringToList(String string) {
		if (string == null) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		String[] stringArray = string.split(STRING_ARRAY_SPLITTER);
		for (String value : stringArray) {
			value = value.trim();
			if (value.isEmpty()) {
				continue;
			}
			list.add(value);
		}
		return list;
	}
	
	
		
	/*
	 * STRING TO FILTER LIST
	 */
	// filter=and--name--eq--thomson akajsk sjsjsk~id--like--mugkin lsksksl~umur--between--10--20
	public static List<AdminRequest.Filter> stringToFilter(String string) {
		if (string == null) {
			return null;
		}
		List<AdminRequest.Filter> result = new ArrayList<AdminRequest.Filter>();
		String[] items = string.split(ITEM_SPLITTER);
		for (String item : items) {
			String[] filters = item.split(FILTER_SPLITTER);
			if (filters.length < 3) {
				throw new ResultRuntimeException(Result.ERROR("20", "Invalid filter length"));
			}
			LogicalType logical = LogicalType.and;
			if (filters.length > 3) {
				String slogical = filters[0].trim().toLowerCase();
				if ("and".equals(slogical) || "or".equals(slogical)) {
					logical = LogicalType.valueOf(slogical);
					filters = Arrays.copyOfRange(filters, 1, filters.length);
				}
				filters[2] = String.join(FILTER_SPLITTER, Arrays.copyOfRange(filters, 2, filters.length));
			}
			String field = filters[0];
			ConditionType condition = ConditionType.getConditionType(filters[1], ConditionType.EQUAL);
			String value = filters[2];
			AdminRequest.Filter filter = AdminRequest.Filter.NEW(logical, field, condition, value);
			result.add(filter);
		}
		return result;
	}
	
	
	
	/*
	 * NODE TO FILTER
	 */
	public static List<AdminRequest.Filter> nodeToFilter(JsonNode node) {
		List<AdminRequest.Filter> result = new ArrayList<AdminRequest.Filter>();
		Iterator<JsonNode> iter = node.iterator();
		while (iter.hasNext()) {
			AdminRequest.Filter filter = new AdminRequest.Filter();
			JsonNode item = iter.next();
			JsonNode field = item.get("field");
			ResultAssert.notNull(Result.ERROR("ERR-4", "Filter field is required"), field);
			filter.setField(field.asText().trim());
			
			JsonNode jcondition = item.get("condition");
			ConditionType condition = ConditionType.EQUAL;
			if (jcondition != null) {
				condition = ConditionType.getConditionType(jcondition.asText().trim().toLowerCase(), ConditionType.EQUAL);					
			}
			filter.setCondition(condition);
			
			JsonNode jlogical = item.get("logical");
			LogicalType logical = LogicalType.and;
			if (jlogical != null) {
				logical = LogicalType.getLogicalType(jlogical.asText().trim(), LogicalType.and);					
			}
			filter.setLogical(logical);
			
			JsonNode value = item.get("value");
			if (value != null) {
				filter.setValue(value.asText());
			}
			result.add(filter);
		}
		return result;
	}
	
	
	
	/*
	 * FILTER TO MAP
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> filterToMap(List<AdminRequest.Filter> filter) {
		if (filter == null) {
			return null;
		}
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		for (AdminRequest.Filter af : filter) {
			String[] names = af.getField().split(NAME_SPLITTER);
			Map<String, Object> temp = result;
			for (int i = 0; i < names.length - 1; i++) {
				Map<String, Object> nmap = (Map<String, Object>)temp.get(names[i]);
				if (nmap == null) {
					nmap = new LinkedHashMap<String, Object>();
					temp.put(names[i], nmap);
				}
				temp = nmap;
			}
			temp.put(names[names.length - 1], af);
		}
		return result;
	}
	
	
	/*
	 * BUILDER
	 */
	public static Builder builder(ModelManager modelManager, ModelInfo modelInfo, AdminRequest adminRequest, boolean isSelect) {
		Where where = where(modelManager, modelInfo, adminRequest, isSelect);		
		
		/*
		 * FIELD
		 */
		String fieldQL = "";
        Map<String, String[]> qjoin = new LinkedHashMap<String, String[]>(); 
        if (adminRequest.getField() != null && !adminRequest.getField().isEmpty()) {
        	int count = 0;
        	String s_dql = "";
        	for (String name : adminRequest.getField()) {
        		name = name.replace(".", "__");
        		String[] expname = name.split("__");
                int lenname = expname.length;
                String s_alias = where.alias;
                String s_name = expname[lenname - 1];
                if (lenname > 1) {
                    for (int i = 1; i < lenname - 1; i++) {
                    	String altname = String.join("__", Arrays.copyOfRange(expname, 0, i + 1));
                        if (!qjoin.containsKey(altname)) {
                        	if (where.joins.containsKey(altname)) {
                        		qjoin.put(altname, where.joins.get(altname));
                        		where.joins.remove(altname);
                        	} else {
                        		count++;
                        		String prevname = String.join("__", Arrays.copyOfRange(expname, 0, i));
                        		qjoin.put(altname, new String[] {"b" + count, where.joins.get(prevname)[0] + "." + expname[i]});
                        	}
                        }
                        String prevname = String.join("__", Arrays.copyOfRange(expname, 0, lenname - 1));
                        s_alias = qjoin.get(prevname)[0];
                        s_dql += s_alias + "." + s_name + ","; 
                    }
                } else {
                    s_name = name;
                    s_dql += s_alias + "." + s_name + ",";
                }
        	}
        	s_dql = s_dql.substring(0, s_dql.length() - 1);
        	fieldQL = s_dql;
        }
        for (String key : where.joins.keySet()) {
        	qjoin.put(key, where.joins.get(key));
        }
        where.joins = qjoin;
        
        /*
         * ORDER
         */
        String orderQL = "";
        if (adminRequest.getOrder() != null && !adminRequest.getOrder().isEmpty()) {
        	IdInfo idInfo = modelInfo.getIdInfo();
        	for (String name : adminRequest.getOrder()) {
        		name = name.replace(".", "__");
        		boolean is_asc = true;
        		if (name.startsWith("-")) {
        			is_asc = false;
        			name = name.substring(1);
        		}
        		String[] expname = name.split("__");
        		int lenname = expname.length;
        		String s_alias = where.alias;
        		String s_name = expname[lenname - 1];
        		if(lenname > 1) {
        			String prevname = String.join("__", Arrays.copyOfRange(expname, 0, lenname - 1));
        			if (idInfo.getFields().contains(prevname)) {
        				s_alias += "." + prevname;
        			} else {
        				s_alias = where.joins.get(prevname)[0];
        			}
        		}
        		orderQL += s_alias + "." + s_name + (is_asc ? " asc" : " desc") + ",";
        	}
        	orderQL = orderQL.substring(0, orderQL.length() - 1);
        }
        
        Builder builder = new Builder();
        builder.where = where;
        builder.fieldQL = fieldQL;
        builder.orderQL = orderQL;
		return builder;
	}
	
	public static class Builder {
		public Where where;
		public String fieldQL;
		public String orderQL;
	}
	
	
	
	/*
	 * WHERE
	 */
	public static Where where(ModelManager modelManager, ModelInfo modelInfo, AdminRequest adminRequest, boolean isSelect) {
		IdInfo idInfo = modelInfo.getIdInfo();
		Where where = new Where();
		List<AdminRequest.Filter> filters = adminRequest.getFilter();
		int count  = 0;
        String alias  = "a" + count;
        Boolean isIgnoreSoftDelete = IGNORE_SOFT_DELETE.get();
        boolean isSoftDelete = SoftDeleteModel.class.isAssignableFrom(modelInfo.getModelClass()) && (isIgnoreSoftDelete == null || Boolean.FALSE.equals(isIgnoreSoftDelete));
    	where.query += "where ";
    	if (isSoftDelete) {
    		where.query += alias + "." + SoftDeleteModel.FIELD_NAME + "=?1 ";
    		where.parameters.add(SoftDeleteModel.FLAG_NO);
    	} else {
    		where.query += "1=1 ";
    	}
    	IGNORE_SOFT_DELETE.remove();
        if (filters != null && !filters.isEmpty()) {
        	for (AdminRequest.Filter filter : filters) {
        		if (filter.getField() == null) {
        			throw new RuntimeException("Filter field is required");
        		}
        		String logic = filter.getLogical().name();
        		String i_dql = "";
        		String name  = filter.getField().replace(".", "__");
        		String[] expname = name.split("__");
        		int lenname = expname.length;
                String i_alias = alias;
                String i_name  = expname[lenname - 1];
                if (isSelect) {
                	if (IdType.embedded.equals(idInfo.getType()) && expname[0].equals(idInfo.getFields().iterator().next())) {
                		i_dql = i_alias + "." + expname[0] + "." + i_name;
                	} else {
	                	if (lenname > 1) {
		                	count++;
		                	where.joins.put(expname[0], new String[] {"a" + count, alias + "." + expname[0]});
		                	for (int i = 0; i < lenname - 1; i++) {
		                		String altname = String.join("__", Arrays.copyOfRange(expname, 0, i + 1));
		                		if (where.joins.get(altname) == null) {
		                			count++;
		                			String prevname = String.join("__", Arrays.copyOfRange(expname, 0, i));
		                			where.joins.put(altname, new String[] {"a" + count, where.joins.get(prevname)[0] + "." + expname[i]});
		                		}
		                	}
		                	String prevname = String.join("__", Arrays.copyOfRange(expname, 0, lenname - 1));
		                	i_alias = where.joins.get(prevname)[0];
		                	i_dql += i_alias + "." + i_name;
	                	} else {
	                		i_name = name;
	                        i_dql += i_alias + "." + i_name;
	                	}
                	}
                } else {
                	if (IdType.embedded.equals(idInfo.getType()) && expname[0].equals(idInfo.getFields().iterator().next())) {
                		i_dql = expname[0] + "." + i_name;
                	} else {
	                	i_name = name;
	                    i_dql += i_name;
                	}
                }
                List<Object> lvals;
                String[] values;
                ConditionType condition = filter.getCondition();
                int index = where.parameters.size() + 1;
                switch (condition) {
                case ANY_LIKE:
                	where.query += logic + " lower(" + i_dql + ") like ?" + index;
                	where.parameters.add("%" + (filter.getValue() + "").toLowerCase() + "%");
                	break;
                case ANY_START:
                	where.query += logic + " lower(" + i_dql + ") like ?" + index;
                	where.parameters.add((filter.getValue() + "").toLowerCase() + "%");
                	break;
                case ANY_END:
                	where.query += logic + " lower(" + i_dql + ") like ?" + index; 
                	where.parameters.add((filter.getValue() + "").toLowerCase());
                	break;
                case ANY_EQUAL:
                	where.query += logic + " lower(" + i_dql + ") = ?" + index; 
                	where.parameters.add((filter.getValue() + "").toLowerCase());
                	break;
                case LIKE:
                	where.query += logic + " " + i_dql + " like ?" + index; 
                	where.parameters.add(filter.getValue() + "");
                	break;
                case START:
                	where.query += logic + " " + i_dql + " like ?" + index; 
                	where.parameters.add(filter.getValue() + "%");
                	break;
                case END:
                	where.query += logic + " " + i_dql + " like ?" + index; 
                	where.parameters.add("%" + filter.getValue());
                	break;
                case NOT_EQUAL:
                	where.query += logic + " " + i_dql + " not in (?" + index + ")"; 
                	where.parameters.add(getFieldValue(modelManager, modelInfo, filter.getField(), filter.getValue()));
                	break;
                case BETWEEN:
                	values = filter.getValue().split(",");
                	if (values.length < 2) {
                		throw new RuntimeException("Invalid 'BETWEEN' value");
                	}
                	where.query += logic + " " + i_dql + " between ?" + index + " and ?" + (index + 1);
                	where.parameters.add(getFieldValue(modelManager, modelInfo, filter.getField(), values[0]));
                	where.parameters.add(getFieldValue(modelManager, modelInfo, filter.getField(), values[1]));
                	break;
                case NOT_NULL:
                	where.query += logic + " " + i_dql + " is not null";
                	break;
                case IS_NULL:
                	where.query += logic + " " + i_dql + " is null";
                	break;
                case GREATER_THAN:
                	where.query += logic + " " + i_dql + " > ?" + index;
                	where.parameters.add(getFieldValue(modelManager, modelInfo, filter.getField(), filter.getValue()));
                    break;
                case GREATER_EQUAL:
                	where.query += logic + " " + i_dql + " >= ?" + index;
                	where.parameters.add(getFieldValue(modelManager, modelInfo, filter.getField(), filter.getValue()));
                	break;
                case LESS_THAN:
                	where.query += logic + " " + i_dql + " < ?" + index;
                	where.parameters.add(getFieldValue(modelManager, modelInfo, filter.getField(), filter.getValue()));
                	break;
                case LESS_EQUAL:
                	where.query += logic + " " + i_dql + " <= ?" + index;
                	where.parameters.add(getFieldValue(modelManager, modelInfo, filter.getField(), filter.getValue()));
                	break;
                case IN:
                	values = filter.getValue().split(",");
                	if (values.length == 0) {
                		throw new RuntimeException("Invalid 'in' value");
                	}
                	lvals = new ArrayList<Object>();
                	for (String str : values) {
                		lvals.add(getFieldValue(modelManager, modelInfo, filter.getField(), str));
                	}
                	where.query += logic + " " + i_dql + " in (?" + index + ")";
                	where.parameters.add(lvals.toArray());
                	break;
                case NOT_IN:
                	values = filter.getValue().split(",");
                	if (values.length == 0) {
                		throw new RuntimeException("Invalid 'notin' value");
                	}
                	lvals = new ArrayList<Object>();
                	for (String str : values) {
                		lvals.add(getFieldValue(modelManager, modelInfo, filter.getField(), str));
                	}
                	where.query += logic + " " + i_dql + " not in (?" + index + ")";
                	where.parameters.add(lvals.toArray());
                	break;
                case EQUAL:
                	
				default:
					where.query += logic + " " + i_dql + "=?" + index;
					where.parameters.add(getFieldValue(modelManager, modelInfo, filter.getField(), filter.getValue()));
					break;
				}
            }
		}
        where.alias = alias;
        return where;
	}	
	
	public static class Where {
		public String query = "";
		public String alias = "";		
		public List<Object> parameters = new  ArrayList<Object>();
		public Map<String, String[]> joins = new LinkedHashMap<String, String[]>();
	}	
	
	
	/*
	 * FIELD INFO
	 */
	public static FieldInfo getFieldInfo(ModelManager modelManager, ModelInfo modelInfo, String anyField) {
		String[] fields = anyField.split(NAME_SPLITTER);
		FieldInfo fieldInfo = modelInfo.getFieldInfo(fields[0]);
		if (fields.length > 1) {
			ModelInfo nextModelInfo = modelManager.getModelInfo(fieldInfo.getType());
			if (nextModelInfo == null) {
				return null;
			}
			String[] nextFields = Arrays.copyOfRange(fields, 1, fields.length);
			for (String nextAnyField : nextFields) {
				fieldInfo = getFieldInfo(modelManager, nextModelInfo, nextAnyField);
			}
		}		
		return fieldInfo;
	}
		
	
	
	/*
	 * VALUE
	 */
	public static Object getFieldValue(ModelManager modelManager, ModelInfo modelInfo, String anyField, String value) {
		try {
			FieldInfo fieldInfo = getFieldInfo(modelManager, modelInfo, anyField);
			if (fieldInfo == null) {
				return null;
			}
			TypeConverter converter = fieldInfo.getConverter();
			if (converter == null || TypeConverter.COLLECTION.equals(converter)) {
				return null;
			}
			if (TypeConverter.MODEL.equals(converter)) {
				return fieldInfo.getType().newInstance();
			}		
			return converter.getAction().convert(fieldInfo.getType(), value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/*
	 * GET ID OBJECT FROM NODE
	 */
	public static Object getIdObject(DataMapper dataMapper, ModelInfo modelInfo, JsonNode node) {
		Object id = null;
		IdInfo idInfo = modelInfo.getIdInfo();
		switch (idInfo.getType()) {
		case embedded:
			id = dataMapper.convertData(node, idInfo.getEmbeddedIdInfo().getModelClass());
			break;
		case composite:
			id = nodeToMap(node);			
			break;
		case standard:
			id = modelInfo.getFieldInfo(idInfo.getFields().iterator().next()).convert(node.asText());
			break;
		default:
			break;
		}
		return id;
	}
	
	/*
	 * GET ID OBJECT FROM STRING
	 */
	public static Object getIdObject(DataMapper dataMapper, ModelInfo modelInfo, String value) {
		Object id = null;
		IdInfo idInfo = modelInfo.getIdInfo();
		switch (idInfo.getType()) {
		case embedded:
			id = stringToObject(dataMapper, value, idInfo.getEmbeddedIdInfo().getModelClass());
			break;
		case composite:
			id = stringToMap(value);
			break;
		case standard:
			id = modelInfo.getFieldInfo(idInfo.getFields().iterator().next()).convert(value);
			break;
		default:
			break;
		}
		return id;
	}
	
}
