package insynctive.support.utils.slack;

import insynctive.support.utils.vs.VisualStudioTaskData;

public enum SlackMessage {
	
	//Visual Studio Work Item Updated Messages
	BUG_APPROVED(
			":bug:", 
			"Visual Studio Support", 
			"<%s | Bug #%d> - Bug has been approved and is ready for \""+VisualStudioTaskData.INVESTIGATE_BUG.value+"\"."),

	INVESTIGATE_BUG_DONE(
			":bug:", 
			"Visual Studio Support", 
			"<%s | Bug #%d> - Bug has been investigated and is ready for \""+VisualStudioTaskData.TEST_STRATEGY.value+"\"."),
	
	TEST_STRATEGY_DONE(
			":bug:", 
			"Visual Studio Support", 
			"<%s | Bug #%d> - "+VisualStudioTaskData.TEST_STRATEGY.value+" has been agreed, you can start working on this bug."),
	
	DEVELOP_FIX_DONE(
			":bug:", 
			"Visual Studio Support", 
			"<%s | Bug #%d> - Bug has been fixed, this is ready for testing."),
	
	FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE(
			":bug:",
			"Visual Studio Support", 
			"<%s | Bug #%d> - Bug was tested and Code was reviewed and approved. You can "+VisualStudioTaskData.MERGE_TO_MASTER.value+"."),
	
	FUNCTIONAL_TEST_DONE_GET_CODE_REVIEW_NOT(
			":bug:",
			"Visual Studio Support", 
			"<%s | Bug #%d> - "+VisualStudioTaskData.MERGE_TO_MASTER.value+" task is waiting for you to complete Pull Request."),
	
	GET_CODE_REVIEW_DONE_FUNCTIONAL_TEST_NOT(
			":bug:",
			"Visual Studio Support", 
			"<%s | Bug #%d> - "+VisualStudioTaskData.MERGE_TO_MASTER.value+" task is waiting for you to complete Functional Test."),
	
	MERGE_TO_MASTER_DONE(
			":bug:", 
			"Visual Studio Support", 
			"<%s | Bug #%d> - Bug has been merged to master and is ready for you to Test on master."),
	
	
	NEW_ASSIGNED_TO_WORK_ITEM(
			":point_right:", 
			"Visual Studio Support", 
			"<%s | %s #%d> has just been assigned to you"),
	
	ASSIGNED_TO_WORK_ITEM(
			":point_right:", 
			"Visual Studio Support", 
			"<%s | %s #%d> has been assigned from %s to you");
	
	public String img;
	public String senderName; 
	public String message;
	
	private SlackMessage(String img, String senderName, String message) {
		this.img = img;
		this.senderName = senderName;
		this.message = message;
	}
	
}
