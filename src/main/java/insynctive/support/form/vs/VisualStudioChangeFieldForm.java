package insynctive.support.form.vs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualStudioChangeFieldForm {

	/**
	 *	- System.State ("Approved" - "New")
	 *		- oldValue
	 *		- newValue
	 * */
	
	@JsonProperty("System.State")
	private VisualStudioChangeValue state;
	
	public VisualStudioChangeValue getState() {
		return state;
	}

	public void setState(VisualStudioChangeValue state) {
		this.state = state;
	}
	
	//Methods
	@JsonIgnore
	public boolean isApproved(){
		return state != null && state.getNewValue().toLowerCase().equals("approved");
	}
	
	@JsonIgnore
	public boolean wasApproved(){
		return state != null && state.getOldValue().toLowerCase().equals("approved");
	}

	@JsonIgnore
	public boolean isDone(){
		return state != null && state.getNewValue().toLowerCase().equals("done");
	}

	@JsonIgnore
	public boolean wasDone(){
		return state != null && state.getOldValue().toLowerCase().equals("done");
	}

	@JsonIgnore
	public boolean isInProgress(){
		return state != null && state.getNewValue().toLowerCase().equals("in progress");
	}

	@JsonIgnore
	public boolean wasInProgress(){
		return state != null && state.getOldValue().toLowerCase().equals("in progress");
	}

	@JsonIgnore
	public boolean isTodo() {
		return state != null && state.getNewValue().toLowerCase().equals("to do");
	}

	@JsonIgnore
	public boolean wasTodo() {
		return state != null && state.getOldValue().toLowerCase().equals("to do");
	}
	
}
