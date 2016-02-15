package insynctive.support.form.vs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VisualStudioIterationValueForm {

	@JsonProperty("id")
	private String id;
	
	@JsonProperty("name")
	private String name;

	@JsonProperty("path")
	private String path;
	
	@JsonProperty("url")
	private String url;
	
	public VisualStudioIterationValueForm() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
