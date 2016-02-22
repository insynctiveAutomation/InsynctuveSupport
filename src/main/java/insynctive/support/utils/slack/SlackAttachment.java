package insynctive.support.utils.slack;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackAttachment {

	@JsonProperty("fallback")
	private String fallback;
	
	@JsonProperty("pretext")
	private String pretext;
	
	@JsonProperty("color")
	private String color;
	
	@JsonProperty("fields")
	private List<SlackField> fields = new ArrayList<>();
	
	public SlackAttachment() {
		// TODO Auto-generated constructor stub
	}
	
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
