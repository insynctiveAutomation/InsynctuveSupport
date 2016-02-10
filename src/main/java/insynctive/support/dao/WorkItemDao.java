package insynctive.support.dao;

import java.math.BigInteger;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import insynctive.support.model.VisualStudioWorkItemEntity;

@Repository
@Transactional
public class WorkItemDao {

	private final SessionFactory sessionFactory;
	
	@Inject
	public WorkItemDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private Session openSession() {
		return sessionFactory.getCurrentSession();
	}
	
    public Integer save(BigInteger workItemID){
		return (Integer) openSession().save(new VisualStudioWorkItemEntity(workItemID));
	}
	
    public BigInteger save(String workItemID){
		return (BigInteger) openSession().save(new VisualStudioWorkItemEntity(BigInteger.valueOf(Integer.valueOf(workItemID))));
	}
    
    public VisualStudioWorkItemEntity getByEntityID(String entityID){
		List<VisualStudioWorkItemEntity> entities = (List<VisualStudioWorkItemEntity>) openSession().createCriteria(VisualStudioWorkItemEntity.class).add(Restrictions.eq("workItemID", BigInteger.valueOf(Integer.valueOf(entityID)))).list();
		
		if(entities.size() > 0){
			return entities.get(0);
		} else {
			return null;
		}
    }
    
    public VisualStudioWorkItemEntity getByEntityID(BigInteger entityID){
		List<VisualStudioWorkItemEntity> entities = (List<VisualStudioWorkItemEntity>) openSession().createCriteria(VisualStudioWorkItemEntity.class).add(Restrictions.eq("workItemID", entityID)).list();
		
		if(entities.size() > 0){
			return entities.get(0);
		} else {
			return null;
		}
    }

	public void save(VisualStudioWorkItemEntity dbBug) {
		 openSession().save(dbBug);
	}

	public void saveOrUpdate(VisualStudioWorkItemEntity dbBug) {
		 openSession().saveOrUpdate(dbBug);
	}
}
