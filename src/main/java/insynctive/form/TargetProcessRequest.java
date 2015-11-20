package insynctive.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TargetProcessRequest {

	private String entityID;
	private String intercomID;
	private String status;
	
	public String getEntityID() {
		return entityID;
	}
	public void setEntityID(String entityID) {
		this.entityID = entityID;
	}
	
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
	
}
