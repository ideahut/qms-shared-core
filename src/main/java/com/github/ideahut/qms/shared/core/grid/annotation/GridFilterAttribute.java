package com.github.ideahut.qms.shared.core.grid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ideahut.qms.shared.client.type.GridFilterType;

@Target({ 
	ElementType.ANNOTATION_TYPE, 
	ElementType.FIELD 
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GridFilterAttribute {

	public GridFilterType type() default GridFilterType.single;
	
	public GridFilter[] filters() default {};
	
}
