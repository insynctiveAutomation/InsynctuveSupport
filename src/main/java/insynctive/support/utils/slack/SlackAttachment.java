package insynctive.support.utils.slack;

import java.util.ArrayList;
import java.util.List;

public class SlackAttachment {

	private String fallback;
	private String pretext;
	private String color;
	private List<SlackField> fields = new ArrayList<>();
	
	public String getFallback() {
		return fallback;
	}
	public void setFallback(String fallback) {
		this.fallback = fallback;
	}
	public String getPretext() {
		return pretext;
	}
	public void setPretext(String pretext) {
		this.pretext = pretext;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public List<SlackField> getFields() {
		return fields;
	}
	public void setFields(List<SlackField> fields) {
		this.fields = fields;
	}
	
	public void addField(SlackField field){
		fields.add(field);
	}
}
