package insynctive.support.dao;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import insynctive.support.model.PerformanceConfiguration;
import insynctive.support.model.RunID;

@Repository
@Transactional
public class PerformanceConfigurationDao {

	private final SessionFactory sessionFactory;
	
	@Inject
	public PerformanceConfigurationDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private Session openSession() {
		return sessionFactory.getCurrentSession();
	}
	
	private Long save(PerformanceConfiguration config){
		return (Long) openSession().save(config);
	}
	
	private void saveOrUpdate(PerformanceConfiguration config){
		openSession().saveOrUpdate(config);
	}
	
	private void update(PerformanceConfiguration config){
		openSession().update(config);
	}
	
	public PerformanceConfiguration get(){
		return (PerformanceConfiguration)openSession().get(PerformanceConfiguration.class, 0);
	}
	
	public PerformanceConfiguration init(){
		PerformanceConfiguration performanceConfiguration = new PerformanceConfiguration();
		performanceConfiguration.setId(new Long(0));
		saveOrUpdate(performanceConfiguration);
		return performanceConfiguration;
	}
	
	public PerformanceConfiguration setScheduleEnabled(){
		PerformanceConfiguration config = (PerformanceConfiguration)openSession().get(PerformanceConfiguration.class, 0);
		config.setScheduleEnabled(true);
		update(config);
		return config;
	}
	
	public PerformanceConfiguration setScheduleDisabled(){
		PerformanceConfiguration config = (PerformanceConfiguration)openSession().get(PerformanceConfiguration.class, 0);
		config.setScheduleEnabled(false);
		update(config);
		return config;
	}
	
}
