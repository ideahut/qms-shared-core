package com.github.ideahut.qms.shared.core.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class MetadataIntegrator implements Integrator {
	
	private Metadata metadata;
	
	private SessionFactoryImplementor sessionFactory;
	
	private final ServiceRegistry serviceRegistry;
	
	private final Map<String, Object> settings;
	
	private final MetadataSources metadataSources;
	
	
	public MetadataIntegrator(Map<String, Object> settings) {
		BootstrapServiceRegistry bootstrapServiceRegistry = 
			new BootstrapServiceRegistryBuilder()
			.enableAutoClose()
			.applyIntegrator(this)
			.build();
		StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder(bootstrapServiceRegistry)
			.applySettings(settings)
			.build();		
		this.metadataSources 	= new MetadataSources(serviceRegistry);
		this.serviceRegistry 	= serviceRegistry;
		this.settings			= settings;
	}
	
	public Metadata getMetadata() {
		return metadata;
	}

	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	public Map<String, Object> getSettings() {
		return settings;
	}

	public Collection<Class<?>> getAnnotatedClasses() {
		return metadataSources.getAnnotatedClasses();
	}
	
	public void addAnnotatedClass(Class<?> annotatedClass) {
		metadataSources.addAnnotatedClass(annotatedClass);
	}
	
	public void prepare() {
		metadataSources.buildMetadata().buildSessionFactory();
	}
	
	public void destroy() {
		StandardServiceRegistryBuilder.destroy(this.serviceRegistry);
	}
	
	public Connection getConnection() throws SQLException {
		return sessionFactory
			.getSessionFactoryOptions()
			.getServiceRegistry()
			.getService(ConnectionProvider.class)
			.getConnection();
	}

	@Override
	public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		this.metadata = metadata;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		
	}
	
}
