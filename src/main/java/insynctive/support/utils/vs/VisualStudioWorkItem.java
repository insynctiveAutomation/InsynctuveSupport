package insynctive.support.utils.vs;

import java.util.ArrayList;
import java.util.List;

public class VisualStudioWorkItem {

	List<VisualStudioField> fields = new ArrayList<>();
	List<VisualStudioRelation> relations = new ArrayList<>();
	private VisualStudioWorkItem self;
	
	public void addVisualStudioField(VisualStudioField vsField){
		fields.add(vsField);
	}
	
	public void addVisualStudioRelation(VisualStudioRelation vsRelation){
		relations.add(vsRelation);
	}
}
