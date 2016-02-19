package insynctive.support.utils.vs;

public enum VisualStudioTaskName {
	
	CREATE_A_NEW_BRANCH("Create a new branch"), 
	INVESTIGATE_BUG("Investigate bug"), 
	TEST_STRATEGY("Define test strategy"), 
	ADD_ACCEPTANCE_CRITERIA("Add acceptance criteria"), 
	REPRODUCE_WITH_AUTOMATED_TESTS("Reproduce with automated tests"), 
	DEVELOP_FIX("Develop Fix"), 
	GET_CODE_REVIEW("Get code review"),
	FUNCTIONAL_TEST("Functional test"), 
	MERGE_TO_MASTER("Merge to master"), 
	REBASE_INTEGRATION_TO_MASTER("Rebase Integration to master"), 
	TEST_ON_MASTER("Test on master");
	
	public final String value;
	
	private VisualStudioTaskName(String value){
		this.value = value;
	}
	
}
