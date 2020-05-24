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
public @interface GridHeader {

	public String label() default "";  // custom grid
	
	public String column() default ""; // dipakai jika buat grid di luar class entity (custom grid)
	
	public int sort() default 100;
	
	public HorizontalAlignType titleAlign() default HorizontalAlignType.left;
	
	public HorizontalAlignType contentAlign() default HorizontalAlignType.left;
	
}
