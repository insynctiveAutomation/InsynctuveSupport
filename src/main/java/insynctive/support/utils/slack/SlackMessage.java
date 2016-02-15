package insynctive.support.utils.slack;

import insynctive.support.utils.vs.VisualStudioTaskName;

public enum SlackMessage {
	
	//Visual Studio Work Item Updated Messages
	BUG_APPROVED(
			":bug:", 
			"Visual Studio Support", 
			"<%s | Bug #%d> - Bug has been approved and is ready for \""+VisualStudioTaskName.TEST_STRATEGY.value+"\"."),
	
	TEST_STRATEGY_DONE(
			":bug:", 
			"Visual Studio Support", 
			"<%s | Bug #%d> - "+VisualStudioTaskName.TEST_STRATEGY.value+" has been agreed, you can start working on this bug."),
	
	DEVELOP_FIX_DONE(
			":bug:", 
			"Visual Studio Support", 
			"<%s | Bug #%d> - Bug has been fixed, this is ready for testing."),
	
	FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE(
			":bug:",
			"Visual Studio Support", 
			"<%s | Bug #%d> - Bug was tested and Code was reviewed and approved. You can "+VisualStudioTaskName.MERGE_TO_MASTER.value+"."),
	
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
