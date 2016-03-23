package insynctive.support.utils;

public enum InsynctiveVSEnvironment {

	LOCALHOST(
			1,
			"insynctive-test",
			"Engineering in Productivity Team"
			),
	
	PRODUCTION(
			2,
			"insynctive",
			"insynctive"
			),
	TEST(
			3,
			"insynctive-test",
			"Engineering in Productivity Team"
			)
	;
	
	private final Integer propertyNumber;
	private final String vsProject;
	private final String vsAccount;
	
	private InsynctiveVSEnvironment(Integer propertyNumber, String vsAccount, String vsProject) {
		this.propertyNumber = propertyNumber;
		this.vsAccount = vsAccount;
		this.vsProject = vsProject;
	}

	public Integer getPropertyNumber() {
		return propertyNumber;
	}

	public String getVsProject() {
		return vsProject;
	}

	public String getVsAccount() {
		return vsAccount;
	}
	
	public static InsynctiveVSEnvironment findByProperyNumber(Integer number) {
		for(InsynctiveVSEnvironment environment : values()){
			if(environment.propertyNumber.equals(number)){
				return environment;
			}
		}
		return null;
	}
	
}
