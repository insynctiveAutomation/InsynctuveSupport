package insynctive.support.utils.vs;

import java.util.ArrayList;
import java.util.List;

public class VisualStudioWorkItem {

	private List<VisualStudioField> fields = new ArrayList<>();
	private List<VisualStudioRelation> relations = new ArrayList<>();
	
	public List<VisualStudioField> getFields() {
		return fields;
	}

	public void setFields(List<VisualStudioField> fields) {
		this.fields = fields;
	}

	public List<VisualStudioRelation> getRelations() {
		return relations;
	}

	public void setRelations(List<VisualStudioRelation> relations) {
		this.relations = relations;
	}

	public void addVisualStudioField(VisualStudioField vsField){
		fields.add(vsField);
	}
	
	public void addVisualStudioRelation(VisualStudioRelation vsRelation){
		relations.add(vsRelation);
	}
}
