package insynctive.support.utils.vs;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VisualStudioField {

	private String op;
	private String path;
	private String stringvalue;
	
	public VisualStudioField(String op, String path, String value) {
		this.op = op;
		this.path = path;
		this.stringvalue = value;
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

	public String getStringvalue() {
		return stringvalue;
	}

	public void setStringvalue(String stringvalue) {
		this.stringvalue = stringvalue;
	}

	@JsonIgnore
	public JSONObject asJson(){
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("op", op);
		jsonObj.put("path", path);
		jsonObj.put("value", stringvalue);
		return jsonObj;
	}
}
