package insynctive.support.utils.vs.builder;

import insynctive.support.utils.vs.VisualStudioField;
import insynctive.support.utils.vs.VisualStudioRelation;
import insynctive.support.utils.vs.VisualStudioWorkItem;

public class VisualStudioWorkItemBuilder {

	private VisualStudioWorkItem item;
	
	public VisualStudioWorkItemBuilder(){
		item = new VisualStudioWorkItem();
	}
	
	public VisualStudioWorkItemBuilder addTitle(String title){
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.Title", title));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addIteration(String iteration){
		item.addVisualStudioField(new VisualStudioField("add", "/fields/System.IterationPath", iteration));
		return this;
	}
	
	public VisualStudioWorkItemBuilder addParent(String relationID){
		item.addVisualStudioRelation(new VisualStudioRelation("add", "/relations/-", "insynctive", relationID));
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
