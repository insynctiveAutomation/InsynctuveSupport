package insynctive.support.utils.vs;

public enum VisualStudioBugState {

	TO_DO("To Do"),
	APPROVED("Approved"),
	IN_PROGRESS("In Progress"),
	DONE("Done");
	
	public String value;
	
	private VisualStudioBugState(String value) {
		this.value = value;
	}
}
