package com.github.ideahut.qms.shared.core.model.entity;

public interface SoftDeleteModel {
	
	public static final ThreadLocal<Integer> ACTION = new InheritableThreadLocal<Integer>();
	public static final Integer DELETE		= 0;
	public static final Integer RECREATE 	= 1;
	
	public static final Character FLAG_YES 	= 'Y';
	public static final Character FLAG_NO 	= 'N';	
		
	public static final String FIELD_NAME = "isDeleteFlag";	
	public static final String COLUMN_NAME = "is_delete_flag";
	
	public void setIsDeleteFlag(Character isDeleteFlag);	
	public Character getIsDeleteFlag();
	
}
