package com.github.ideahut.qms.shared.core.mapper;

import java.io.InputStream;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface DataMapper {
	
	public <T> T readData(byte[] data, Class<T> type);
	
	public <T> T readData(String data, Class<T> type);
	
	public <T> T readData(InputStream stream, Class<T> type);
	
	public byte[] writeJsonAsBytes(Object value);
	
	public String writeJsonAsString(Object value);
	
	public byte[] writeXmlAsBytes(Object value);
	
	public String writeXmlAsString(Object value);
	
	public <T> T convertData(Object value, Class<T> type);
	
	public ObjectNode createObjectNode();
	
	public ArrayNode createArrayNode();
	
}
