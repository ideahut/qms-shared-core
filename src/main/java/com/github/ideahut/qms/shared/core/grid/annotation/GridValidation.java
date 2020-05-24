package com.github.ideahut.qms.shared.core.grid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ideahut.qms.shared.client.type.GridValidationType;

@Target({ 
	ElementType.ANNOTATION_TYPE 
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GridValidation {
	
	public GridValidationType type() default GridValidationType.none;
	
	public String format() default "";
	
	public int minLength() default 0;
	
	public int maxLength() default 0;
	
	public int rangeLength() default 0;
	
	public String minValue() default "";
	
	public String maxValue() default "";
	
	public String rangeValue() default "";
	
}
