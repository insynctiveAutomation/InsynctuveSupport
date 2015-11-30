package insynctive.support.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListOfItems {
	 
	@JsonProperty("Items")
	private List<TargetProcessItem> items;

	public List<TargetProcessItem> getItems() {
		return items;
	}

	public void setItems(List<TargetProcessItem> items) {
		this.items = items;
	}
}
