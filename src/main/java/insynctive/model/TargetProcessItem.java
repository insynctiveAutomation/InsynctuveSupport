package insynctive.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TargetProcessItem {

	@JsonProperty("ResourceType")
	private String resourceType;
	
	@JsonProperty("Id")
	private Integer id;
	
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	} 
	
}
