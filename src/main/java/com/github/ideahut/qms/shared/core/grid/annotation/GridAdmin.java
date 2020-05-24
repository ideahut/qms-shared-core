package com.github.ideahut.qms.shared.core.grid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ 
	ElementType.ANNOTATION_TYPE 
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GridAdmin {
	
	public String name();
	
	public int limit() default 0;
	
	public String[] fields() default {};
	
	public String pattern() default ""; // untuk mengenerate label berdasarkan fields (Index 0 => value di select), contoh pattern: {1} ({2})
	
	public GridOrder[] orders() default {};
	
	public GridFilter[] filters() default {};
	
}
