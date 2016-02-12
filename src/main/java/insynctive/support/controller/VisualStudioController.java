package insynctive.support.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.support.dao.WorkItemDao;
import insynctive.support.form.vs.VisualStudioForm;
import insynctive.support.form.vs.VisualStudioRevisionForm;
import insynctive.support.model.VisualStudioWorkItemEntity;
import insynctive.support.utils.VisualStudioUtil;
import insynctive.support.utils.slack.SlackMessage;
import insynctive.support.utils.slack.SlackMessages;
import insynctive.support.utils.slack.SlackUtil;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;
import insynctive.support.utils.vs.VisualStudioTaskName;
import insynctive.support.utils.vs.VisualStudioTaskState;
import insynctive.support.utils.vs.VisualStudioWorkItem;
import insynctive.support.utils.vs.VisualStudioWorkItemState;
import insynctive.support.utils.vs.builder.VisualStudioWorkItemBuilder;

@Controller
@RequestMapping(value = "/vs")
public class VisualStudioController {

	private final WorkItemDao workItemDao;
	
	@Inject
	public VisualStudioController(WorkItemDao workItemDao) {
		this.workItemDao = workItemDao;
	}
	
	
	@RequestMapping(value = "/updateWorkItem/{account}" ,method = RequestMethod.POST)
	@ResponseBody
	public String restToState(@RequestBody VisualStudioForm workItemUpdated, @PathVariable String account) throws Exception{

		//Default return message
		String returnMessage = "Is Task: "+workItemUpdated.isATask();
		returnMessage += "- Is Bug: "+workItemUpdated.isABug();
		returnMessage += "- Is Develop Fix: "+workItemUpdated.isDevelopFix();
		returnMessage += "- Was Change to approved: "+workItemUpdated.wasChangeToApprooved();
		returnMessage += "- Was Change to In Progress: "+workItemUpdated.wasChangeToInProgress();
		returnMessage += "- Was Change to Done: "+workItemUpdated.wasChangeToDone();

		if(workItemUpdated.wasChangeAssignation()){
			alertAssignation(workItemUpdated, account);
		}
		
		//Manage Updated For Individual BUG
		if(workItemUpdated.isABug() && workItemUpdated.noHaveParent()) {
			returnMessage = manageUpdatedForIndividualBug(workItemUpdated, account, returnMessage);
		}
		
		//Manage Updated For TASK
		if(workItemUpdated.isATask()) {
			returnMessage = manageUpdatedForTask(workItemUpdated, account, returnMessage);
		}
		
		return "{\"status\" : 200, \"message\": \""+returnMessage+"\"}";
	}


	private void alertAssignation(VisualStudioForm workItemUpdated, String account) throws IOException, Exception {
		//Slack Assigned..
		String project = workItemUpdated.getProject();
		String text = workItemUpdated.getOldAssigned() != null ?
				String.format(SlackMessages.ASSIGNED_TO_WORK_ITEM.message, 
						VisualStudioUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), 
						workItemUpdated.getType(), 
						workItemUpdated.getWorkItemID(), 
						workItemUpdated.getOldAssignedName()) 
				:
				String.format(SlackMessages.NEW_ASSIGNED_TO_WORK_ITEM.message, 
						VisualStudioUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), 
						workItemUpdated.getType(), 
						workItemUpdated.getWorkItemID());
		
		SlackMessage message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessages.ASSIGNED_TO_WORK_ITEM.img)
			.setUsername(SlackMessages.ASSIGNED_TO_WORK_ITEM.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(workItemUpdated.getAssignedToEmail()))
			.setText(text)
			.build();
		SlackUtil.sendMessage(message);
	}


	//Manage Task Upadated
	private String manageUpdatedForTask(VisualStudioForm workItemUpdated, String account, String returnMessage) throws Exception {
		
		//Test Strategy was moved to DONE
		if(workItemUpdated.isTestStrategy() && workItemUpdated.wasChangeToDone()) {
			returnMessage = testStrategyDoneProcess(workItemUpdated, account);
		}
		
		//Create a New Branch was moved to DONE
		else if(workItemUpdated.isCreateANewBranch() && workItemUpdated.wasChangeToDone()){
			returnMessage = createNewBranchDoneProcess(workItemUpdated, account);
		}
		
		
		//Reproduce with automated tests was moved to DONE
		else if(workItemUpdated.isReproduceWithAutomatedTest() && workItemUpdated.wasChangeToDone()){
			returnMessage = reproduceWithAutomatedTestsDoneProcess(workItemUpdated, account);
		}
		
		//Develop Fix was moved to DONE - TODO Need to make the build automatically
		else if (workItemUpdated.isDevelopFix() && workItemUpdated.wasChangeToDone()){
			returnMessage = developFixDoneProcess(workItemUpdated, account);
		}
		
		//Get Code Review and functional Test were moved to DONE
		else if((workItemUpdated.isGetCodeReview() || workItemUpdated.isFunctionalTest() ) && workItemUpdated.wasChangeToDone()){

			String project = workItemUpdated.getProject();
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			Boolean isGetCodeReview = workItemUpdated.isGetCodeReview();
			Boolean isFunctionalTest = workItemUpdated.isFunctionalTest();
			
			if((isGetCodeReview && bugWorkItem.findFunctionalTest(account).isStateDone()) || (isFunctionalTest && bugWorkItem.findGetCodeReview(account).isStateDone())){
				
				returnMessage = getCodeReviewanFunctionalTestDoneProcess(account, project, bugWorkItem);
			}
		}
		
		//Merge to Master was moved to DONE - TODO Run Build 
		else if(workItemUpdated.isMergeToMaster() && workItemUpdated.wasChangeToDone()){
			returnMessage = mergeToMasterDoneProcess(workItemUpdated, account);
		}
		
		//Rebase Integration to Master and Test on Master were move to Done
		else if((workItemUpdated.isRebaseIntegrationToMaster() || workItemUpdated.isTestOnMaster())  && workItemUpdated.wasChangeToDone()){
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			Boolean isRebaseIntegrationToMaster = workItemUpdated.isRebaseIntegrationToMaster();
			Boolean isTestOnMaster = workItemUpdated.isTestOnMaster();
			
			if((isRebaseIntegrationToMaster && bugWorkItem.findDoneDoneTest(account).isStateDone()) || (isTestOnMaster && bugWorkItem.findRebaseIntegrationToMaster(account).isStateDone())){
				rebaseIntegrationToMasterAndTestOnMasterDoneProcess(workItemUpdated, account, bugWorkItem);
			}
		}
//		//Slack QA fort testing..
//		SlackMessage message = new SlackMessageBuilder()
//				.setIconEmoji(":bug:")
//				.setUsername("Visual Studio Support")
//				.setChannel(SlackUtil.getSlackAccountMentionByEmail(testBugWorkItem.getAssignedToEmail()))
//				.setText("<"+workItemUpdated.getFirstRelation().getEditURL()+"| Bug #"+workItemUpdated.getFirstRelation().getRelationID()+"> - Has been fixed and is ready for you to test it.")
//				.build();
//		SlackUtil.sendMessage(message);
		return returnMessage;
	}

	//Manage Bug Updated
	private String manageUpdatedForIndividualBug(VisualStudioForm workItemUpdated, String account, String returnMessage) throws Exception {
			
			//Check if the bug was change to Approved
			if(workItemUpdated.wasChangeToApprooved()){
				
				returnMessage = "Create Test Strategy - Slack to QA. ";
				String project = workItemUpdated.getProject();

				VisualStudioWorkItem testStrategyWorkItem = new VisualStudioWorkItemBuilder()
					.addParent(String.valueOf(workItemUpdated.getWorkItemID()))
					.addTitle(VisualStudioTaskName.TEST_STRATEGY.value)
					.addStatus(VisualStudioTaskState.TO_DO)
					.addIteration(workItemUpdated.getIteration())
					.addAssignTo(workItemUpdated.getCreatedBy())
					.build();
				
				
				//Check if the task were not created.
				if(workItemDao.getByEntityID(workItemUpdated.getWorkItemID()) == null){
					
					VisualStudioWorkItemEntity dbBug = new VisualStudioWorkItemEntity(); 
					dbBug.setWorkItemID(workItemUpdated.getWorkItemID());
					createANewTask(dbBug, testStrategyWorkItem, project, account, () -> dbBug.setTestStrategy(true), () -> !dbBug.isTestStrategy());
					workItemDao.save(dbBug);

				}
				
				//Slack QA for create test strategy..
				SlackMessage message = new SlackMessageBuilder()
					.setIconEmoji(SlackMessages.BUG_APPROVED.img)
					.setUsername(SlackMessages.BUG_APPROVED.senderName)
					.setChannel(SlackUtil.getSlackAccountMentionByEmail(workItemUpdated.getCreatedByEmail()))
					.setText(String.format(SlackMessages.BUG_APPROVED.message, VisualStudioUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), workItemUpdated.getWorkItemID()))
					.build();
				SlackUtil.sendMessage(message);
				
			}
			return returnMessage;
		}
		
	private void rebaseIntegrationToMasterAndTestOnMasterDoneProcess(VisualStudioForm workItemUpdated, String account,
			VisualStudioRevisionForm bugWorkItem) throws IOException, URISyntaxException, Exception {
		//Modified State to DONE
		VisualStudioWorkItem partialBugItem = new VisualStudioWorkItemBuilder()
				.modifiedStatus(VisualStudioWorkItemState.DONE)
				.build();
		//Update
		VisualStudioUtil.updateWorkItem(partialBugItem, String.valueOf(bugWorkItem.getId()), workItemUpdated.getProject(), account);
	}


	private String mergeToMasterDoneProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage;
		returnMessage = "Create 'Done done test' and 'Rebase integration to master'";
		String project = workItemUpdated.getProject();
		
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
		
		VisualStudioWorkItem rebaseIntegrationToMasterTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.REABASE_INTEGRATION_TO_MASTER.value)
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(bugWorkItem.getAssignedToName())
			.build();
		
		
		//Find Owner of 'Test Strategy'
		String ownserOFAddAcceptanceCriteria = bugWorkItem.findTestStrategy(account).getAssignedToName();
		VisualStudioWorkItem doneDoneTestTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.TEST_ON_MASTER.value)
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(ownserOFAddAcceptanceCriteria)	
			.build();

		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString()); 
		createANewTask(dbBug, rebaseIntegrationToMasterTask, project, account, () -> dbBug.setRebaseIntegrationToMaster(true), () -> !dbBug.isRebaseIntegrationToMaster());
		createANewTask(dbBug, doneDoneTestTask, project, account, () -> dbBug.setDoneDoneTest(true), () -> !dbBug.isDoneDoneTest());
		workItemDao.saveOrUpdate(dbBug);
		
		//Slack QA for Functional Test
		SlackMessage message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessages.MERGE_TO_MASTER_DONE.img)
			.setUsername(SlackMessages.MERGE_TO_MASTER_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(workItemUpdated.getCreatedByEmail()))
			.setText(String.format(SlackMessages.MERGE_TO_MASTER_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), workItemUpdated.getWorkItemID()))
			.build();
		SlackUtil.sendMessage(message);
				
		return returnMessage;
	}


	private String getCodeReviewanFunctionalTestDoneProcess(String account, String project, VisualStudioRevisionForm bugWorkItem) throws Exception, IOException, URISyntaxException {
		String returnMessage;
		returnMessage = "Create 'Merge to Master'";
		
		VisualStudioWorkItem mergeToMasterTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.MERGE_TO_MASTER.value)
			.addStatus(VisualStudioTaskState.TO_DO)
			.addAssignTo(bugWorkItem.getAssignedToName())
			.addIteration(bugWorkItem.getIteration())
			.build();

		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString()); 
		createANewTask(dbBug, mergeToMasterTask, project, account, () -> dbBug.setMergeToMaster(true), () -> !dbBug.isMergeToMaster());
		workItemDao.saveOrUpdate(dbBug);
		
		//Slack QA for Functional Test
		SlackMessage message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessages.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.img)
			.setUsername(SlackMessages.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(bugWorkItem.getAssignedToEmail()))
			.setText(String.format(SlackMessages.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
			.build();
		SlackUtil.sendMessage(message);
				
		return returnMessage;
	}


	private String developFixDoneProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage;
		returnMessage = "Develop Fix was change to DONE";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString());
		
		//Find Owner of 'Find Test Strategy'
		String ownserOFAddAcceptanceCriteria = bugWorkItem.findTestStrategy(account).getAssignedToName();
		VisualStudioWorkItem functionalTestTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.FUNCTIONAL_TEST.value)
			.addStatus(VisualStudioTaskState.TO_DO)
			.addAssignTo(ownserOFAddAcceptanceCriteria)
			.addIteration(bugWorkItem.getIteration())
			.build();

		VisualStudioWorkItem getCodeReviewTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.GET_CODE_REVIEW.value)
			.addStatus(VisualStudioTaskState.TO_DO)
			.addAssignTo(bugWorkItem.getAssignedToName())
			.addIteration(bugWorkItem.getIteration())
			.build();
		
		createANewTask(dbBug, functionalTestTask, project, account, () -> dbBug.setFunctionalTest(true), () -> !dbBug.isFunctionalTest());
		createANewTask(dbBug, getCodeReviewTask, project, account, () -> dbBug.setGetCodeReview(true), () -> !dbBug.isGetCodeReview());
		workItemDao.saveOrUpdate(dbBug);
		
		//Slack QA for Functional Test
		SlackMessage message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessages.DEVELOP_FIX_DONE.img)
			.setUsername(SlackMessages.DEVELOP_FIX_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(workItemUpdated.getCreatedByEmail()))
			.setText(String.format(SlackMessages.DEVELOP_FIX_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), workItemUpdated.getWorkItemID()))
			.build();
		SlackUtil.sendMessage(message);
		
		return returnMessage;
	}


	private String reproduceWithAutomatedTestsDoneProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage;
		returnMessage = "Reproduce With automated Test Change to DONE";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString());
		
		VisualStudioWorkItem developFixTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.DEVELOP_FIX.value)
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(bugWorkItem.getAssignedToName())
			.build();
		createANewTask(dbBug, developFixTask, project, account, () -> dbBug.setDevelopFix(true), () -> !dbBug.isDevelopFix());
		workItemDao.saveOrUpdate(dbBug);
		return returnMessage;
	}


	private String createNewBranchDoneProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage;
		returnMessage = "Create a new Branch moved to Done";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString());
		//Create Reproduce with automated test TASK
		VisualStudioWorkItem reproduceWithAutomatedTestWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.REPRODUCE_WITH_AUTOMATED_TESTS.value)
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(bugWorkItem.getAssignedToName())
			.build();
		
		createANewTask(dbBug, reproduceWithAutomatedTestWorkItem, project, account, () -> dbBug.setReproduceWithAutomatedTest(true), () -> !dbBug.isReproduceWithAutomatedTest());
		workItemDao.saveOrUpdate(dbBug);
		return returnMessage;
	}


	private String testStrategyDoneProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage;
		returnMessage = "Test Strategy moved to Done";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString());
		
		//Modified State to COMMITED
		VisualStudioWorkItem updatedItem = new VisualStudioWorkItemBuilder()
			.modifiedStatus(VisualStudioWorkItemState.COMMITTED)
			.build();
		//Update
		VisualStudioUtil.updateWorkItem(updatedItem, String.valueOf(bugWorkItem.getId()), workItemUpdated.getProject(), account);
		
		//Find Owner of 'Test Strategy'
		String ownserOFAddAcceptanceCriteria = bugWorkItem.findTestStrategy(account).getAssignedToName();
		//Create Add Acceptance Criteria
		VisualStudioWorkItem addAcceptanceCriteriaTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.ADD_ACCEPTANCE_CRITERIA.value)
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(ownserOFAddAcceptanceCriteria)
			.build();

		VisualStudioWorkItem createNewBranchWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.CREATE_A_NEW_BRANCH.value)
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(bugWorkItem.getAssignedToName() != null ? bugWorkItem.getAssignedToName() : "")
			.build();
		
		createANewTask(dbBug, addAcceptanceCriteriaTask, project, account, () -> dbBug.setAddAcceptanceCriteria(true), () -> !dbBug.isAddAcceptanceCriteria());
		createANewTask(dbBug, createNewBranchWorkItem, project, account, () -> dbBug.setCreateANewBranch(true), () -> !dbBug.isCreateANewBranch());
		workItemDao.saveOrUpdate(dbBug);
		
		//Slack DEV for start working
		SlackMessage message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessages.TEST_STRATEGY_DONE.img)
			.setUsername(SlackMessages.TEST_STRATEGY_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(workItemUpdated.getCreatedByEmail()))
			.setText(String.format(SlackMessages.TEST_STRATEGY_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), workItemUpdated.getWorkItemID()))
			.build();
		SlackUtil.sendMessage(message);
		
		return returnMessage;
	}

	//Lambda Methods
	interface setValue {
		void evaluate();
	}
	interface isValue {
		Boolean evaluate();
	}
	
	private void createANewTask(VisualStudioWorkItemEntity dbBug, VisualStudioWorkItem task, String project, String account, setValue setValueIfSuccess, isValue preChecker) throws IOException, URISyntaxException {
		if(preChecker.evaluate()){
			Boolean statusCreate = VisualStudioUtil.createNewTask(task, project, account);
			if(statusCreate) {
				setValueIfSuccess.evaluate();
			}
		}
	}
}
