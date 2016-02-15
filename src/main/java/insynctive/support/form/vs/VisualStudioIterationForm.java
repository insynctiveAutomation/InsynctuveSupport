package insynctive.support.form.vs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualStudioIterationForm {

	@JsonProperty("count")
	private String count;
	
	@JsonProperty("value")
	private List<VisualStudioIterationValueForm> value;

	public VisualStudioIterationForm() {
		// TODO Auto-generated constructor stub
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public List<VisualStudioIterationValueForm> getValue() {
		return value;
	}

	public void setValue(List<VisualStudioIterationValueForm> value) {
		this.value = value;
	}
	
	@JsonIgnore
	public String getPath(){
		return (value.size() > 0) ? value.get(0).getPath() : ""; 
	}
	
	@JsonIgnore
	public String getName(){
		return (value.size() > 0) ? value.get(0).getName() : "";
	}
	
}
