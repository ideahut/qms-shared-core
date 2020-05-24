package com.github.ideahut.qms.shared.core.model.entity;

import java.io.Serializable;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import com.github.ideahut.qms.shared.core.model.ModelListener;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@SuppressWarnings("serial")
@EntityListeners(ModelListener.class)
@MappedSuperclass
public abstract class BaseModel extends PanacheEntityBase implements Serializable {

}
