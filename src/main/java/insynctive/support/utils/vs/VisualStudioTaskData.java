package insynctive.support.utils.vs;

public enum VisualStudioTaskData {
	
	CREATE_A_NEW_BRANCH("Create a new branch", ""), 
	REBASE_INTEGRATION_TO_MASTER("Rebase Integration to master", "0.5"), 

	//Individual Bug Task
	INVESTIGATE_BUG("Investigate bug", "0.5"), 
	TEST_STRATEGY_BUG("Define test strategy", "0.5"), 
	ADD_ACCEPTANCE_CRITERIA("Add acceptance criteria", ""), 
	REPRODUCE_WITH_AUTOMATED_TESTS("Reproduce with automated tests", "1"), 
	DEVELOP_FIX("Develop Fix", "1"), 
	GET_CODE_REVIEW("Get code review", ""),
	MERGE_TO_MASTER_BUG("Merge to master", "0.5"), 
	FUNCTIONAL_TEST("Functional test", "0.5"), 
	TEST_ON_MASTER_BUG("Test on master", "0.5"),
	
	//Story Task
	ESTIMATE_STORY("Estimate Story", "1"),
	TEST_STRATEGY_STORY("Testing strategy", "4"), 
	DEVELOP_TDD_INTEGRATION_TESTS("Develop TDD Unit/Integration Tests for Story", "4"),
	DEVELOP_CODE_FOR_STORY("Develop Code for Story", "6"),
	POST_STORY_MOVIE("Post Story Movie", "0.5"),
	APPROVE_STORY_MOVIE("Approve story movie", ""),
	PULL_REQUEST_FOR_STORY("Pull Request for Story", "0.5"),
	FUNCTIONAL_TEST_ON_INTEGRATION("Functional Test on integration", "2"),
	UI_AUTOMATED_TESTING("UI aTesting", "6"),
	MERGE_TO_MASTER_STORY("Merge to Master", "1"),
	TEST_ON_MASTER_STORY("Test on Master", "1"),
	APPROVE_FOR_RELEASE("Approve for Release", "");
	
	public final String value;
	public final String defaultEstimate;
	
	private VisualStudioTaskData(String value, String defaultEstimate){
		this.value = value;
		this.defaultEstimate = defaultEstimate;
	}
	
}
