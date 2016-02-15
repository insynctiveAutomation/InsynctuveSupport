package insynctive.support.form.intercom;

public enum IntercomTopic {

	CONVERSATION_CREATED("conversation.user.created");
	
	private String value;
	
	private IntercomTopic(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
