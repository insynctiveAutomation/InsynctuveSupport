package insynctive.dao;

import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import insynctive.model.TargetProcessIntercomEntity;

@Repository
@Transactional
public class TargetProcessAndIntercomDao {

	private final SessionFactory sessionFactory;

	@Inject
	public TargetProcessAndIntercomDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private Session openSession() {
		return sessionFactory.getCurrentSession();
	}

	public void save(TargetProcessIntercomEntity entity){
		openSession().save(entity);
	}

	public void updateStatus(TargetProcessIntercomEntity entity, String newStatus){
		entity.setStatus(newStatus);
		openSession().update(entity);
	}

	public TargetProcessIntercomEntity getByEntityID(String entityID){
		List<TargetProcessIntercomEntity> entities = (List<TargetProcessIntercomEntity>) openSession().createCriteria(TargetProcessIntercomEntity.class).add(Restrictions.eq("entityID", entityID)).list();
		if(entities.size() > 0){
			return entities.get(0);
		} else {
			return null;
		}
	}
}
