package insynctive.dao;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import insynctive.model.RunID;

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
	
	public Integer getNextRunID(){
		RunID runID = (RunID) openSession().createQuery("from run_id order by run_id DESC LIMIT 1").list().get(0);
		runID.setId(null);
		runID.setRunID(runID.getRunID()+1);
		openSession().save(runID);
		return runID.getRunID();
	}
	
}
