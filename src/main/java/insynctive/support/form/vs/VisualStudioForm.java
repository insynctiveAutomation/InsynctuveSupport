package insynctive.support.form.vs;

import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import insynctive.support.utils.VisualStudioUtil;
import insynctive.support.utils.vs.VisualStudioTaskData;

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
	public boolean changedToCritical() {
		return resource != null && resource.wasChangeToCritical();
	}
	
	@JsonIgnore	
	public boolean criticalChangeStateToDone() {
		return isCritical() && changedToDone();
	}

	@JsonIgnore	
	public boolean isCritical() {
		return resource != null && resource.isCritical();
	}

	@JsonIgnore
	public boolean changedAssignation() {
		return resource != null && resource.wasChangeAssignation(); 
	}
	
	@JsonIgnore
	public boolean wasChangeToApprooved(){
		return resource != null && resource.wasChangeToApproved(); 
	}
	
	@JsonIgnore
	public boolean changedToDone(){
		return resource != null && resource.wasChangeToDone();
	}
	
	@JsonIgnore
	public boolean waschangeToRemoved(){
		return resource != null && resource.waschangeToRemoved();
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
	public boolean isInvestigateBug() {
		return resource.isInvestigateBug();
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
	
	@JsonIgnore
	public boolean isEstimateStory() {
		return resource.isEstimateStory();	
	}
	
	@JsonIgnore
	public boolean isTestingStrategyStory() {
		return resource.isTestingStrategyStory();	
	}
	
	@JsonIgnore
	public boolean isDevelopTDD() {
		return resource.isDevelopTDD();	
	}
	
	@JsonIgnore
	public boolean isDevelopCodeStory() {
		return resource.isDevelopCodeStory();	
	}
	
	@JsonIgnore
	public boolean isPostStoryMovie() {
		return resource.isPostStoryMovie();	
	}
	
	@JsonIgnore
	public boolean isPullRequestForStory() {
		return resource.isPullRequestForStory();	
	}
	
	@JsonIgnore
	public boolean isFunctionalTestOnIntegration() {
		return resource.isFunctionalTestOnIntegration();	
	}
	
	@JsonIgnore
	public boolean isUIAutomatedTesting() {
		return resource.isUIAutomatedTesting();	
	}
	
	@JsonIgnore
	public boolean isMergeToMasterStory() {
		return resource.isMergeToMasterStory();	
	}
	
	@JsonIgnore
	public boolean isTestOnMasterStory() {
		return resource.isTestOnMasterStory();	
	}
	
	@JsonIgnore
	public boolean isApproveForRelease() {
		return resource.isApproveForRelease();	
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
	public boolean isAStory(){
		return resource.isAStory();
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
	public VisualStudioRevisionForm getParentFullObject(String account) throws Exception {
		
		return VisualStudioUtil.getWorkItem(getParent().getRelationID(), account);
	}
	
	@JsonIgnore	
	public VisualStudioRelationsForm getParent() throws Exception {
		List<VisualStudioRelationsForm> parents = getParents();
		return parents.size() > 0 ? parents.get(0) : null;
	}
	
	@JsonIgnore	
	public List<VisualStudioRelationsForm> getParents() throws Exception {
		if(resource != null){
			return resource.getParents();
		} else {
			throw new Exception("resource is NULL");
		}
	}
	
	@JsonIgnore	
	public List<VisualStudioRelationsForm> getChilds() throws Exception {
		if(resource != null){
			return resource.getChilds();
		} else {
			throw new Exception("resource is NULL");
		}
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
	
	public boolean noHaveParent() throws Exception{
		return getParents().size() == 0;
	}
}