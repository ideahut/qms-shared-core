package com.github.ideahut.qms.shared.core.support;

import com.github.ideahut.qms.shared.client.exception.ResultRuntimeException;
import com.github.ideahut.qms.shared.client.object.Result;

public final class ResultAssert {
	
	private ResultAssert() {}

	public static void notNull(Result result, Object value) {
		if (value == null) {
			throw new ResultRuntimeException(result);
		}
	}
	
	public static void notNullAll(Result result, Object...values) {
		for (Object value : values) {
			if (value == null) {
				throw new ResultRuntimeException(result);
			}
		}
	}	
	
	public static void notEmpty(Result result, String value) {
		if (value == null || value.isEmpty()) {
			throw new ResultRuntimeException(result);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void notLessThan(Result result, Object value1, Object value2) {
		Class<?>[] types = getTypes(value1, value2);
		if (types[0].isPrimitive()) {
			if (int.class.isAssignableFrom(types[0])) {
				if ((int)value1 < (int)value2) {
					throw new ResultRuntimeException(result);
				}
			} else if (float.class.isAssignableFrom(types[0])) {
				if ((float)value1 < (float)value2) {
					throw new ResultRuntimeException(result);
				}
			} else if (double.class.isAssignableFrom(types[0])) {
				if ((double)value1 < (double)value2) {
					throw new ResultRuntimeException(result);
				}
			}
		} 
		else if (Comparable.class.isAssignableFrom(types[0])) {
			Comparable c1 = (Comparable)value1;
			if (c1.compareTo(value2) == 1) {
				throw new ResultRuntimeException(result);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void notLessOrEqualThan(Result result, Object value1, Object value2) {
		Class<?>[] types = getTypes(value1, value2);
		if (types[0].isPrimitive()) {
			if (int.class.isAssignableFrom(types[0])) {
				if ((int)value1 <= (int)value2) {
					throw new ResultRuntimeException(result);
				}
			} else if (float.class.isAssignableFrom(types[0])) {
				if ((float)value1 <= (float)value2) {
					throw new ResultRuntimeException(result);
				}
			} else if (double.class.isAssignableFrom(types[0])) {
				if ((double)value1 <= (double)value2) {
					throw new ResultRuntimeException(result);
				}
			}
		} 
		else if (Comparable.class.isAssignableFrom(types[0])) {
			Comparable c1 = (Comparable)value1;
			if (c1.compareTo(value2) == 1 || c1.compareTo(value2) == 0) {
				throw new ResultRuntimeException(result);
			}
		}
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void notGreaterThan(Result result, Object value1, Object value2) {
		Class<?>[] types = getTypes(value1, value2);
		if (types[0].isPrimitive()) {
			if (int.class.isAssignableFrom(types[0])) {
				if ((int)value1 > (int)value2) {
					throw new ResultRuntimeException(result);
				}
			} else if (float.class.isAssignableFrom(types[0])) {
				if ((float)value1 > (float)value2) {
					throw new ResultRuntimeException(result);
				}
			} else if (double.class.isAssignableFrom(types[0])) {
				if ((double)value1 > (double)value2) {
					throw new ResultRuntimeException(result);
				}
			}
		} 
		else if (Comparable.class.isAssignableFrom(types[0])) {
			Comparable c1 = (Comparable)value1;
			if (c1.compareTo(value2) == -1) {
				throw new ResultRuntimeException(result);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void notGreaterOrEqualThan(Result result, Object value1, Object value2) {
		Class<?>[] types = getTypes(value1, value2);
		if (types[0].isPrimitive()) {
			if (int.class.isAssignableFrom(types[0])) {
				if ((int)value1 >= (int)value2) {
					throw new ResultRuntimeException(result);
				}
			} else if (float.class.isAssignableFrom(types[0])) {
				if ((float)value1 >= (float)value2) {
					throw new ResultRuntimeException(result);
				}
			} else if (double.class.isAssignableFrom(types[0])) {
				if ((double)value1 >= (double)value2) {
					throw new ResultRuntimeException(result);
				}
			}
		} 
		else if (Comparable.class.isAssignableFrom(types[0])) {
			Comparable c1 = (Comparable)value1;
			if (c1.compareTo(value2) == -1 || c1.compareTo(value2) == 0) {
				throw new ResultRuntimeException(result);
			}
		}
	}
	
	
	
	private static Class<?>[] getTypes(Object value1, Object value2) {
		if (value1 == null || value2 == null) {
			throw new NullPointerException("value1=" + value1 + ", value2=" + value2);
		}
		Class<?> type1 = value1.getClass(), type2 = value2.getClass();
		if (!type1.equals(type2)) {
			throw new IllegalArgumentException("Invalid value type");
		}
		return new Class<?>[] { type1, type2 };
	}
	
}
