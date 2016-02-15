package insynctive.support.utils.vs;

public enum VisualStudioBugState {
   
	APPROVED("Approved"), COMMITTED("Committed"), DONE("Done"), NEW("New"), REMOVED("Removed");
    
    public final String value;
	
	private VisualStudioBugState(String value) {
		this.value = value;
	}
}
