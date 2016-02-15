package insynctive.support.form.intercom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.intercom.api.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IntercomDataItem {

	@JsonProperty("type")
	private String type;
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("user")
	private User user;
	
	@JsonProperty("conversation_message")
	private ConversationMessage conversationMessage;
	
	@JsonProperty("open")
	private boolean open;
	
	@JsonProperty("read")
	private boolean read;
	
	@JsonProperty("links")
	private IntercomLinks links;

	
	public IntercomDataItem() {
		// TODO Auto-generated constructor stub
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean isOpen() {
		return open;
	}

	public void setOpen(Boolean open) {
		this.open = open;
	}

	public Boolean isRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public IntercomLinks getLinks() {
		return links;
	}

	public void setLinks(IntercomLinks links) {
		this.links = links;
	}

	public ConversationMessage getConversationMessage() {
		return conversationMessage;
	}

	public void setConversationMessage(ConversationMessage conversationMessage) {
		this.conversationMessage = conversationMessage;
	}
}
