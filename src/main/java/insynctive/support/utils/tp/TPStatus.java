package insynctive.support.utils.tp;

public enum TPStatus {
    OPEN(53, "Open"),
    CANT_NOT_REPRODUCE(220, "Can Not Reproduce"),
    NEED_DISCUSSION(223, "Need Discussion"),
    FIXING_IN_PROGRESS(98, "Fixing In Progress"),
    RESOLVED(55, "Resolved"),
    TESTING_IN_PROGRESS(99, "Testing In Progress"),
    RE_OPEN(82, "Re-Open"),
    TRACKED_BY_QA(134, "Tracked by QA"),
    READY_FOR_DEPLOYMENT(127, "Ready for Deployment"),
    OBSOLETE(221, "Obsolete"),
    DONE(56, "Done");
	
	private final Integer id;
	private final String value;
	
	private TPStatus(Integer id, String value){
		this.id = id;
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public static Integer getID(String value){
		for(TPStatus status : TPStatus.values()){
			if(status.getValue().equals(value)){
				return status.id;
			}
		}
		return null;
	}
}