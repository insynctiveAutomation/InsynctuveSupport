package insynctive.support.utils.vs;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VisualStudioRelation {
	
	private String op;
	private String path;
	private VisualStudioRelationObject value;
	
	@JsonIgnore
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public VisualStudioRelation(String op, String path, VisualStudioRelationObject value) {
		this.op = op;
		this.path = path;
		this.value = value;
	}
	
	public VisualStudioRelation(String op, String path, String account, String relationID) {
		this.op = op;
		this.path = "/relations/-";
		this.value = new VisualStudioRelationObject("System.LinkTypes.Hierarchy-Reverse", "https://"+account+".visualstudio.com/DefaultCollection/_apis/wit/workItems/"+relationID);
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
	
	public VisualStudioRelationObject getValue() {
		return value;
	}
	public void setValue(VisualStudioRelationObject value) {
		this.value = value;
	}
	
	public JSONObject asJson() {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("op", op);
		jsonObj.put("path", path);
		jsonObj.put("value", value.asJson());
		return jsonObj;
	}
	
}
