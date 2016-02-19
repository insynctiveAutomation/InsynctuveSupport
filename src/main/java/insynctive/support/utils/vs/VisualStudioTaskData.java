package insynctive.support.utils.vs;

public enum VisualStudioTaskData {
	
	INVESTIGATE_BUG("Investigate bug", "0.5"), 
	TEST_STRATEGY("Define test strategy", "0.5"), 
	ADD_ACCEPTANCE_CRITERIA("Add acceptance criteria", ""), 
	CREATE_A_NEW_BRANCH("Create a new branch", ""), 
	REPRODUCE_WITH_AUTOMATED_TESTS("Reproduce with automated tests", "1"), 
	DEVELOP_FIX("Develop Fix", "1"), 
	GET_CODE_REVIEW("Get code review", ""),
	FUNCTIONAL_TEST("Functional test", "0.5"), 
	MERGE_TO_MASTER("Merge to master", "0.5"), 
	REBASE_INTEGRATION_TO_MASTER("Rebase Integration to master", "0.5"), 
	TEST_ON_MASTER("Test on master", "0.5");
	
	public final String value;
	public final String defaultEstimate;
	
	private VisualStudioTaskData(String value, String defaultEstimate){
		this.value = value;
		this.defaultEstimate = defaultEstimate;
	}
	
}
