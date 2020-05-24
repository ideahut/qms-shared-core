package com.github.ideahut.qms.shared.core.grid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ideahut.qms.shared.client.type.GridMatrixType;
import com.github.ideahut.qms.shared.core.bean.GridBean;

@Target({ 
	ElementType.TYPE 
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GridAttribute {
	
	public String name();
	
	public String title() default "";
	
	public String admin() default ""; // untuk akses ke resource admin (common)
	
	public String path() default ""; // untuk akses ke resource model (standar)
	
	public GridTable[] table() default {}; 	 // custom grid
	
	public GridFilter[] filters() default {}; // custom grid
	public GridMatrixType filterMatrixType() default GridMatrixType.single;
	public int filterMatrixNum() default 0;
	
	public GridOrder[] orders() default {};
	public GridMatrixType orderMatrixType() default GridMatrixType.single;
	public int orderMatrixNum() default 0;
	
	public Class<? extends GridBean>[] customClasses() default {}; // daftar class grid custom yang menggunakan entity yang sama
	public String[] stringCustomClasses() default {}; // daftar nama class grid custom yang menggunakan entity yang sama, pakai string jika beda jar antara model dengan classCustom
	
}
