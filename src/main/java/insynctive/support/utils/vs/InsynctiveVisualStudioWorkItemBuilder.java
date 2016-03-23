package insynctive.support.utils.vs;

import support.utils.vs.VisualStudioField;
import support.utils.vs.builder.VisualStudioWorkItemBuilder;

public class InsynctiveVisualStudioWorkItemBuilder extends VisualStudioWorkItemBuilder {
	
	public InsynctiveVisualStudioWorkItemBuilder() {
		super();
	}
	
	public InsynctiveVisualStudioWorkItemBuilder addIntercomConversation(String conversation) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/insynctive.InsynctiveSCRUM.IntercomConversaton", conversation));
		return this;
	}
	
	public InsynctiveVisualStudioWorkItemBuilder modifiedIntercomConversation(String conversation) {
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/insynctive.InsynctiveSCRUM.IntercomConversaton", conversation));
		return this;
	}
	
	public InsynctiveVisualStudioWorkItemBuilder addIsIncident(Boolean isIncident) {
		item.addVisualStudioField(new VisualStudioField("add", "/fields/insynctive.InsynctiveSCRUM.Incident", isIncident ? "YES" : "NO"));
		return this;
	}
	
	public InsynctiveVisualStudioWorkItemBuilder modifiedIsIncident(Boolean isIncident) {
		item.addVisualStudioField(new VisualStudioField("replace", "/fields/insynctive.InsynctiveSCRUM.Incident", isIncident ? "YES" : "NO"));
		return this;
	}
	
}
