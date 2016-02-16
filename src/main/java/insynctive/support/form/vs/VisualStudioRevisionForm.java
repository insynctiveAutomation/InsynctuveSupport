package insynctive.support.form.vs;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import insynctive.support.utils.VisualStudioUtil;
import insynctive.support.utils.vs.VisualStudioTaskName;
import insynctive.support.utils.vs.VisualStudioTaskState;

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
	public String getAssignedToName() throws Exception {
		if(fields != null){
			return fields.getAssignedToName();
		} 
		throw new Exception("VisualStudioRevisionForm.getAssignToName");
	}
	
	@JsonIgnore
	public String getCreatedByEmail() throws Exception {
		if(fields != null){
			return fields.getCreatedByEmail();
		} 
		throw new Exception("VisualStudioRevisionForm.getCreatedByEmail");
	}

	@JsonIgnore
	public String getCreatedByName() throws Exception {
		if(fields != null){
			return fields.getCreatedByName();
		} 
		throw new Exception("VisualStudioRevisionForm.getCreatedByName");
	}
	
	@JsonIgnore
	public boolean isDevelopFix(){
		String title = getTitle();
		return (title != null) ? title.equals(VisualStudioTaskName.DEVELOP_FIX.value) : false;
	}
	
	@JsonIgnore
	public boolean isMergeToMaster(){
		String title = getTitle();
		return (title != null) ? title.equals(VisualStudioTaskName.MERGE_TO_MASTER.value) : false;
	}
	
	@JsonIgnore
	public boolean isTestStrategy() {
		String title = getTitle();
		return (title != null) ? title.equals(VisualStudioTaskName.TEST_STRATEGY.value) : false;
	}
	
	@JsonIgnore
	public boolean isCreateANewBranch() {
		String title = getTitle();
		return (title != null) ? title.equals(VisualStudioTaskName.CREATE_A_NEW_BRANCH.value) : false;
	}
	
	@JsonIgnore
	public boolean isReproduceWithAutomatedTest() {
		String title = getTitle();
		return (title != null) ? title.equals(VisualStudioTaskName.REPRODUCE_WITH_AUTOMATED_TESTS.value) : false;
	}
	
	@JsonIgnore
	public boolean isGetCodeReview() {
		String title = getTitle();
		return (title != null) ? title.equals(VisualStudioTaskName.GET_CODE_REVIEW.value) : false;
	}
	
	@JsonIgnore
	public boolean isFunctionalTest() {
		String title = getTitle();
		return (title != null) ? title.equals(VisualStudioTaskName.FUNCTIONAL_TEST.value) : false;
	}
	
	@JsonIgnore
	public boolean isRebaseIntegrationToMaster() {
		String title = getTitle();
		return (title != null) ? title.equals(VisualStudioTaskName.REABASE_INTEGRATION_TO_MASTER.value) : false;
	}
	
	@JsonIgnore
	public boolean isTestOnMaster() {
		String title = getTitle();
		return (title != null) ? title.equals(VisualStudioTaskName.TEST_ON_MASTER.value) : false;
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
	public boolean isCritical() {
		return getFields().isCritical();
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
	public VisualStudioRevisionForm findMergeToMasterTask(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isMergeToMaster()) return workItem;
		}
		return null;
	}
	
	@JsonIgnore	
	public VisualStudioRevisionForm findCreateNewBranch(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isCreateANewBranch()) return workItem;
		}
		return null;
	}
	
	@JsonIgnore	
	public VisualStudioRevisionForm findTestStrategy(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isTestStrategy()) return workItem;
		}
		return null;
	}

	@JsonIgnore	
	public VisualStudioRevisionForm findReproduceWithAutomatedTest(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isReproduceWithAutomatedTest()) return workItem;
		}
		return null;
	}

	@JsonIgnore	
	public VisualStudioRevisionForm findDevelopFix(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isDevelopFix()) return workItem;
		}
		return null;
	}

	@JsonIgnore	
	public VisualStudioRevisionForm findGetCodeReview(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isGetCodeReview()) return workItem;
		}
		return null;
	}

	@JsonIgnore	
	public VisualStudioRevisionForm findFunctionalTest(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isFunctionalTest()) return workItem;
		}
		return null;
	}

	@JsonIgnore	
	public VisualStudioRevisionForm findMergeToMaster(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isMergeToMaster()) return workItem;
		}
		return null;
	}

	@JsonIgnore	
	public VisualStudioRevisionForm findRebaseIntegrationToMaster(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isRebaseIntegrationToMaster()) return workItem;
		}
		return null;
	}

	@JsonIgnore	
	public VisualStudioRevisionForm findDoneDoneTest(String account) throws Exception {
		for(VisualStudioRelationsForm relation : relations){
			VisualStudioRevisionForm workItem = VisualStudioUtil.getWorkItem(relation.getRelationID(), account);
			if(workItem.isTestOnMaster()) return workItem;
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
	
	@JsonIgnore	
	public String getCreatedBy() throws Exception {
		if(fields != null){
			return fields.getCreatedBy();
		}
			throw new Exception("VisualStudioRevisionForm.getCreatedBy()");
	}

	@JsonIgnore	
	public String getState() {
		return fields.getState();
	}

	@JsonIgnore	
	public boolean isState(VisualStudioTaskState state) {
		return fields.getState().equals(state.value);
	}
	
	@JsonIgnore	
	public boolean isStateToDo() {
		return fields.getState().equals(VisualStudioTaskState.TO_DO.value);
	}
	
	@JsonIgnore	
	public boolean isStateInProgress() {
		return fields.getState().equals(VisualStudioTaskState.IN_PROGRESS.value);
	}
	
	@JsonIgnore	
	public boolean isStateDone() {
		return fields.getState().equals(VisualStudioTaskState.DONE.value);
	}
	
	public String getType() {
		return fields.getType();
	}
}
