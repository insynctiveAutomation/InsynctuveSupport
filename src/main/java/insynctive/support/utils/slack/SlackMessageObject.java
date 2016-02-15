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
	
	
	//TEST
	public static void main(String[] args) throws IOException {
		String name = "name example";
		
		String fullUrl = "https://www.google.com";
		String phase = "phase";
		
		String user = "evaleiras@insynctive.com";
		String branch = "branchName";
		String node = "node example";
		String account = "alpha";
		String version = "5.0.000.0000";
		
		String message = "Main Message";
		SlackMessageObject Slackmessage = new SlackMessageBuilder()
				.setUsername("Jenkins Install - Success Messenger")
				.setText(message)
				.setIconEmoji(":sunglasses:")
				.setChannel("@eugeniovaleiras")
				.addAttachment(new SlackAttachmentBuilder()
						.setPretext("pretext")
						.setColor("#00CE00")
						.setFallback("fallback")
						.addField(new SlackFieldBuilder().setTitle("title 1").setValue("value 1").build())
						.build())
				.addAttachment(new SlackAttachmentBuilder()
						.setPretext("pretext")
						.setColor("#00CE00")
						.setFallback("fallback")
						.addField(new SlackFieldBuilder().setTitle("title 2.0").setValue("value 2.0").build())
						.addField(new SlackFieldBuilder().setTitle("title 2.1").setValue("value 2.1").build())
						.build())
				.build();
			
		SlackUtil.sendMessage(Slackmessage);
	}
}
