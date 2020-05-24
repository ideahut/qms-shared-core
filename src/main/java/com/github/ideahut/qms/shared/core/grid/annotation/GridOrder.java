package com.github.ideahut.qms.shared.core.grid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ideahut.qms.shared.client.type.GridOrderType;

@Target({ 
	ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GridOrder {

	public String label() default ""; // custom label (tidak mengikuti label dari column)
	
	public String column() default ""; // dipakai untuk custom grid (bukan di entity / model)
	
	public int sort() default 0;
 	
	public GridOrderType orderType() default GridOrderType.asc;
	
}
