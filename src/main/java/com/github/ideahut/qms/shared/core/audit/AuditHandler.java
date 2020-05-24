package com.github.ideahut.qms.shared.core.audit;

public interface AuditHandler {

	public void doAudit(String action, Object object);
	
}
