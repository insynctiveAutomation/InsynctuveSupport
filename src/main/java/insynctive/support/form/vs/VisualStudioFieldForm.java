package insynctive.support.form.vs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import insynctive.support.utils.vs.VisualStudioTag;

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
	
	@JsonProperty("System.ChangedBy")
	private String changeBy;
	
	@JsonProperty("System.Tags")
	private String tags;

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

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getChangeBy() {
		return changeBy;
	}

	public void setChangeBy(String changeBy) {
		this.changeBy = changeBy;
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
	private String getEmail(String str) {
		try {
			if(str != null){
				return str.split("<")[1].split(">")[0];
			} else {
				return "";
			}
		} catch (Exception ex){
			return str;
		}
	}
	
	@JsonIgnore
	private String getName(String str) {
		if(str != null){
			return str.split(" <")[0];
		} else {
			return "";
		}
	}

	@JsonIgnore
	public String getAssignedToEmail() {
		return getEmail(assignedTo);
	}
	
	@JsonIgnore
	public String getAssignedToName() {
		return getName(assignedTo);
	}

	@JsonIgnore
	public String getCreatedByEmail() {
		return getEmail(createdBy);
	}
	
	@JsonIgnore
	public String getCreatedByName() {
		return getName(createdBy);
	}

	@JsonIgnore
	public boolean isCritical() {
		return tags != null && tags.toLowerCase().contains(VisualStudioTag.CRITICAL.getValue().toLowerCase());
	}
	
	@JsonIgnore
	public String getChangeByName() {
		return getName(changeBy);
	}
	
}
