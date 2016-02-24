package insynctive.support.utils.vs.builder;

import insynctive.support.utils.vs.VisualStudioField;
import insynctive.support.utils.vs.VisualStudioRelation;
import insynctive.support.utils.vs.VisualStudioTaskState;
import insynctive.support.utils.vs.VisualStudioWorkItem;

import org.springframework.beans.factory.annotation.Autowired;

import insynctive.support.utils.Property;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;
import insynctive.support.utils.vs.VisualStudioBugState;

public class VisualStudioWorkItemBuilder {

	private VisualStudioWorkItem item;
	
	public VisualStudioWorkItemBuilder(){
		item = new VisualStudioWorkItem();
	}
	
	public VisualStudioWorkItemBuilder addTitle(String title){
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.Title", title));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedTitle(String title){
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/System.Title", title));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addIteration(String iteration){
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.IterationPath", iteration));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedIteration(String iteration){
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/System.IterationPath", iteration));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addStatus(String status){
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.State", status));
		return this;
	}

	public VisualStudioWorkItemBuilder addStatus(VisualStudioTaskState status) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.State", status.value));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addStatus(VisualStudioBugState status) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.State", status.value));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedStatus(String status){
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/System.State", status));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedStatus(VisualStudioTaskState status){
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/System.State", status.value));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedStatus(VisualStudioBugState status){
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/System.State", status.value));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addAssignTo(String employee){
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.AssignedTo", employee));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedAssignTo(String employee){
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/System.AssignedTo", employee));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addParent(String relationID, String account) throws Exception {
		item.addVisualStudioRelation(new VisualStudioRelation("add", "/relations/-", account, relationID));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedParent(String relationID, String account) throws Exception {
		item.addVisualStudioRelation(new VisualStudioRelation("replace", "/relations/-", account, relationID));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addVisualStudioField(VisualStudioField field){
		item.addVisualStudioField(field);
		return this;
	}
	
	public VisualStudioWorkItemBuilder addVisualStudioRelation(VisualStudioRelation relation){
		item.addVisualStudioRelation(relation);
		return this;
	}

	public VisualStudioWorkItemBuilder addEstimate(String time) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/Microsoft.VSTS.Scheduling.RemainingWork", time));
		return this;
	}

	public VisualStudioWorkItemBuilder modifiedEstimate(String time) {
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/Microsoft.VSTS.Scheduling.RemainingWork", time));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addIntercomConversation(String conversation) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/insynctive.InsynctiveSCRUM.IntercomConversaton", conversation));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedIntercomConversation(String conversation) {
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/insynctive.InsynctiveSCRUM.IntercomConversaton", conversation));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addIsIncident(Boolean isIncident) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/insynctive.InsynctiveSCRUM.Incident", isIncident ? "YES" : "NO"));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedIsIncident(Boolean isIncident) {
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/insynctive.InsynctiveSCRUM.Incident", isIncident ? "YES" : "NO"));
		return this;
	}

	public VisualStudioWorkItemBuilder addCreatedBy(String name) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.CreatedBy", name));
		return this;
	}

	public VisualStudioWorkItemBuilder modifiedCreatedBy(String name) {
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/System.CreatedBy", name));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addDescription(String description) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.Description", description));
		return this;
	}
		
	public VisualStudioWorkItemBuilder modifiedDescription(String description) {
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/System.Description", description));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addReproSteps(String description) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/Microsoft.VSTS.TCM.ReproSteps", description));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedReproSteps(String description) {
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/Microsoft.VSTS.TCM.ReproSteps", description));
		return this;
	}
	
	public VisualStudioWorkItem build(){
		return item;
	}
}
