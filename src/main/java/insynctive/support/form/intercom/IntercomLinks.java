package insynctive.support.form.intercom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntercomLinks {

	@JsonProperty("conversation_web")
	private String conversationWeb;
	
	public IntercomLinks() {
		// TODO Auto-generated constructor stub
	}

	public String getConversationWeb() {
		return conversationWeb;
	}

	public void setConversationWeb(String conversationWeb) {
		this.conversationWeb = conversationWeb;
	}
	
	
	
}
