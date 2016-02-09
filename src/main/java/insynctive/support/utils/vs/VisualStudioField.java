package insynctive.support.utils.vs;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VisualStudioField {

	private String op;
	private String path;
	private String value;
	
	public VisualStudioField(String op, String path, String value) {
		this.op = op;
		this.path = path;
		this.value = value;
	}
	
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	@JsonIgnore
	public JSONObject asJson(){
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("op", op);
		jsonObj.put("path", path);
		jsonObj.put("value", value);
		return jsonObj;
	}
}
