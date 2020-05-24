package com.github.ideahut.qms.shared.core.model;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.ideahut.qms.shared.core.context.RequestContext;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@SuppressWarnings("serial")
public class ModelIntrospector extends JacksonAnnotationIntrospector {

	@Override
	public boolean hasIgnoreMarker(AnnotatedMember m) {
		if (PanacheEntityBase.class.isAssignableFrom(m.getDeclaringClass()) && "isPersistent".equals(m.getName())) {
			return true;
		} else {
			ModelIgnoreMember member = RequestContext.currentContext().getAttribute(ModelIgnoreMember.CONTEXT_ATTRIBUTE);
			if (member != null) {
				if (member.isIgnored(m.getDeclaringClass(), m.getName())) {
					return true;
				}
			}
		}
		return super.hasIgnoreMarker(m);
	}	
	
}
