package com.github.ideahut.qms.shared.core.audit;

public class AuditInfo {
	
	public static final String CONTEXT_ATTRIBUTE = AuditInfo.class.getName();	

	private String auditor;	
	private String info;

	public AuditInfo() {}
	
	public AuditInfo(String auditor) {
		this(auditor, null);
	}
	
	public AuditInfo(String auditor, String info) {
		this.auditor = auditor;
		this.info = info;
	}
	
	public String getAuditor() {
		return auditor;
	}

	public AuditInfo setAuditor(String auditor) {
		this.auditor = auditor;
		return this;
	}

	public String getInfo() {
		return info;
	}

	public AuditInfo setInfo(String info) {
		this.info = info;
		return this;
	}
	
}
