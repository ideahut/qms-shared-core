package com.github.ideahut.qms.shared.core.mapper;

import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class DataMapperImpl implements DataMapper {
	
	private final ObjectMapper jsonMapper;
	
	private final XmlMapper xmlMapper;
	
	private AnnotationIntrospector introspector;
	
	private boolean includeNull = false;
	
	public DataMapperImpl() {
		this(false);
	}
	
	public DataMapperImpl(boolean includeNull) {
		jsonMapper = new ObjectMapper();
		jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		
		xmlMapper = new XmlMapper();
		xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		xmlMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);		
		
		setIncludeNull(includeNull);
	}
	
	public AnnotationIntrospector getIntrospector() {
		return introspector;
	}

	public void setIntrospector(AnnotationIntrospector introspector) {
		this.introspector = introspector;
		if (introspector != null) {
			jsonMapper.setAnnotationIntrospector(introspector);
			xmlMapper.setAnnotationIntrospector(introspector);
		}
	}

	public boolean isIncludeNull() {
		return includeNull;
	}

	public void setIncludeNull(boolean includeNull) {
		this.includeNull = includeNull;
		if (!includeNull) {			
			jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);									
		}
	}

	@Override
	public <T> T readData(byte[] data, Class<T> type) {
		try {
			return jsonMapper.readValue(data, type);
		} catch (Exception e) {
			try {
				return xmlMapper.readValue(data, type);
			} catch (Exception e1) {
				throw new RuntimeException("Invalid format");
			}
		}
	}

	@Override
	public <T> T readData(String data, Class<T> type) {
		try {
			return jsonMapper.readValue(data, type);
		} catch (Exception e) {
			try {
				return xmlMapper.readValue(data, type);
			} catch (Exception e1) {
				throw new RuntimeException("Invalid format");
			}
		}
	}

	@Override
	public <T> T readData(InputStream stream, Class<T> type) {
		try {
			return jsonMapper.readValue(stream, type);
		} catch (Exception e) {
			try {
				return xmlMapper.readValue(stream, type);
			} catch (Exception e1) {
				throw new RuntimeException("Invalid format");
			}
		}
	}

	@Override
	public byte[] writeJsonAsBytes(Object value) {
		try {
			return jsonMapper.writeValueAsBytes(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String writeJsonAsString(Object value) {
		try {
			return jsonMapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] writeXmlAsBytes(Object value) {
		try {
			return xmlMapper.writeValueAsBytes(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String writeXmlAsString(Object value) {
		try {
			return xmlMapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T convertData(Object value, Class<T> type) {
		return jsonMapper.convertValue(value, type);
	}

	@Override
	public ObjectNode createObjectNode() {
		return jsonMapper.createObjectNode();
	}

	@Override
	public ArrayNode createArrayNode() {
		return jsonMapper.createArrayNode();
	}

}
