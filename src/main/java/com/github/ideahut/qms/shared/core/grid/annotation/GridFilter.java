package com.github.ideahut.qms.shared.core.grid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ideahut.qms.shared.client.type.ConditionType;
import com.github.ideahut.qms.shared.client.type.LogicalType;

@Target({ 
	ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GridFilter {

	public String label() default ""; 	// custom grid
	
	public String column() default ""; 	// custom grid
	
	public int sort() default 0; 
	
	public ConditionType condition() default ConditionType.EQUAL;
	
	public LogicalType logical() default LogicalType.and;
	
	public String value() default ""; 	// dipakai di @GridAdmin
	
}
