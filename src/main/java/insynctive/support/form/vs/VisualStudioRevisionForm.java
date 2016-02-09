package insynctive.support.form.vs;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import insynctive.support.utils.VisualStudioUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualStudioRevisionForm {

	private VisualStudioFieldForm fields;
	private List<VisualStudioRelationsForm> relations = new ArrayList<>();
	private Integer id;

	public VisualStudioFieldForm getFields() {
		return fields;
	}
	public void setFields(VisualStudioFieldForm fields) {
		this.fields = fields;
	}

	public List<VisualStudioRelationsForm> getRelations() {
		return relations;
	}
	public void setRelations(List<VisualStudioRelationsForm> relations) {
		this.relations = relations;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	//Methods
	@JsonIgnore
	public String getTitle() {
		return fields.getTitle();
	}
	
	@JsonIgnore
	public Boolean haveRelations(){
		return relations != null && relations.size() > 0;
	}
	
	@JsonIgnore
	public VisualStudioRelationsForm getFirstRelation(){
		if(haveRelations()){
			return relations.get(0);
		} else {
			return null;
		}
	}
	
	@JsonIgnore
	public VisualStudioRelationsForm getRelationNumber(Integer number){
		if(haveRelations() && relations.size() >= number){
			return relations.get(number-1);
		} else {
			return null;
		}
	}

	@JsonIgnore
	public String getAssignedToEmail() throws Exception {
		if(fields != null){
			return fields.getAssignedToEmail();
		} 
		throw new Exception("VisualStudioRevisionForm.getAssignedToEmail");
	}
	
	@JsonIgnore
	public String getNameOfOwner() throws Exception {
		if(fields != null){
			return fields.getNameOfOwner();
		} 
		throw new Exception("VisualStudioRevisionForm.getNameOfOwner");
	}
	
	@JsonIgnore
	public boolean isDevelopFix(){
		String title = getTitle();
		return (title != null) ? title.toLowerCase().equals("develop fix") || title.toLowerCase().equals("developfix")  : false;
	}
	
	@JsonIgnore
	public boolean isTestFix(){
		String title = getTitle();
		return (title != null) ? title.toLowerCase().equals("test fix") || title.toLowerCase().equals("testfix")  : false;
	}
	
	@JsonIgnore
	public boolean isMergeToMaster(){
		String title = getTitle();
		return (title != null) ? title.toLowerCase().equals("merge to master") || title.toLowerCase().equals("mergetomaster")  : false;
	}
	
	@JsonIgnore
	public boolean isABug(){
		return getFields().isBug();
	}
	
	@JsonIgnore
	public boolean isATask(){
		return getFields().isTask();
	}
	
	@JsonIgnore	
	public VisualStudioRevisionForm findTestFixTask(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isTestFix()) return workItem;
		}
		return null;
	}
	
	@JsonIgnore	
	public String findIDOfTestFixTask(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isTestFix()) return relation.getRelationID();
		}
		return null;
	}
	
	
	@JsonIgnore	
	public VisualStudioRevisionForm findDevelopFixTask(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isDevelopFix()) return workItem;
		}
		return null;
	}

	@JsonIgnore	
	public String findIDOfDevelopFixTask(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isDevelopFix()) return relation.getRelationID();
		}
		return null;
	}
	
	
	@JsonIgnore	
	public VisualStudioRevisionForm findMergeToMasterTask(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isMergeToMaster()) return workItem;
		}
		return null;
	}
	
	@JsonIgnore	
	public String findIDOfMergeToMasterTask(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isMergeToMaster()) return relation.getRelationID();
		}
		return null;
	}
	
	@JsonIgnore	
	public void changeState(String value) throws Exception {
		if(fields != null){
			fields.setState(value);
		} else{
			throw new Exception("fields not assigned.");
		}
	}

	@JsonIgnore	
	public String getProject() throws Exception {
		if(fields != null){
			return fields.getProject();
		}
			throw new Exception("VisualStudioRevisionForm.getProject()");
	}
	
	@JsonIgnore	
	public String getIteration() throws Exception {
		if(fields != null){
			return fields.getIteration();
		}
			throw new Exception("VisualStudioRevisionForm.getIteration()");
	}
	
}
