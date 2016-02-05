package insynctive.support.utils.vs;

import org.json.JSONObject;

public class VisualStudioRelationObject {

	private String rel;
	private String url;
	
	public VisualStudioRelationObject(String rel, String url) {
		this.rel = rel;
		this.url = url;
	}

	public String getRel() {
		return rel;
	}
	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public JSONObject asJson(){
		JSONObject relationObject = new JSONObject();
		relationObject.put("rel", rel);
		relationObject.put("url", url);
		return relationObject;
	}
}
