package com.github.ideahut.qms.shared.core.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class AuditModel extends BaseModel {
	
	@Column(name = "created_by")
	private String createdBy;
	
	@Column(name = "created_datetime", nullable = false)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
	@Temporal(TemporalType.DATE)	
	@CreationTimestamp
	private Date createdDatetime;
	
	@Column(name = "updated_by")
	private String updatedBy;
	
	@Column(name = "updated_datetime", nullable = false)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
	@Temporal(TemporalType.DATE)
	@UpdateTimestamp
	private Date updatedDatetime;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getUpdatedDatetime() {
		return updatedDatetime;
	}

	public void setUpdatedDatetime(Date updatedDatetime) {
		this.updatedDatetime = updatedDatetime;
	}
		
}
