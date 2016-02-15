package insynctive.support.form.slack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackForm {

	@JsonProperty("channel")
	private String channel;
	
	public SlackForm() {
		// TODO Auto-generated constructor stub
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	
}
