package com.github.ideahut.qms.shared.core.grid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ideahut.qms.shared.client.type.GridControlType;
import com.github.ideahut.qms.shared.client.type.HorizontalAlignType;
import com.github.ideahut.qms.shared.core.annotation.Accessible;

@Target({ 
	ElementType.ANNOTATION_TYPE, 
	ElementType.FIELD 
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GridColumn {
	
	// Column bisa di-order atau tidak 
	public GridOrder[] order() default {};
	
	// Column bisa di filter atau tidak
	public GridFilter[] filter() default {};
	
	// Column yang bisa ditampilkan di table
	public GridHeader[] header() default {};
	
	
	
	public String label();
	
	public GridControlType controlType();
	
	public String name() default "";
	
	public String metadata() default ""; // dipakai jika controlType = grid
	
	public HorizontalAlignType align() default HorizontalAlignType.left;
	
	public boolean editable() default true;
	
	public boolean insertable() default true;
	
	public boolean visible() default true;
	
	public boolean readOnly() default false;
	
	public boolean required() default true;
	
	public String defaultValue() default "";
	
	public int sort() default 0;
	
	public Accessible[] options() default {};
	
	public GridAdmin[] admin() default {};
	
	public GridAdmin[] recall() default {}; // untuk memanggil single object, dan hanya id yang ditampilkan (kasus jika pakai @JsonIgnore) 
	
	public String viewColumn() default ""; // view column, untuk view column dengan pattern, cth: ${user.name} - ${user.id}
	
	public GridValidation[] validations() default {};
	
}
