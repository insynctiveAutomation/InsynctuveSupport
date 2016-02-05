package insynctive.support.utils.slack.builder;

import insynctive.support.utils.slack.SlackAttachment;
import insynctive.support.utils.slack.SlackMessage;

public class SlackMessageBuilder {

	SlackMessage slackMessage;
	
	public SlackMessageBuilder() {
		slackMessage = new SlackMessage(); 
	}
	
	public SlackMessageBuilder setUrl(String url){
		slackMessage.setUrl(url);
		return this;
	}
	
	public SlackMessageBuilder setIconEmoji(String iconEmoji){
		slackMessage.setIconEmoji(iconEmoji);
		return this;
	}
	
	public SlackMessageBuilder setUsername(String username){
		slackMessage.setUsername(username);
		return this;
	}
	
	public SlackMessageBuilder setText(String text){
		slackMessage.setText(text);
		return this;
	}
	
	public SlackMessageBuilder setChannel(String channel){
		slackMessage.setChannel(channel);
		return this;
	}
	
	public SlackMessageBuilder addAttachment(SlackAttachment attachment){
		slackMessage.addAttachment(attachment);
		return this;
	}
	
	public SlackMessage build(){
		return slackMessage;
	}
}
