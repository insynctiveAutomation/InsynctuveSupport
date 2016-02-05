package insynctive.support.utils.vs;

public class VisualStudioRelation {
	
	private String op;
	private String path;
	private VisualStudioRelationObject value;
	
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
	
}
