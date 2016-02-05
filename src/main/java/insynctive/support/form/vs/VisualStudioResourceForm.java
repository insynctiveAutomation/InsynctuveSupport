package insynctive.support.form.vs;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
	public boolean isTestBug(){
		if(revision != null) {
			return revision.isTestBug();
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
		throw new Exception("getAssignedToEmail");
	}
}
