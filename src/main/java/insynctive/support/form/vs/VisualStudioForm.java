package insynctive.support.form.vs;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import insynctive.support.utils.VisualStudioUtil;
import insynctive.support.utils.vs.VisualStudioTaskName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualStudioForm {

	/**
	 * - resource < ITEM UPDETEADO
	 * 		- workItemId (ID)
	 * 		- fields
	 * 			- System.State ("Approved" - "New" - "In Progress")
	 * 				- oldValue
	 * 				- newValue
	 * 		- relations
	 *  	- revision
	 *  		- fields
	 * */
	
	private VisualStudioResourceForm resource;

	public VisualStudioResourceForm getResource() {
		return resource;
	}

	public void setResource(VisualStudioResourceForm resource) {
		this.resource = resource;
	}
	
	//METHODS
	@JsonIgnore
	public String getTitle(){
		return resource.getTitle();
	}

	@JsonIgnore
	public String getUrl(){
		return resource != null ? resource.getUrl() : "";
	}
	
	@JsonIgnore
	public VisualStudioRelationsForm getFirstRelation(){
		if(resource != null){
			return resource.getFirstRelation();
		}
		return null;
	}

	@JsonIgnore
	public String getCreatedBy() throws Exception {
		if(resource != null){
			return resource.getCreatedBy();
		}
		return null;
	}

	@JsonIgnore
	public BigInteger getWorkItemID(){
		return resource.getWorkItemId();
	}

	@JsonIgnore	
	public boolean wasChangeToCritical() {
		return resource != null && resource.wasChangeToCritical();
	}

	@JsonIgnore	
	public boolean isCritical() {
		return resource != null && resource.isCritical();
	}

	@JsonIgnore
	public boolean wasChangeAssignation() {
		return resource != null && resource.wasChangeAssignation(); 
	}
	
	@JsonIgnore
	public boolean wasChangeToApprooved(){
		return resource != null && resource.wasChangeToApproved(); 
	}
	
	@JsonIgnore
	public boolean wasChangeToDone(){
		return resource != null && resource.wasChangeToDone();
	}
	
	@JsonIgnore	
	public boolean wasChangeToInProgress(){
		return resource != null && resource.wasChangeToInProgress();
	}

	@JsonIgnore	
	public boolean wasChangeFromDoneToToDo() {
		return resource != null && resource.wasChangeFromDoneToTodo();
	}
	
	//TASK Manage
	@JsonIgnore
	public String getOldAssigned(){
		return resource.getOldAssigned();
	}
	
	@JsonIgnore
	public String getOldAssignedName(){
		return resource.getOldAssignedName();
	}
	
	@JsonIgnore
	public String getOldAssignedEmail(){
		return resource.getOldAssignedEmail();
	}
	
	@JsonIgnore
	public boolean isDevelopFix(){
		return resource.isDevelopFix();
	}
	
	@JsonIgnore
	public boolean isMergeToMaster(){
		return resource.isMergeToMaster();
	}
	
	@JsonIgnore
	public boolean isTestStrategy() {
		return resource.isTestStrategy();
	}
	
	@JsonIgnore
	public boolean isCreateANewBranch() {
		return resource.isCreateANewBranch();
	}
	
	@JsonIgnore
	public boolean isReproduceWithAutomatedTest() {
		return resource.isReproduceWithAutomatedTest();
	}
	
	@JsonIgnore
	public boolean isGetCodeReview() {
		return resource.isGetCodeReview();
	}
	
	@JsonIgnore
	public boolean isFunctionalTest() {
		return resource.isFunctionalTest();
	}
	
	@JsonIgnore
	public boolean isRebaseIntegrationToMaster() {
		return resource.isRebaseIntegrationToMaster();
	}
	
	@JsonIgnore
	public boolean isTestOnMaster() {
		return resource.isTestOnMaster();
	}
	
	//Work Item Manage
	@JsonIgnore	
	public boolean isABug(){
		return resource.isABug();
	}
	
	@JsonIgnore	
	public boolean isATask(){
		return resource.isATask();
	}
	
	@JsonIgnore	
	public String getProject() throws Exception{
		return resource.getProject();
	}
	
	@JsonIgnore	
	public String getAssignedToName() throws Exception{
		if(resource != null){
			return resource.getAssignedToName();
		}
		throw new Exception("VisualStudioForm.getAssignedToName()");
	}
	
	@JsonIgnore	
	public String getAssignedToEmail() throws Exception{
		if(resource != null){
			return resource.getAssignedToEmail();
		}
		throw new Exception("VisualStudioForm.getAssignToEmail()");
	}
	
	@JsonIgnore	
	public String getCreatedByEmail() throws Exception{
		if(resource != null){
			return resource.getCreatedByEmail();
		}
		throw new Exception("VisualStudioForm.getCreatedByEmail()");
	}
	
	@JsonIgnore	
	public String getCreatedByName() throws Exception{
		if(resource != null){
			return resource.getCreatedByName();
		}
		throw new Exception("VisualStudioForm.getCreatedByName()");
	}
	
	@JsonIgnore	
	public VisualStudioRevisionForm getFirstRelationFullObject(String account) throws Exception {
		return VisualStudioUtil.getWorkItem(getFirstRelation().getRelationID(), account);
	}

	@JsonIgnore	
	public boolean noHaveParent() {
		return getFirstRelation() == null;
	}

	@JsonIgnore	
	public String getIteration() throws Exception {
		if(resource != null){
			return resource.getIteration();
		}
		throw new Exception("VisualStudioForm.getIteration");
	}
	
	@JsonIgnore	
	public String getType() throws Exception{
		if(resource != null){
			return resource.getType();
		}
		throw new Exception("VisualStudioForm.getType()");
	}
}