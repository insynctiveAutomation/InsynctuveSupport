package insynctive.support.form.vs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import insynctive.support.utils.vs.VisualStudioBugState;
import insynctive.support.utils.vs.VisualStudioTag;
import insynctive.support.utils.vs.VisualStudioTaskState;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualStudioChangeFieldForm {

	/**
	 *	- System.State 
	 *		- oldValue
	 *		- newValue
	 * */
	
	@JsonProperty("System.State")
	private VisualStudioChangeValue state;
	
	@JsonProperty("System.AssignedTo")
	private VisualStudioChangeValue assignedTo;
	
	@JsonProperty("System.Tags")
	private VisualStudioChangeValue tags;
	
	public VisualStudioChangeValue getState() {
		return state;
	}

	public void setState(VisualStudioChangeValue state) {
		this.state = state;
	}
	
	public VisualStudioChangeValue getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(VisualStudioChangeValue assignedTo) {
		this.assignedTo = assignedTo;
	}

	//Methods
	@JsonIgnore
	public boolean isCritical() {
		return tags.getNewValue().toLowerCase().contains(VisualStudioTag.CRITICAL.getValue().toLowerCase());
	}

	@JsonIgnore
	public boolean wasCritical() {
		return tags.getOldValue().toLowerCase().contains(VisualStudioTag.CRITICAL.getValue().toLowerCase());
	}
	
	@JsonIgnore
	public boolean isApproved(){
		return state != null && state.getNewValue().equals(VisualStudioBugState.APPROVED.value);
	}
	
	@JsonIgnore
	public boolean wasApproved(){
		return state != null && state.getOldValue().equals(VisualStudioBugState.APPROVED.value);
	}

	@JsonIgnore
	public boolean isDone(){
		return state != null && state.getNewValue().equals(VisualStudioTaskState.DONE.value);
	}

	@JsonIgnore
	public boolean wasDone(){
		return state != null && state.getOldValue().equals(VisualStudioTaskState.DONE.value);
	}

	@JsonIgnore
	public boolean isInProgress(){
		return state != null && state.getNewValue().equals(VisualStudioTaskState.IN_PROGRESS.value);
	}

	@JsonIgnore
	public boolean wasInProgress(){
		return state != null && state.getOldValue().equals(VisualStudioTaskState.IN_PROGRESS.value);
	}

	@JsonIgnore
	public boolean isTodo() {
		return state != null && state.getNewValue().equals(VisualStudioTaskState.DONE.value);
	}

	@JsonIgnore
	public boolean wasTodo() {
		return state != null && state.getOldValue().equals(VisualStudioTaskState.DONE.value);
	}

	@JsonIgnore
	public boolean wasChangeAssignation() {
		return assignedTo != null && !assignedTo.getNewValue().equals(assignedTo.getOldValue());
	}

	public String getOldAssigned() {
		return (assignedTo != null) ? assignedTo.getOldValue() : null;
	}
	
	public String getOldAssignedName() {
		return (assignedTo != null) ? getName(assignedTo.getOldValue()) : null;
	}
	
	public String getOldAssignedEmail() {
		return (assignedTo != null) ? getEmail(assignedTo.getOldValue()) : null;
	}
	
	@JsonIgnore
	private String getEmail(String str) {
		if(str != null){
			return str.split("<")[1].split(">")[0];
		} else {
			return "";
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
	
}
