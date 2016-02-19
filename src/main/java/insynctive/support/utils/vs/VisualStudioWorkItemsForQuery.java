package insynctive.support.utils.vs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import insynctive.support.form.vs.VisualStudioRevisionForm;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualStudioWorkItemsForQuery {

	@JsonProperty("workItems")
	private List<VisualStudioRevisionForm> workItems;

	public List<VisualStudioRevisionForm> getWorkItems() {
		return workItems;
	}

	public void setWorkItems(List<VisualStudioRevisionForm> workItems) {
		this.workItems = workItems;
	}
	
	public Integer countWorkItems(){
		return workItems.size();
	}
	
	
}
