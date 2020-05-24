package com.github.ideahut.qms.shared.core.audit;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;

import com.github.ideahut.qms.shared.client.type.IdType;
import com.github.ideahut.qms.shared.core.annotation.Auditable;
import com.github.ideahut.qms.shared.core.bean.InitializationBean;
import com.github.ideahut.qms.shared.core.context.RequestContext;
import com.github.ideahut.qms.shared.core.converter.TypeConverter;
import com.github.ideahut.qms.shared.core.hibernate.MetadataIntegrator;
import com.github.ideahut.qms.shared.core.model.FieldInfo;
import com.github.ideahut.qms.shared.core.model.IdInfo;
import com.github.ideahut.qms.shared.core.model.ModelInfo;
import com.github.ideahut.qms.shared.core.model.ModelManager;
import com.github.ideahut.qms.shared.core.model.entity.BaseModel;
import com.github.ideahut.qms.shared.core.task.TaskHandler;

public class ModelAuditHandler implements AuditHandler, InitializationBean {
	
	private final Map<Class<?>, TableAccessible> tableAccessibles = new HashMap<Class<?>, TableAccessible>();
	
	private boolean initialized = false;	
	private ModelManager modelManager;	
	private TaskHandler taskHandler;	
	private AuditProperties properties;
	
	public void setModelManager(ModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public void setTaskHandler(TaskHandler taskHandler) {
		this.taskHandler = taskHandler;
	}
	
	public void setProperties(AuditProperties properties) {
		this.properties = properties;
	}

	public ModelAuditHandler() {}
	
	public ModelAuditHandler(AuditProperties properties) {
		setProperties(properties);
	}
		

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void doInitialization() throws Exception {
		if (modelManager == null) {
			throw new Exception("modelManager is required");
		}
		if (taskHandler == null) {
			throw new Exception("taskHandler is required");
		}
		if (properties == null) {
			properties = new AuditProperties();
		}
		tableAccessibles.clear();
		MetadataIntegrator integrator = modelManager.getMetadataIntegrator();
		Metadata metadata = integrator.getMetadata();
		Database database = metadata.getDatabase();
		@SuppressWarnings("deprecation")
		Dialect dialect = integrator.getSessionFactory().getDialect();
		
		AuditProperties.Model model = properties.model;
		
		AuditProperties.Model.Table mtable = model.table;
		String tblPrefix = mtable.prefix.orElse("").trim();
		String tblSuffix = mtable.suffix.orElse("").trim();
		if (tblPrefix.isEmpty() && tblSuffix.isEmpty()) {
			tblSuffix = "_audit";
		}
		
		AuditProperties.Model.Generate generate = model.generate;
		Integer maxPrecision = generate.maxPrecision.orElse(new Integer(1000));
		Integer maxScale = generate.maxScale.orElse(new Integer(100));
		boolean isGenerateTable = generate.table.orElse(Boolean.TRUE).booleanValue();
		
		AuditProperties.Model.Enable enable = model.enable;
		boolean isEnableRowId = enable.rowid.orElse(Boolean.TRUE).booleanValue();
		boolean isEnableIndex = enable.index.orElse(Boolean.TRUE).booleanValue();
		
		AuditProperties.Model.Column mcolumn = model.column;
		AuditColumn auditColumn = new AuditColumn();
		auditColumn.auditor = mcolumn.auditor.orElse("_auditor_");
		auditColumn.action = mcolumn.action.orElse("_action_");
		auditColumn.info = mcolumn.info.orElse("_info_");
		auditColumn.entry = mcolumn.entry.orElse("_entry_");
		
		for (Namespace namespace : database.getNamespaces()) {
		    for (Table table : namespace.getTables()) {
		    	String tname = (table.getSchema() != null ? table.getSchema() : "") + "_" + table.getName();
		    	ModelInfo modelInfo = modelManager.getTableModelInfo(tname);
		    	if (modelInfo == null) {
		    		continue;
		    	}
		    	Auditable modelAuditable = modelInfo.getAnnotation(Auditable.class);
		    	if (modelAuditable == null || modelAuditable.value() == false) {
		    		continue;
		    	}
		    	
		    	IdInfo idInfo = modelInfo.getIdInfo();
		    	
		    	Table newTable = new Table();
		    	newTable.setName(tblPrefix + table.getName() + tblSuffix);
		    	newTable.setSchema(table.getSchema());
		    	newTable.setCatalog(table.getCatalog());
		    	newTable.setComment(table.getComment());
		    	newTable.setAbstract(table.isAbstract());								
		    	newTable.setSubselect(table.getSubselect());		    	
		    	
		    	List<FieldInfo> parameters = new ArrayList<FieldInfo>();
		    	
		    	StringBuilder sqlInsert = new StringBuilder("insert into ")
		    	.append(newTable.getQualifiedTableName()).append("(");
		    	
		    	String catalog = newTable.getCatalog();
				String schema = newTable.getSchema();
		    	Iterator<Column> iterator = table.getColumnIterator();
		    	int countparam = 0;
		    	while (iterator.hasNext()) {
		    		Column column = iterator.next();
		    		FieldInfo fieldInfo = modelInfo.getColumnInfo(column.getName());
					if (fieldInfo == null && IdType.embedded.equals(idInfo.getType())) {
						fieldInfo = idInfo.getEmbeddedIdInfo().getColumnInfo(column.getName());
					}
					Auditable fieldAuditable = fieldInfo != null ? fieldInfo.getAnnotation(Auditable.class) : null;
					if (fieldAuditable != null && fieldAuditable.value() == false) {
						continue;
					}					
		    		Column newColumn = column.clone();
					if (newColumn.getPrecision() > maxPrecision) {
						newColumn.setPrecision(maxPrecision); // ERROR: NUMERIC precision 131089 must be between 1 and 1000
					}
					if (newColumn.getScale() > maxScale) {
						newColumn.setScale(maxScale);
					}
		    		newColumn.setUnique(false); // set selalu false
					newTable.addColumn(newColumn);
					sqlInsert.append(newColumn.getQuotedName(dialect)).append(",");
					parameters.add(fieldInfo);
					countparam++;
				}
		    	// Tambah column audit
				countparam += addAuditTableColumn(integrator, newTable, auditColumn, sqlInsert);
				
				sqlInsert.delete(sqlInsert.length() - 1, sqlInsert.length()).append(") values (");
				for (int i = 0; i < countparam; i++) {
					sqlInsert.append("?,");
				}
				sqlInsert.delete(sqlInsert.length() - 1, sqlInsert.length()).append(")");
				
				if (isEnableRowId) {
					newTable.setRowId(table.getRowId());
				}
				List<String> sqlIndex = new ArrayList<String>();
		    	if (isEnableIndex) {
					Iterator<Index> iterIndex = table.getIndexIterator();
					while (iterIndex.hasNext()) {
						Index index = iterIndex.next();
						Index newIndex = new Index();
						newIndex.setName(tblPrefix + index.getName() + tblSuffix);
						newIndex.setTable(newTable);
						Iterator<Column> iterColumn = index.getColumnIterator();
						while (iterColumn.hasNext()) {
							Column column = iterColumn.next();
							Column newColumn = column.clone();
							newColumn.setUnique(false);
							newIndex.addColumn(newColumn);
						}
						newTable.addIndex(newIndex);
						String sql = newIndex.sqlCreateString(dialect, integrator.getMetadata(), catalog, schema);
						sqlIndex.add(sql);
					}
				}
		    	
		    	TableAccessible tableAccessible = new TableAccessible();
		    	tableAccessible.parameters = parameters;
		    	tableAccessible.sqlInsert = sqlInsert.toString();
		    	tableAccessibles.put(modelInfo.getModelClass(), tableAccessible);
		    	
		    	if (Boolean.TRUE.equals(isGenerateTable)) {
			    	boolean isTableExist = isTableExist(newTable);
			    	if (!isTableExist) {
				    	Session session = null;
				    	try {
				    		String sqlCreate = newTable.sqlCreateString(dialect, integrator.getMetadata(), catalog, schema);
				    		session = integrator.getSessionFactory().openSession();			    		
							session.beginTransaction();
							NativeQuery query = session.createNativeQuery(sqlCreate);
							query.executeUpdate();
							for (String sql : sqlIndex) {
								query = session.createNativeQuery(sql);
								query.executeUpdate();
							}
							session.getTransaction().commit();
						} catch (Exception e) {
							if (session != null) {
								session.getTransaction().rollback();
							}
							throw e;
						} finally {
							try { session.close(); } catch (Exception e) {}
						}
			    	}
		    	}
		    }
		}
		initialized = true;
	}
	
	
	@Override
	public void doAudit(String action, Object object) {
		initialized();
		if (!(object instanceof BaseModel)) {
			return;
		}
		BaseModel entity = (BaseModel)object;
		Auditable auditable = entity.getClass().getAnnotation(Auditable.class);
		if (auditable == null || auditable.value() == false) {
			return;
		}
		
		String auditor = null;
		String info = null;
		AuditInfo auditInfo = RequestContext.currentContext().getAttribute(AuditInfo.CONTEXT_ATTRIBUTE);
		if (auditInfo != null) {
			auditor = auditInfo.getAuditor() != null ? new String(auditInfo.getAuditor()) : null;
			info = auditInfo.getInfo() != null ? new String(auditInfo.getInfo()) : null;
		}
				
		final String fauditor = auditor;
		final String finfo = info;
		taskHandler.execute(new Runnable() {
			@SuppressWarnings("rawtypes")
			@Override
			public void run() {
				TableAccessible tableAccessible = tableAccessibles.get(entity.getClass());
				if (tableAccessible != null) {
					Session session = null;
					try {
						List<Object> parameters = new ArrayList<Object>();
						for (int i = 0; i < tableAccessible.parameters.size(); i++) {
							FieldInfo fieldInfo = tableAccessible.parameters.get(i);
							
							Object value;
							ModelInfo modelInfo = fieldInfo.getModelInfo();
							
							ModelInfo parentInfo = modelInfo != null ? modelInfo.getParentInfo() : null;
							if (parentInfo != null) {
								Object idvalue = parentInfo.getFieldInfo(parentInfo.getIdInfo().getFields().iterator().next()).getValue(entity);
								value = fieldInfo.getValue(idvalue);								
							} else {
								value = fieldInfo.getValue(entity);								
							}
							if (value != null) {
								if (TypeConverter.MODEL.equals(fieldInfo.getConverter())) {
									ModelInfo valModelInfo = modelManager.getModelInfo(fieldInfo.getType());
									if (valModelInfo != null) {
										value = valModelInfo.getFieldInfo(valModelInfo.getIdInfo().getFields().iterator().next()).getValue(value);
									}
									else {
										value = null;
									}
								}
								parameters.add(value);
							} else {
								parameters.add(null);
							}
						}
						parameters.add(fauditor);
						parameters.add(action);
						parameters.add(finfo);
						parameters.add(new Date());
						
			    		session = modelManager.getMetadataIntegrator().getSessionFactory().openSession();			    		
						session.beginTransaction();
						NativeQuery query = session.createNativeQuery(tableAccessible.sqlInsert);
						for (int i = 0; i < parameters.size(); i++) {
							query.setParameter(i + 1, parameters.get(i));
						}
						query.executeUpdate();					
						session.getTransaction().commit();
					} catch (Exception e) {
						if (session != null) {
							session.getTransaction().rollback();
						}
						throw new RuntimeException(e);
					} finally {
						try { session.close(); } catch (Exception e) {}
					}
				}
			}
		});
	}	
	
	
	private void initialized() {
		if (!initialized) {			
			throw new RuntimeException("Model audit handler not initialized; call doInitialization() before using it");
		}
	}
	
	private int addAuditTableColumn(MetadataIntegrator integrator, Table table, AuditColumn auditColumn, StringBuilder sqlInsert) {
		try {
			@SuppressWarnings("deprecation")
			Dialect dialect = integrator.getSessionFactory().getDialect();
			Field typeField = SimpleValue.class.getDeclaredField("type");
			typeField.setAccessible(true);
			
			@SuppressWarnings("deprecation")
			SimpleValue stringValue = new SimpleValue((MetadataImplementor)integrator.getMetadata(), table);
			stringValue.setTypeName(String.class.getName());
			typeField.set(stringValue, StringType.INSTANCE);
			
			Column auditorIdColumn = new Column();
			auditorIdColumn.setName(auditColumn.auditor);
			auditorIdColumn.setLength(255);
			auditorIdColumn.setScale(2);
			auditorIdColumn.setValue(stringValue);
			auditorIdColumn.setTypeIndex(0);
			auditorIdColumn.setNullable(true);
			auditorIdColumn.setPrecision(19);
			auditorIdColumn.setUnique(false);
			table.addColumn(auditorIdColumn);
			sqlInsert.append(auditorIdColumn.getQuotedName(dialect)).append(",");
			
			Column actionColumn = new Column();
			actionColumn.setName(auditColumn.action);
			actionColumn.setLength(255);
			actionColumn.setScale(2);
			actionColumn.setValue(stringValue);
			actionColumn.setTypeIndex(0);
			actionColumn.setNullable(true);
			actionColumn.setPrecision(19);
			actionColumn.setUnique(false);
			table.addColumn(actionColumn);
			sqlInsert.append(actionColumn.getQuotedName(dialect)).append(",");
			
			Column infoColumn = new Column();
			infoColumn.setName(auditColumn.info);
			infoColumn.setLength(255);
			infoColumn.setScale(2);
			infoColumn.setValue(stringValue);
			infoColumn.setTypeIndex(0);
			infoColumn.setNullable(true);
			infoColumn.setPrecision(19);
			infoColumn.setUnique(false);
			table.addColumn(infoColumn);
			sqlInsert.append(infoColumn.getQuotedName(dialect)).append(",");
			
			@SuppressWarnings("deprecation")
			SimpleValue timestampValue = new SimpleValue((MetadataImplementor)integrator.getMetadata(), table);
			timestampValue.setTypeName("timestamp");
			typeField.set(timestampValue, TimestampType.INSTANCE);		
			
			Column entryColumn = new Column();
			entryColumn.setName(auditColumn.entry);
			entryColumn.setLength(255);
			entryColumn.setScale(2);
			entryColumn.setValue(timestampValue);
			entryColumn.setTypeIndex(0);
			entryColumn.setNullable(false);
			entryColumn.setPrecision(19);
			entryColumn.setUnique(false);
			table.addColumn(entryColumn);
			sqlInsert.append(entryColumn.getQuotedName(dialect)).append(",");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return 4;
	}
	
	private boolean isTableExist(Table table) {
		Connection connection = null;
		try {
			connection = modelManager.getMetadataIntegrator().getConnection();
			DatabaseMetaData dbMetaData = connection.getMetaData();
			ResultSet rs = dbMetaData.getTables(
				table.getCatalog(), 
				table.getSchema(), 
				table.getName(), 
				new String[] {"TABLE"}
			);
			boolean result = rs.next();
			rs.close();
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try { connection.close(); } catch (Exception e) {}
		}
	}
	
	private class TableAccessible {
		private String sqlInsert;
		private List<FieldInfo> parameters;
	}
	
	private class AuditColumn {
		private String auditor;
		private String action;
		private String info;
		private String entry;
	}

}
