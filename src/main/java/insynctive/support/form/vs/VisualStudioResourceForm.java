package insynctive.support.form.vs;

import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import insynctive.support.utils.vs.VisualStudioTaskData;

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
			return revision.isMergeToMasterBug();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isTestStrategy() {
		if(revision != null) {
			return revision.isTestStrategyBug();
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
			return revision.isTestOnMasterBug();
		}
		return false;
	}

	@JsonIgnore
	public boolean isInvestigateBug() {
		if(revision != null) {
			return revision.isInvestigateBug();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isEstimateStory() {
		if(revision != null) {
			return revision.isEstimateStory();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isTestingStrategyStory() {
		if(revision != null) {
			return revision.isTestingStrategyStory();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isDevelopTDD() {
		if(revision != null) {
			return revision.isDevelopTDD();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isDevelopCodeStory() {
		if(revision != null) {
			return revision.isDevelopCodeStory();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isPostStoryMovie() {
		if(revision != null) {
			return revision.isPostStoryMovie();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isApproveStoryMovie() {
		if(revision != null) {
			return revision.isApproveStoryMovie();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isPullRequestForStory() {
		if(revision != null) {
			return revision.isPullRequestForStory();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isFunctionalTestOnIntegration() {
		if(revision != null) {
			return revision.isFunctionalTestOnIntegration();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isUIAutomatedTesting() {
		if(revision != null) {
			return revision.isUIAutomatedTesting();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isMergeToMasterStory() {
		if(revision != null) {
			return revision.isMergeToMasterStory();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isTestOnMasterStory() {
		if(revision != null) {
			return revision.isTestOnMasterStory();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isApproveForRelease() {
		if(revision != null) {
			return revision.isApproveForRelease();
		}
		return false;
	}
	
	@JsonIgnore
	public boolean isABug(){
		return revision != null && revision.isABug();
	}
	
	@JsonIgnore
	public boolean isATask(){
		return revision != null && revision.isATask();
	}
	
	@JsonIgnore
	public boolean isAStory(){
		return revision != null && revision.isAStory();
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
	
	public List<VisualStudioRelationsForm> getParents() throws Exception {
		if(revision != null){
			return revision.getParents();
		} else {
			throw new Exception("Revision is NULL");
		}
	}
	
	public List<VisualStudioRelationsForm> getChilds() throws Exception {
		if(revision != null){
			return revision.getChilds();
		} else {
			throw new Exception("Revision is NULL");
		}
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
