package com.github.ideahut.qms.shared.core.converter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;

@SuppressWarnings("unchecked")
public enum TypeConverter {
	STRING (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String value = args.length != 0 ? args[0] : null;
				return (T)value;
			}			
		}
	),
	INT (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim() : "";
				Object value;
				if (input.isEmpty()) {
					value = 0;
				} else {
					value = Integer.parseInt(input);
				}
				return (T)value;
			}			
		}
	),
	LONG (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim() : "";
				Object value;
				if (input.isEmpty()) {
					value = 0l;
				} else {
					value = Long.parseLong(input);
				}
				return (T)value;
			}			
		}
	),
	BOOL (
		new Action() {
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim().toLowerCase() : "";
				Object value = null;
				if (input.isEmpty()) {
					value = true;
				} else {
					value = !"0".equals(input) && !"false".equals(input);
				}
				return (T)value;
			}			
		}
	),
	FLOAT (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim() : "";
				Object value;
				if (input.isEmpty()) {
					value = 0f;
				} else {
					value = Float.parseFloat(input);
				}
				return (T)value;
			}			
		}
	),
	DOUBLE (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim() : "";
				Object value;
				if (input.isEmpty()) {
					value = 0d;
				} else {
					value = Double.parseDouble(input);
				}
				return (T)value;
			}			
		}
	),
	NUMBER (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim() : "";
				Object value;
				if (input.isEmpty()) {
					value = null;
				} else {
					if (!Number.class.isAssignableFrom(type)) {
						throw new Exception("Invalid class");
					}
					value = type.getConstructor(String.class).newInstance(input);
				}
				return (T)value;
			}			
		}
	),
	BOOLEAN (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim().toLowerCase() : "";
				Object value;
				if (input.isEmpty()) {
					value = null;
				} else {
					value = new Boolean(!"0".equals(input) && !"false".equals(input));
				}
				return (T)value;
			}			
		}
	),
	DATE (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim() : "";
				Object value;
				if (input.isEmpty()) {
					value = null;
				} else {
					SimpleDateFormat dateFormat = DATE_FORMAT;
					if (args.length > 1) {
						dateFormat = new SimpleDateFormat(args[1]);
					}
					value = dateFormat.parse(input);
				}
				return (T)value;
			}			
		}
	),
	LOCALDATE(
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim() : "";
				Object value;
				if (input.isEmpty()) {
					value = null;
				} else {
					DateTimeFormatter formatter = LOCALDATE_FORMATTER;
					if (args.length > 1) {
						formatter = DateTimeFormatter.ofPattern(args[1]);
					}
					value = LocalDate.parse(input, formatter);
				}
				return (T)value;
			}			
		}
	),
	LOCALTIME(
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				String input = args.length != 0 ? args[0].trim() : "";
				Object value;
				if (input.isEmpty()) {
					value = null;
				} else {
					DateTimeFormatter formatter = LOCALTIME_FORMATTER;
					if (args.length > 1) {
						formatter = DateTimeFormatter.ofPattern(args[1]);
					}
					value = LocalTime.parse(input, formatter);
				}
				return (T)value;
			}			
		}
	),
	MODEL (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				return null;
			}			
		}
	),
	COLLECTION (
		new Action() {			
			@Override
			public <T> T convert(Class<?> type, String...args) throws Exception {
				return null;
			}			
		}
	),
	;
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private static final DateTimeFormatter LOCALDATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter LOCALTIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
	
	private final Action action;
	
	TypeConverter(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}
	
	public static TypeConverter getByType(Class<?> type) {
		if (String.class.isAssignableFrom(type)) {
			return TypeConverter.STRING;
		}
		else if (type.isPrimitive()) {
			if (int.class.isAssignableFrom(type)) {
				return TypeConverter.INT;
			}
			else if (float.class.isAssignableFrom(type)) {
				return TypeConverter.FLOAT;
			}
			else if (double.class.isAssignableFrom(type)) {
				return TypeConverter.DOUBLE;
			}
			else if (boolean.class.isAssignableFrom(type)) {
				return TypeConverter.BOOL;
			}
		}
		else if (Number.class.isAssignableFrom(type)) {
			return TypeConverter.NUMBER;
		}
		else if (Date.class.isAssignableFrom(type)) {
			return TypeConverter.DATE;
		}
		else if (LocalDate.class.isAssignableFrom(type)) {
			return TypeConverter.LOCALDATE;
		}
		else if (LocalTime.class.isAssignableFrom(type)) {
			return TypeConverter.LOCALTIME;
		}
		else if (Collection.class.isAssignableFrom(type)) {
			return TypeConverter.COLLECTION;
		}
		return null;
	}
	
	public interface Action {		
		public <T> T convert(Class<?> type, String...args) throws Exception;		
	}
	
}
