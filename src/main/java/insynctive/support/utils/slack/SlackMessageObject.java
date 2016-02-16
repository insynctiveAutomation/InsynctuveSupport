package insynctive.support.utils.slack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import insynctive.support.utils.slack.builder.SlackAttachmentBuilder;
import insynctive.support.utils.slack.builder.SlackFieldBuilder;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;

public class SlackMessageObject {

	private String url = "https://hooks.slack.com/services/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";
	private String iconEmoji = ":ghost:";
	private String username = "";
	private String text;
	private String channel;
	private List<SlackAttachment> attachments = new ArrayList<>();
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getIconEmoji() {
		return iconEmoji;
	}
	public void setIconEmoji(String iconEmoji) {
		this.iconEmoji = iconEmoji;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public List<SlackAttachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<SlackAttachment> attachments) {
		this.attachments = attachments;
	}
	
	public void addAttachment(SlackAttachment attachment){
		attachments.add(attachment);
	}
}
