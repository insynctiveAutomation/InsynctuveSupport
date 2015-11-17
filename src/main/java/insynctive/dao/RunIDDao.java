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
		RunID runID = (RunID) openSession().createQuery("from RunID order by runID DESC").setMaxResults(1).list().get(0);
		RunID newRunID = new RunID();
		newRunID.setRunID(runID.getRunID()+1);
		openSession().save(newRunID);
		return newRunID.getRunID();
	}
	
}
