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
	public String getType() {
		return revision != null ? revision.getType() : null;
	}
	
	@JsonIgnore
	public boolean isDevelopFix(){
		if(revision != null) {
			return revision.isDevelopFix();
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
	public boolean isTestStrategy() {
		if(revision != null) {
			return revision.isTestStrategy();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isCreateANewBranch() {
		if(revision != null) {
			return revision.isCreateANewBranch();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isReproduceWithAutomatedTest() {
		if(revision != null) {
			return revision.isReproduceWithAutomatedTest();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isGetCodeReview() {
		if(revision != null) {
			return revision.isGetCodeReview();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isFunctionalTest() {
		if(revision != null) {
			return revision.isFunctionalTest();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isRebaseIntegrationToMaster() {
		if(revision != null) {
			return revision.isRebaseIntegrationToMaster();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isTestOnMaster() {
		if(revision != null) {
			return revision.isTestOnMaster();
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
	public String getProject() throws Exception {
		if(revision != null){
			return revision.getProject();
		}
		throw new Exception("VisualStudioResourceForm.getProject()");
	}
	
	@JsonIgnore
	public String getAssignedToName() throws Exception {
		if(revision != null){
			return revision.getAssignedToName();
		}
		throw new Exception("VisualStudioResourceForm.getAssignToName()");
	}

	@JsonIgnore
	public String getAssignedToEmail() throws Exception {
		if(revision != null && revision.getFields() != null){
			return revision.getAssignedToEmail();
		}
		throw new Exception("VisualStudioResourceForm.getAssignedToEmail()");
	}
	
	@JsonIgnore
	public String getCreatedByEmail() throws Exception {
		if(revision != null){
			return revision.getCreatedByEmail();
		}
		throw new Exception("VisualStudioResourceForm.getCreatedByEmail()");
	}

	@JsonIgnore
	public String getCreatedByName() throws Exception {
		if(revision != null && revision.getFields() != null){
			return revision.getCreatedByName();
		}
		throw new Exception("VisualStudioResourceForm.getCreatedByName()");
	}
	
	@JsonIgnore
	public String getIteration() throws Exception {
		if(revision != null){
			return revision.getIteration();
		}
		throw new Exception("VisualStudioResourceForm.getIteration()");
	}

	@JsonIgnore
	public VisualStudioRelationsForm getFirstRelation() {
		if(revision != null){
			return revision.getFirstRelation();
		}
		return null;
	}

	@JsonIgnore
	public String getCreatedBy() throws Exception {
		if(revision != null){
			return revision.getCreatedBy(); 
		}
		return null;
	}

	@JsonIgnore
	public boolean wasChangeToCritical() {
		return getFields() != null && getFields().isCritical() && !getFields().wasCritical();
	}
	
	@JsonIgnore
	public boolean isCritical() {
		return revision != null && revision.isCritical();
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
	public boolean waschangeToRemoved() {
		return getFields() != null && getFields().isRemoved() && !getFields().wasRemoved();
	}
	
	@JsonIgnore
	public boolean wasChangeToInProgress() {
		return getFields() != null && getFields().isInProgress() && !getFields().wasInProgress();
	}

	@JsonIgnore
	public boolean wasChangeFromDoneToTodo() {
		return getFields() != null && getFields().isTodo() && getFields().wasDone();
	}
	
	@JsonIgnore
	public boolean wasChangeAssignation() {
		return getFields() != null && getFields().wasChangeAssignation();
	}
	
	@JsonIgnore
	public String getOldAssigned() {
		return getFields() != null ? getFields().getOldAssigned() : null;
	}
	
	@JsonIgnore
	public String getOldAssignedName() {
		return getFields() != null ? getFields().getOldAssignedName() : null;
	}
	
	@JsonIgnore
	public String getOldAssignedEmail() {
		return getFields() != null ? getFields().getOldAssignedEmail() : null;
	}
}
