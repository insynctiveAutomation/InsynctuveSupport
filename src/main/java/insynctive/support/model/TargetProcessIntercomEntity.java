package insynctive.support.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import insynctive.support.form.TargetProcessRequest;

@Entity
@Table(name = "TargetProcessIntercomEntity")
public class TargetProcessIntercomEntity {
	
	@Id
	@GeneratedValue
	@Column(name = "id")
	public Integer id;
	
	@Column(name = "intercom_id")
	private String intercomID;
	
	@Column(name = "status")
	private String status;

	@Column(name = "entity_id")
	private String entityID;
	
	
	public String getIntercomID() {
		return intercomID;
	}
	public void setIntercomID(String intercomID) {
		this.intercomID = intercomID;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getEntityID() {
		return entityID;
	}
	public void setEntityID(String entityID) {
		this.entityID = entityID;
	}
	public void addValuesFromTPRequest(TargetProcessRequest form) {
		this.status = form.getStatus();
		this.intercomID = form.getIntercomID();
		this.entityID = form.getEntityID();
	}
	
}
