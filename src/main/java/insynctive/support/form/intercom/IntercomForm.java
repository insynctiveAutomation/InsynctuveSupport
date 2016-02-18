package insynctive.support.form.intercom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true )
public class IntercomForm {

	@JsonProperty("data")
	private IntercomData data;

	@JsonProperty("topic")
	private String topic;
	
	public IntercomForm() {
		// TODO Auto-generated constructor stub
	}

	public IntercomData getData() {
		return data;
	}

	public void setData(IntercomData data) {
		this.data = data;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@JsonIgnore
	public String getItemID() {
		return getData().getItem().getId();
	}

	@JsonIgnore
	public IntercomDataItem getConversation() {
		return getData().getItem();
	}

	@JsonIgnore
	public String getConversationUrl() {
		return getData().getItem().getLinks().getConversationWeb();
	}
	
	@JsonIgnore
	public boolean isAConversationCreation(){
		return topic.equals(IntercomTopic.CONVERSATION_CREATED.getValue());
	}
	
}
