package com.github.ideahut.qms.shared.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ 
	ElementType.TYPE,
	ElementType.CONSTRUCTOR,
	ElementType.METHOD,
	ElementType.FIELD,
	ElementType.ANNOTATION_TYPE,
	ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Accessible {

	public Class<?> target() default Void.class;
	
	public String targetAsString() default "";
	
	public String method() default "";
	
	public String field() default "";
	
	public boolean isStatic() default false;
	
}
