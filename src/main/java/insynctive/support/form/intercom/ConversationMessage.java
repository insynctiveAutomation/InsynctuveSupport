package insynctive.support.form.intercom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversationMessage {

	@JsonProperty("type")
    private final String type = "conversation_message";

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("body")
    private String body;

    @JsonProperty("author")
    private IntercomAuthor author;

    public ConversationMessage() {
    }

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public IntercomAuthor getAuthor() {
		return author;
	}

	public void setAuthor(IntercomAuthor author) {
		this.author = author;
	}

	public String getType() {
		return type;
	}

}
