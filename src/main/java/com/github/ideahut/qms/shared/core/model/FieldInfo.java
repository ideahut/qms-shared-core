package com.github.ideahut.qms.shared.core.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.json.bind.annotation.JsonbDateFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.ideahut.qms.shared.core.converter.TypeConverter;

public class FieldInfo {
	
	private final ModelInfo modelInfo;
	
	private final Field field;
	
	private final String column;
	
	private final Method setter;
	
	private final Method getter;
	
	private final boolean lazyObject;
	
	private final boolean lazyCollection;
	
	private final String format;
	
	private TypeConverter typeConverter;
	
	public FieldInfo(ModelInfo modelInfo, Field field, String column, boolean lazyObject, boolean lazyCollection) {
		Class<?> modelClass = field.getDeclaringClass();
		String fieldName = field.getName();
		String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		Method setter = null;
		try {
			setter = modelClass.getMethod("set" + methodName, field.getType());
		} catch (Exception e) {}
		Method getter = null;
		try {
			getter = modelClass.getMethod("get" + methodName);
		} catch (Exception e) {}
		String format = null;
		JsonFormat jsonFormat = field.getAnnotation(JsonFormat.class);
		if (jsonFormat != null) {
			format = jsonFormat.pattern().trim();
		} else {
			JsonbDateFormat jsonbDateFormat = field.getAnnotation(JsonbDateFormat.class);
			if (jsonbDateFormat != null) {
				format = jsonbDateFormat.value().trim();
			}
		}
		this.modelInfo		= modelInfo;
		this.field 			= field;
		this.lazyObject		= lazyObject;
		this.lazyCollection	= lazyCollection;
		this.setter 		= setter;
		this.getter 		= getter;
		this.column 		= column;
		this.format			= format;
	}

	public ModelInfo getModelInfo() {
		return modelInfo;
	}

	public String getColumn() {
		return column;
	}
	
	public String getName() {
		return field.getName();
	}
	
	public Class<?> getType() {
		return field.getType();
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return field.getAnnotation(annotationClass);
	}
	
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return field.getAnnotationsByType(annotationClass);
	}
	
	public Annotation[] getAnnotations() {
		return field.getAnnotations();
	}

	public TypeConverter getConverter() {
		return typeConverter;
	}	

	public void setConverter(TypeConverter typeConverter) {
		this.typeConverter = typeConverter;
	}

	public boolean isLazyObject() {
		return lazyObject;
	}

	public boolean isLazyCollection() {
		return lazyCollection;
	}

	public void setValue(Object target, Object value) throws Exception {
		if (setter != null) {
			setter.invoke(target, value);
		} else {
			field.set(target, value);
		}
	}
	
	public Object getValue(Object target) throws Exception {
		if (getter != null) {
			return getter.invoke(target);
		} else {
			return field.get(target);
		}
	}
	
	public <T> T convert(String value) {
		if (typeConverter == null) {
			return null;
		}
		try {
			return typeConverter.getAction().convert(getType(), value, format);
		} catch (Exception e) {
			throw new RuntimeException(e.getCause() != null ? e.getCause() : e);
		}
	}	
	
}
