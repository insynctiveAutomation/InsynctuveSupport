package insynctive.support.utils.victorops;

public enum VIctorOpsMessageType {

	INFO("INFO"), 
	WARNING("WARNING"), 
	ACKNOWLEDGEMENT("ACKNOWLEDGEMENT"), 
	CRITICAL("CRITICAL"), 
	RECOVERY("RECOVERY");
	
	private String value;
	
	private VIctorOpsMessageType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
