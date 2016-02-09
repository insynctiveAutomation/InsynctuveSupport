package insynctive.support.form.vs;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualStudioResourceForm {

	/**
	 * 	- workItemId (ID)
	 * 	- fields
	 * 		- System.State ("Approved" - "New")
	 * 			- oldValue
	 * 			- newValue
	 * 	- relations
	 * 	- revision
	 *  - url
	 * */
	
	private BigInteger workItemId;
	private VisualStudioChangeFieldForm fields;
	private VisualStudioRevisionForm revision;
	private String url;
	
	public BigInteger getWorkItemId() {
		return workItemId;
	}
	public void setWorkItemId(BigInteger workItemId) {
		this.workItemId = workItemId;
	}
	
	public VisualStudioChangeFieldForm getFields() {
		return fields;
	}
	public void setFields(VisualStudioChangeFieldForm fields) {
		this.fields = fields;
	}
	
	public VisualStudioRevisionForm getRevision() {
		return revision;
	}
	public void setRevision(VisualStudioRevisionForm revision) {
		this.revision = revision;
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	//Methods
	@JsonIgnore
	public String getTitle(){
		return revision != null ? revision.getTitle() : "";
	}
	
	@JsonIgnore
	public boolean isDevelopFix(){
		if(revision != null) {
			return revision.isDevelopFix();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isTestFix(){
		if(revision != null) {
			return revision.isTestFix();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isMergeToMaster(){
		if(revision != null) {
			return revision.isMergeToMaster();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isABug(){
		return revision != null && revision.getFields().isBug();
	}
	
	@JsonIgnore
	public boolean isATask(){
		return revision != null && revision.getFields().isTask();
	}

	@JsonIgnore
	public String getAssignedToEmail() throws Exception {
		if(revision != null && revision.getFields() != null){
			return revision.getAssignedToEmail();
		}
		throw new Exception("VisualStudioResourceForm.getAssignedToEmail()");
	}
	
	@JsonIgnore
	public String getProject() throws Exception {
		if(revision != null){
			return revision.getProject();
		}
		throw new Exception("VisualStudioResourceForm.getProject()");
	}
	
	@JsonIgnore
	public String getNameOfOwner() throws Exception {
		if(revision != null){
			return revision.getNameOfOwner();
		}
		throw new Exception("VisualStudioResourceForm.getProject()");
	}
	
	@JsonIgnore
	public String getIteration() throws Exception {
		if(revision != null){
			return revision.getIteration();
		}
		throw new Exception("VisualStudioResourceForm.getIteration()");
	}

	@JsonIgnore
	public boolean wasChangeToApproved() {
		return getFields() != null && getFields().isApproved() && !getFields().wasApproved();
	}
	
	@JsonIgnore
	public boolean wasChangeToDone() {
		return getFields() != null && getFields().isDone() && !getFields().wasDone();
	}
	
	@JsonIgnore
	public boolean wasChangeToInProgress() {
		return getFields() != null && getFields().isInProgress() && !getFields().wasInProgress();
	}

	@JsonIgnore
	public boolean wasChangeFromDoneToTodo() {
		return getFields() != null && getFields().isTodo() && getFields().wasDone();
	}
}
