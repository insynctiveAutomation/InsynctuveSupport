package insynctive.support.form.intercom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true )
public class IntercomData {

	@JsonProperty("item")
	private IntercomDataItem item;
	
	public IntercomData() {
	}

	public IntercomDataItem getItem() {
		return item;
	}

	public void setItem(IntercomDataItem item) {
		this.item = item;
	}
	
}
