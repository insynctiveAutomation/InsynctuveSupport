package insynctive.support.dao;

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
	
    public Integer save(Integer workItemID){
		return (Integer) openSession().save(new VisualStudioWorkItemEntity(workItemID));
	}
	
    public Integer save(String workItemID){
		return (Integer) openSession().save(new VisualStudioWorkItemEntity(Integer.parseInt(workItemID)));
	}
    
    public VisualStudioWorkItemEntity getByEntityID(String entityID){
		List<VisualStudioWorkItemEntity> entities = (List<VisualStudioWorkItemEntity>) openSession().createCriteria(VisualStudioWorkItemEntity.class).add(Restrictions.eq("workItemID", Integer.valueOf(entityID))).list();
		
		if(entities.size() > 0){
			return entities.get(0);
		} else {
			return null;
		}
	
    }
}
