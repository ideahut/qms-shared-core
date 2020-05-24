package com.github.ideahut.qms.shared.core.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.github.ideahut.qms.shared.client.type.IdType;
import com.github.ideahut.qms.shared.core.converter.TypeConverter;

public class ModelInfo {
	
	private final ModelManager modelManager;
	
	private final Class<?> modelClass;
	
	private final String tableName;
	
	private final String tableSchema;
	
	private final boolean isEntity; 
	
	private final Map<String, FieldInfo> fieldInfos;
	
	private final Map<String, FieldInfo> columnInfos;
	
	private final Map<String, FieldInfo> lazyObjectFields;
	
	private final Map<String, FieldInfo> lazyCollectionFields;
	
	private final Set<String> uniqueFields;
	
	private final IdInfo idInfo;
	
	private ModelInfo parentInfo;
	
	
	public ModelInfo(ModelManager modelManager, Class<?> modelClass) throws Exception {
		String tableSchema = "";
		String tableName = modelClass.getSimpleName().toLowerCase();
		Entity annotEntity = modelClass.getAnnotation(Entity.class);
		boolean isEntity = annotEntity != null;
		Table annotTable = modelClass.getAnnotation(Table.class);
		if (annotTable != null) {
			tableName = annotTable.name();
			tableSchema = annotTable.schema();
		}
		Map<String, FieldInfo> fieldInfos = new HashMap<String, FieldInfo>();
		Map<String, FieldInfo> columnInfos = new HashMap<String, FieldInfo>();
		Map<String, FieldInfo> lazyObjectFields = new HashMap<String, FieldInfo>();		
		Map<String, FieldInfo> lazyCollectionFields = new HashMap<String, FieldInfo>();
		Set<String> uniqueFields = new HashSet<String>();
		Set<String> idFields = new HashSet<String>();
		ModelInfo embeddedIdInfo = null;
		
		Class<?> theClass = modelClass;
		while (theClass != null) {
			for (Field field : theClass.getDeclaredFields()) {
				ModelInfo idEmbeddedInfo = populate(field, fieldInfos, columnInfos, lazyObjectFields, lazyCollectionFields, idFields, uniqueFields);
				if (idEmbeddedInfo != null) {
					embeddedIdInfo = idEmbeddedInfo;
				}				
			}
			theClass = theClass.getSuperclass();
		}	
		
		if (idFields.size() == 1) {
			uniqueFields.add(idFields.iterator().next());
		}
		IdInfo idInfo = new IdInfo();
		idInfo.setFields(idFields);
		if (idFields.size() > 1) {
			idInfo.setType(IdType.composite);
		} else if (idFields.size() == 1) {
			if (embeddedIdInfo != null) {
				idInfo.setEmbeddedIdInfo(embeddedIdInfo);
				idInfo.setType(IdType.embedded);
			} else {
				idInfo.setType(IdType.standard);
			}
		} else {
			idInfo.setType(IdType.none);
		}
		
		this.modelManager 			= modelManager;
		this.idInfo					= idInfo;
		this.uniqueFields			= Collections.unmodifiableSet(uniqueFields);
		this.fieldInfos				= Collections.unmodifiableMap(fieldInfos);
		this.lazyObjectFields		= Collections.unmodifiableMap(lazyObjectFields);
		this.lazyCollectionFields	= Collections.unmodifiableMap(lazyCollectionFields);
		this.columnInfos			= Collections.unmodifiableMap(columnInfos);
		this.modelClass 			= modelClass;
		this.tableName				= tableName;
		this.tableSchema			= tableSchema;
		this.isEntity				= isEntity;
	}

	public ModelManager getModelManager() {
		return modelManager;
	}

	public Class<?> getModelClass() {
		return modelClass;
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return modelClass.getAnnotation(annotationClass);
	}
	
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return modelClass.getAnnotationsByType(annotationClass);
	}
	
	public Annotation[] getAnnotations() {
		return modelClass.getAnnotations();
	}

	public String getTableName() {
		return tableName;
	}

	public String getTableSchema() {
		return tableSchema;
	}

	public boolean isEntity() {
		return isEntity;
	}

	public FieldInfo getFieldInfo(String name) {
		return fieldInfos.get(name);
	}
	
	public Set<String> getFieldInfoNames() {
		return fieldInfos.keySet();
	}
	
	public FieldInfo getColumnInfo(String name) {
		return columnInfos.get(name);
	}
	
	public Set<String> getColumnInfoNames() {
		return columnInfos.keySet();
	}

	public Map<String, FieldInfo> getLazyObjectFields() {
		return lazyObjectFields;
	}

	public Map<String, FieldInfo> getLazyCollectionFields() {
		return lazyCollectionFields;
	}

	public boolean isUniqueField(String field) {
		return uniqueFields.contains(field);
	}

	public IdInfo getIdInfo() {
		return idInfo;
	}

	public ModelInfo getParentInfo() {
		return parentInfo;
	}		
	
	public ModelInfo prepare() {
		for (FieldInfo fieldInfo : fieldInfos.values()) {
			Class<?> type = fieldInfo.getType();
			TypeConverter converter = TypeConverter.getByType(type);
			if (converter == null && modelManager != null && modelManager.getModelInfo(type) != null) {
				converter = TypeConverter.MODEL;
			}
			fieldInfo.setConverter(converter);
		}
		if (IdType.embedded.equals(idInfo.getType())) {
			for (FieldInfo fieldInfo : idInfo.getEmbeddedIdInfo().fieldInfos.values()) {
				Class<?> type = fieldInfo.getType();
				TypeConverter converter = TypeConverter.getByType(type);
				if (converter == null && modelManager != null && modelManager.getModelInfo(type) != null) {
					converter = TypeConverter.MODEL;
				}
				fieldInfo.setConverter(converter);
			}
		}
		return this;
	}
	
	
	
	private ModelInfo populate(
		Field field, Map<String, FieldInfo> fieldInfos, Map<String, FieldInfo> columnInfos, 
		Map<String, FieldInfo> lazyObjectFields, Map<String, FieldInfo> lazyCollectionFields, 
		Set<String> idFields, Set<String> uniqueFields
	) throws Exception {
		Transient annotTransient = field.getAnnotation(Transient.class);
		if (annotTransient != null) {
			return null;
		}
		String name = field.getName();
		field.setAccessible(true);
					
		Class<?> type = field.getType();
		boolean lazyObject = false;
		boolean lazyCollection = false;
		if (Collection.class.isAssignableFrom(type)) {
			javax.persistence.OneToMany oneToMany = field.getAnnotation(javax.persistence.OneToMany.class);
			javax.persistence.ManyToMany manyToMany = field.getAnnotation(javax.persistence.ManyToMany.class);
			if (oneToMany != null) {
				lazyCollection = FetchType.LAZY.equals(oneToMany.fetch());
			} 
			else if (manyToMany != null) {
				lazyCollection = FetchType.LAZY.equals(manyToMany.fetch());
			}				
		} else {
			javax.persistence.ManyToOne manyToOne = field.getAnnotation(javax.persistence.ManyToOne.class);
			if (manyToOne != null) {
				lazyObject = FetchType.LAZY.equals(manyToOne.fetch());
			}
		}
		String columnName = field.getName();
		javax.persistence.Column column = field.getAnnotation(javax.persistence.Column.class);
		javax.persistence.JoinColumn joinColumn = field.getAnnotation(javax.persistence.JoinColumn.class);
		if (column != null) {
			if (!column.name().isEmpty()) {
				columnName = column.name();
			}
			if (column.unique()) {
				uniqueFields.add(name);
			}
		}
		else if (joinColumn != null) {
			if (!joinColumn.name().isEmpty()) {
				columnName = joinColumn.name();
			}
			if (joinColumn.unique()) {
				uniqueFields.add(name);
			}
		}
		
		FieldInfo fieldInfo = new FieldInfo(this, field, columnName, lazyObject, lazyCollection);
		fieldInfos.put(name, fieldInfo);			
		columnInfos.put(columnName, fieldInfo);
		columnInfos.put(columnName.toLowerCase(), fieldInfo);
		if (fieldInfo.isLazyObject()) {
			lazyObjectFields.put(name, fieldInfo);
		} else if (fieldInfo.isLazyCollection()) {
			lazyCollectionFields.put(name, fieldInfo);
		}
		
		ModelInfo embeddedIdInfo = null;
		javax.persistence.EmbeddedId embeddedId = field.getAnnotation(javax.persistence.EmbeddedId.class);
		if (embeddedId != null) {
			embeddedIdInfo = new ModelInfo(modelManager, field.getType());
			embeddedIdInfo.parentInfo = this;
			idFields.add(name);
		} else {
			javax.persistence.Id id = field.getAnnotation(javax.persistence.Id.class);
			if (id != null) {
				idFields.add(name);
			}
		}
		return embeddedIdInfo;
	}
	
}
