package insynctive.support.form.vs;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import insynctive.support.utils.VisualStudioUtil;

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
	
	public VisualStudioRelationsForm getFirstRelation(){
		if(resource != null){
			VisualStudioRevisionForm revision = resource.getRevision();
			if(revision != null){
				return revision.getFirstRelation();
			}
		}
		return null;
	}

	@JsonIgnore
	public BigInteger getWorkItemID(){
		return resource.getWorkItemId();
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
	
	@JsonIgnore	
	public boolean isDevelopFix(){
		return resource.isDevelopFix();
	}
	
	@JsonIgnore	
	public boolean isTestFix(){
		return resource.isTestFix();
	}
	
	@JsonIgnore	
	public boolean isMergeToMaster(){
		return resource.isMergeToMaster();
	}
	
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
	public String getNameOfOwner() throws Exception{
		if(resource != null){
			return resource.getNameOfOwner();
		}
		throw new Exception("VisualStudioForm.getNameOfOwner");
	}
	
	@JsonIgnore	
	public VisualStudioRevisionForm getFirstRelationFullObject(String account) throws Exception {
		return VisualStudioUtil.getWorkItem(getFirstRelation().getRelationID(), account);
	}

	@JsonIgnore	
	public String getIteration() throws Exception {
		if(resource != null){
			return resource.getIteration();
		}
		throw new Exception("VisualStudioForm.getIteration");
	}
}