package com.github.ideahut.qms.shared.core.grid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ideahut.qms.shared.client.type.HorizontalAlignType;

@Target({ 
	ElementType.ANNOTATION_TYPE 
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GridAlign {
	
	public HorizontalAlignType header() default HorizontalAlignType.left;
	
	public HorizontalAlignType column()  default HorizontalAlignType.left;
	
}
