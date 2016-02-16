package insynctive.support.utils.vs;

public enum VisualStudioTaskState {
	
	TO_DO("To Do"), REMOVED("Removed"), IN_PROGRESS("In Progress"), DONE("Done");
    
    public final String value;
	
	private VisualStudioTaskState(String value) {
		this.value = value;
	}
}
