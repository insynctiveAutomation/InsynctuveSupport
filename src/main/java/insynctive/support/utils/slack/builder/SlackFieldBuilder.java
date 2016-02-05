package insynctive.support.utils.slack.builder;

import insynctive.support.utils.slack.SlackField;

public class SlackFieldBuilder {

	SlackField slackField;
	
	public SlackFieldBuilder() {
		slackField = new SlackField();
	}
	
	public SlackFieldBuilder setTitle(String title){
		slackField.setTitle(title);
		return this;
	}
	
	public SlackFieldBuilder setValue(String value){
		slackField.setValue(value);
		return this;
	}
	
	public SlackField build(){
		return slackField;
	}
	
}
