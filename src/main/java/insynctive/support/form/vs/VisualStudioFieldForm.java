package insynctive.support.form.vs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualStudioFieldForm {

	@JsonProperty("System.Title")
	private String title;
	
	@JsonProperty("System.TeamProject")
	private String project;
	
	@JsonProperty("System.State")
	private String state;
	
	@JsonProperty("System.WorkItemType")
	private String type;
	
	@JsonProperty("System.IterationPath")
	private String iteration;
	
	@JsonProperty("System.AssignedTo")
	private String assignedTo;
	
	
	@JsonProperty("System.CreatedBy")
	private String createdBy;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getIteration() {
		return iteration;
	}

	public void setIteration(String iteration) {
		this.iteration = iteration;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	//Methods
	@JsonIgnore
	public boolean isBug(){
		return state != null && type.toLowerCase().equals("bug");
	}

	@JsonIgnore
	public boolean isTask() {
		return state != null && type.toLowerCase().equals("task");
	}

	@JsonIgnore
	public String getAssignedToEmail() {
		if(assignedTo != null){
			return assignedTo.split("<")[1].split(">")[0];
		} else {
			return "";
		}
	}
	
	@JsonIgnore
	public String getNameOfOwner() {
		if(assignedTo != null){
			return assignedTo.split(" <")[0];
		} else {
			return "";
		}
	}
}
