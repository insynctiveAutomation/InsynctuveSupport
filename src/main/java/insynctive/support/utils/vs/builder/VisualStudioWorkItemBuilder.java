package insynctive.support.utils.vs.builder;

import insynctive.support.utils.vs.VisualStudioField;
import insynctive.support.utils.vs.VisualStudioRelation;
import insynctive.support.utils.vs.VisualStudioTaskState;
import insynctive.support.utils.vs.VisualStudioWorkItem;
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
	
	public VisualStudioWorkItemBuilder addParent(String relationID){
		item.addVisualStudioRelation(new VisualStudioRelation("add", "/relations/-", "insynctive", relationID));
		return this;
	}
	
	public VisualStudioWorkItemBuilder modifiedParent(String relationID){
		item.addVisualStudioRelation(new VisualStudioRelation("replace", "/relations/-", "insynctive", relationID));
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
	
	public VisualStudioWorkItem build(){
		return item;
	}
}
