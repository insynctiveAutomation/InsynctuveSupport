package insynctive.support.utils.vs;

public enum VisualStudioWorkItemState {
   
	APPROVED("Approved"), COMMITTED("Committed"), DONE("Done"), NEW("New"), REMOVED("Removed");
    
    public final String value;
	
	private VisualStudioWorkItemState(String value) {
		this.value = value;
	}
}
