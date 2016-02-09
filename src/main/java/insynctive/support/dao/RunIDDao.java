package insynctive.support.dao;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import insynctive.support.model.RunID;

@Repository
@Transactional
public class RunIDDao {

	private final SessionFactory sessionFactory;
	
	@Inject
	public RunIDDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private Session openSession() {
		return sessionFactory.getCurrentSession();
	}
	
	private Integer save(RunID runID){
		return (Integer) openSession().save(runID);
	}
	
	public synchronized Integer getNextRunID(){
		Integer id = save(new RunID());
		double runID = ((double) id-2)/10;
		return (int) runID;
	}
	
}
