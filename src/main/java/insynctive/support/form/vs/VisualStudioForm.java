package insynctive.support.form.vs;

import java.io.IOException;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import insynctive.support.utils.vs.VisualStudioField;
import insynctive.support.utils.vs.VisualStudioUtil;

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
		return resource != null && resource.getFields().isApproved() && !resource.getFields().wasApproved(); 
	}
	
	@JsonIgnore
	public boolean wasChangeToDone(){
		return resource != null && resource.getFields().isDone() && !resource.getFields().wasDone(); 
	}
	
	@JsonIgnore	
	public boolean wasChangeToInProgress(){
		return resource != null && resource.getFields().isInProgress() && !resource.getFields().wasInProgress(); 
	}
	
	@JsonIgnore	
	public boolean isDevelopFix(){
		return resource.isDevelopFix();
	}
	
	@JsonIgnore	
	public boolean isTestBug(){
		return resource.isTestBug();
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
	public String getNameOfOwner() throws Exception{
		if(resource.getRevision() != null){
			return resource.getRevision().getNameOfOwner();
		}
		throw new Exception("VisualStudioForm.getNameOfOwner");
	}
	
	@JsonIgnore	
	public VisualStudioRevisionForm getFirstRelationFullObject() throws Exception {
		return VisualStudioUtil.getWorkItem(getFirstRelation().getRelationID());
	}
	
}