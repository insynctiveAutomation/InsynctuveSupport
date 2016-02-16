package insynctive.support.utils.vs;

public enum VisualStudioTag {

	CRITICAL("Critical");
	
	private String value;
	
	private VisualStudioTag(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
