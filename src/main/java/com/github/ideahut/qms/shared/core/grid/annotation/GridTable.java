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
public @interface GridTable {
	
	public boolean multiselect() default false; // untuk select all
	
	public boolean footer() default false; // tampilkan footer
	
	public GridHeader[] headers() default {}; // dipakai di grid custom	
	
}
