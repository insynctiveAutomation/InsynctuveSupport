package insynctive.support.dao;

import java.math.BigDecimal;
import java.math.BigInteger;

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
	
	private BigInteger save(RunID runID){
		return (BigInteger) openSession().save(runID);
	}
	
	public synchronized BigInteger getNextRunID(){
		BigInteger id = save(new RunID());
		BigInteger two = new BigInteger("2");
		BigInteger ten = new BigInteger("10");
		Double runID = new Double(id.subtract(two).divide(ten).toString()); 
		
		return new BigDecimal(runID).toBigInteger();
	}
	
}
