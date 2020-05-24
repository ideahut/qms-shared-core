package com.github.ideahut.qms.shared.core.model.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BaseSoftDeleteModel extends BaseModel implements SoftDeleteModel {
	
	@Column(name = SoftDeleteModel.COLUMN_NAME, nullable = false)
	private Character isDeleteFlag = SoftDeleteModel.FLAG_NO;
	
	public Character getIsDeleteFlag() {
		return isDeleteFlag;
	}

	@Override
	public void setIsDeleteFlag(Character isDeleteFlag) {
		this.isDeleteFlag = isDeleteFlag;
	}
	
}
