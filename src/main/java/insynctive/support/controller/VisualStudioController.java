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
import insynctive.support.utils.slack.SlackMessageObject;
import insynctive.support.utils.slack.SlackMessage;
import insynctive.support.utils.slack.SlackUtil;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;
import insynctive.support.utils.vs.VisualStudioTaskName;
import insynctive.support.utils.vs.VisualStudioTaskState;
import insynctive.support.utils.vs.VisualStudioWorkItem;
import insynctive.support.utils.vs.VisualStudioBugState;
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
		returnMessage += "- Was Change to Done: "+workItemUpdated.changedToDone();

		//IF is a BUG
		if(workItemUpdated.isABug()){
			//Manage Updated For Individual BUG
			if(workItemUpdated.noHaveParent()) {
				returnMessage = manageUpdatedForIndividualBug(workItemUpdated, account, returnMessage);
			}
			
			//Manage Updated For Critical Bug
			if(workItemUpdated.changedToCritical()) {
				SlackUtil.createNewChannel("Critical-"+workItemUpdated.getTitle());
			}
			
			//Manage Updated For Critical Bug
			if(workItemUpdated.isCritical() && workItemUpdated.changedToDone()) {
				SlackUtil.archiveChannel("Critical-"+workItemUpdated.getTitle());
			}
		}
		
		//IF is a TASK
		if(workItemUpdated.isATask()){
			if(workItemUpdated.changedAssignation()){
				alertAssignation(workItemUpdated, account);
			}
			returnMessage = manageUpdatedForTask(workItemUpdated, account, returnMessage);
		}
		
		return "{\"status\" : 200, \"message\": \""+returnMessage+"\"}";
	}

	private void alertAssignation(VisualStudioForm workItemUpdated, String account) throws IOException, Exception {
		//Slack Assigned..
		String project = workItemUpdated.getProject();
		String text = workItemUpdated.getOldAssigned() != null ?
				String.format(SlackMessage.ASSIGNED_TO_WORK_ITEM.message, 
						VisualStudioUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), 
						workItemUpdated.getType(), 
						workItemUpdated.getWorkItemID(), 
						workItemUpdated.getOldAssignedName()) 
				:
				String.format(SlackMessage.NEW_ASSIGNED_TO_WORK_ITEM.message, 
						VisualStudioUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), 
						workItemUpdated.getType(), 
						workItemUpdated.getWorkItemID());
		
		SlackMessageObject message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessage.ASSIGNED_TO_WORK_ITEM.img)
			.setUsername(SlackMessage.ASSIGNED_TO_WORK_ITEM.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(workItemUpdated.getAssignedToEmail()))
			.setText(text)
			.build();
		SlackUtil.sendMessage(message);
	}

	//Manage Task Upadated
	private String manageUpdatedForTask(VisualStudioForm workItemUpdated, String account, String returnMessage) throws Exception {
		
		//Test Strategy was moved to DONE
		if(workItemUpdated.isInvestigateBug() && workItemUpdated.changedToDone()) {
			returnMessage = investigateBugDoneProcess(workItemUpdated, account);
		}
		
		//Test Strategy was moved to DONE
		if(workItemUpdated.isTestStrategy() && workItemUpdated.changedToDone()) {
			returnMessage = testStrategyDoneProcess(workItemUpdated, account);
		}
		
		//Create a New Branch was moved to DONE
		else if(workItemUpdated.isCreateANewBranch() && workItemUpdated.changedToDone()){
			returnMessage = createNewBranchDoneProcess(workItemUpdated, account);
		}
		
		
		//Reproduce with automated tests was moved to DONE
		else if(workItemUpdated.isReproduceWithAutomatedTest() && (workItemUpdated.changedToDone() || workItemUpdated.waschangeToRemoved())){
			returnMessage = reproduceWithAutomatedTestsDoneOrRemovedProcess(workItemUpdated, account);
		}
		
		//Develop Fix was moved to DONE - TODO Need to make the build automatically
		else if (workItemUpdated.isDevelopFix() && workItemUpdated.changedToDone()){
			returnMessage = developFixDoneProcess(workItemUpdated, account);
		}
		
		//Get Code Review and functional Test were moved to DONE
		else if((workItemUpdated.isGetCodeReview() || workItemUpdated.isFunctionalTest() ) && workItemUpdated.changedToDone()){

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
		else if(workItemUpdated.isMergeToMaster() && workItemUpdated.changedToDone()){
			returnMessage = mergeToMasterDoneProcess(workItemUpdated, account);
		}
		
		//Rebase Integration to Master and Test on Master were move to Done
		else if((workItemUpdated.isRebaseIntegrationToMaster() || workItemUpdated.isTestOnMaster())  && workItemUpdated.changedToDone()){
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			Boolean isRebaseIntegrationToMaster = workItemUpdated.isRebaseIntegrationToMaster();
			Boolean isTestOnMaster = workItemUpdated.isTestOnMaster();
			
			if((isRebaseIntegrationToMaster && bugWorkItem.findDoneDoneTest(account).isStateDone()) || (isTestOnMaster && bugWorkItem.findRebaseIntegrationToMaster(account).isStateDone())){
				rebaseIntegrationToMasterAndTestOnMasterDoneProcess(workItemUpdated, account, bugWorkItem);
			}
		}
		return returnMessage;
	}

	//Manage Bug Updated
	private String manageUpdatedForIndividualBug(VisualStudioForm workItemUpdated, String account, String returnMessage) throws Exception {
			
			//Check if the bug was change to Approved
			if(workItemUpdated.wasChangeToApprooved()){
				
				returnMessage = "Create Test Strategy - Slack to QA. ";
				String project = workItemUpdated.getProject();

				VisualStudioWorkItem investigateBugWorkItem = new VisualStudioWorkItemBuilder()
						.addParent(String.valueOf(workItemUpdated.getWorkItemID()))
						.addTitle(VisualStudioTaskName.INVESTIGATE_BUG.value + " - " + workItemUpdated.getTitle())
						.addStatus(VisualStudioTaskState.TO_DO)
						.addIteration(workItemUpdated.getIteration())
						.addAssignTo(workItemUpdated.getAssignedToName() != null ? workItemUpdated.getAssignedToName() : "")
						.addEstimate("0.5")
						.build();
				
				//Check if the task were not created.
				if(workItemDao.getByEntityID(workItemUpdated.getWorkItemID()) == null){
					VisualStudioWorkItemEntity dbBug = new VisualStudioWorkItemEntity(); 
					dbBug.setWorkItemID(workItemUpdated.getWorkItemID());
					createANewTask(dbBug, investigateBugWorkItem, project, account, () -> dbBug.setInvestigateBug(true), () -> !dbBug.isInvestigateBug());
					workItemDao.save(dbBug);

				}
				
				//Slack QA for create test strategy..
				SlackMessageObject message = new SlackMessageBuilder()
					.setIconEmoji(SlackMessage.BUG_APPROVED.img)
					.setUsername(SlackMessage.BUG_APPROVED.senderName)
					.setChannel(SlackUtil.getSlackAccountMentionByEmail(workItemUpdated.getAssignedToEmail()))
					.setText(String.format(SlackMessage.BUG_APPROVED.message, VisualStudioUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), workItemUpdated.getWorkItemID()))
					.build();
				SlackUtil.sendMessage(message);
				
			}
			return returnMessage;
		}
		
	private void rebaseIntegrationToMasterAndTestOnMasterDoneProcess(VisualStudioForm workItemUpdated, String account,
			VisualStudioRevisionForm bugWorkItem) throws IOException, URISyntaxException, Exception {
		//Modified State to DONE
		VisualStudioWorkItem partialBugItem = new VisualStudioWorkItemBuilder()
				.modifiedStatus(VisualStudioBugState.DONE)
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
			.addTitle(VisualStudioTaskName.REBASE_INTEGRATION_TO_MASTER.value + " - " + bugWorkItem.getTitle() )
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(bugWorkItem.findMergeToMaster(account).getAssignedToName())
			.addEstimate("0.5")
			.build();
		
		
		//Find Owner of 'Test Strategy'
		VisualStudioRevisionForm functionalTest = bugWorkItem.findFunctionalTest(account);
		VisualStudioWorkItem doneDoneTestTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.TEST_ON_MASTER.value + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(functionalTest.getAssignedToName())
			.addEstimate("0.5")
			.build();

		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString()); 
		createANewTask(dbBug, rebaseIntegrationToMasterTask, project, account, () -> dbBug.setRebaseIntegrationToMaster(true), () -> !dbBug.isRebaseIntegrationToMaster());
		createANewTask(dbBug, doneDoneTestTask, project, account, () -> dbBug.setDoneDoneTest(true), () -> !dbBug.isDoneDoneTest());
		workItemDao.saveOrUpdate(dbBug);
		
		//Slack QA for Functional Test
		SlackMessageObject message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessage.MERGE_TO_MASTER_DONE.img)
			.setUsername(SlackMessage.MERGE_TO_MASTER_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(functionalTest.getAssignedToEmail()))
			.setText(String.format(SlackMessage.MERGE_TO_MASTER_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
			.build();
		SlackUtil.sendMessage(message);
				
		return returnMessage;
	}


	private String getCodeReviewanFunctionalTestDoneProcess(String account, String project, VisualStudioRevisionForm bugWorkItem) throws Exception, IOException, URISyntaxException {
		String returnMessage;
		returnMessage = "Create 'Merge to Master'";
		
		VisualStudioRevisionForm findGetCodeReview = bugWorkItem.findGetCodeReview(account);
		VisualStudioWorkItem mergeToMasterTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.MERGE_TO_MASTER.value + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addAssignTo(findGetCodeReview.getAssignedToName())
			.addIteration(bugWorkItem.getIteration())
			.addEstimate("0.5")
			.build();

		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString()); 
		createANewTask(dbBug, mergeToMasterTask, project, account, () -> dbBug.setMergeToMaster(true), () -> !dbBug.isMergeToMaster());
		workItemDao.saveOrUpdate(dbBug);
		 
		//Slack QA for Functional Test
		SlackMessageObject message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessage.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.img)
			.setUsername(SlackMessage.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(findGetCodeReview.getAssignedToEmail()))
			.setText(String.format(SlackMessage.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
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
		VisualStudioRevisionForm ownserOFAddAcceptanceCriteria = bugWorkItem.findTestStrategy(account);
		VisualStudioWorkItem functionalTestTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.FUNCTIONAL_TEST.value + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addAssignTo(ownserOFAddAcceptanceCriteria.getAssignedToName())
			.addIteration(bugWorkItem.getIteration())
			.addEstimate("0.5")
			.build();

		VisualStudioWorkItem getCodeReviewTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.GET_CODE_REVIEW.value + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addAssignTo(bugWorkItem.findDevelopFix(account).getAssignedToName())
			.addIteration(bugWorkItem.getIteration())
			.build();
		
		createANewTask(dbBug, functionalTestTask, project, account, () -> dbBug.setFunctionalTest(true), () -> !dbBug.isFunctionalTest());
		createANewTask(dbBug, getCodeReviewTask, project, account, () -> dbBug.setGetCodeReview(true), () -> !dbBug.isGetCodeReview()); 
		workItemDao.saveOrUpdate(dbBug);
		 
		//Slack QA for Functional Test
		SlackMessageObject message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessage.DEVELOP_FIX_DONE.img)
			.setUsername(SlackMessage.DEVELOP_FIX_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(ownserOFAddAcceptanceCriteria.getAssignedToEmail()))
			.setText(String.format(SlackMessage.DEVELOP_FIX_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
			.build();
		SlackUtil.sendMessage(message);
		
		return returnMessage;
	}


	private String reproduceWithAutomatedTestsDoneOrRemovedProcess(VisualStudioForm workItemUpdated, String account)
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
			.addTitle(VisualStudioTaskName.DEVELOP_FIX.value + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addEstimate("1")
			.addAssignTo(bugWorkItem.findReproduceWithAutomatedTest(account).getAssignedToName())
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
			.addTitle(VisualStudioTaskName.REPRODUCE_WITH_AUTOMATED_TESTS.value + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(bugWorkItem.findCreateNewBranch(account).getAssignedToName())
			.addEstimate("1")
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
			.modifiedStatus(VisualStudioBugState.COMMITTED)
			.build();
		//Update
		VisualStudioUtil.updateWorkItem(updatedItem, String.valueOf(bugWorkItem.getId()), workItemUpdated.getProject(), account);
		
		//Find Owner of 'Test Strategy'
		String ownserOFAddAcceptanceCriteria = bugWorkItem.findTestStrategy(account).getAssignedToName();
		//Create Add Acceptance Criteria
		VisualStudioWorkItem addAcceptanceCriteriaTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.ADD_ACCEPTANCE_CRITERIA.value + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(ownserOFAddAcceptanceCriteria)
			.build();

		VisualStudioWorkItem createNewBranchWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()))
			.addTitle(VisualStudioTaskName.CREATE_A_NEW_BRANCH.value + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(bugWorkItem.getAssignedToName() != null ? bugWorkItem.getAssignedToName() : "")
			.build();
		
		createANewTask(dbBug, addAcceptanceCriteriaTask, project, account, () -> dbBug.setAddAcceptanceCriteria(true), () -> !dbBug.isAddAcceptanceCriteria());
		createANewTask(dbBug, createNewBranchWorkItem, project, account, () -> dbBug.setCreateANewBranch(true), () -> !dbBug.isCreateANewBranch());
		workItemDao.saveOrUpdate(dbBug);
		
		//Slack DEV for start working
		SlackMessageObject message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessage.TEST_STRATEGY_DONE.img)
			.setUsername(SlackMessage.TEST_STRATEGY_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(bugWorkItem.getAssignedToEmail()))
			.setText(String.format(SlackMessage.TEST_STRATEGY_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
			.build();
		SlackUtil.sendMessage(message);
		
		return returnMessage;
	}

	private String investigateBugDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception, IOException, URISyntaxException {
		String returnMessage;
		returnMessage = "Investigate Bug moved to Done";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString());
		
		VisualStudioWorkItem testStrategyWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(workItemUpdated.getWorkItemID()))
			.addTitle(VisualStudioTaskName.TEST_STRATEGY.value + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(workItemUpdated.getIteration())
			.addAssignTo(workItemUpdated.getCreatedByName())
			.addEstimate("0.5")
			.build();		

		createANewTask(dbBug, testStrategyWorkItem, project, account, () -> dbBug.setTestStrategy(true), () -> !dbBug.isTestStrategy());
		workItemDao.saveOrUpdate(dbBug);
		
		//Slack DEV for start working
		SlackMessageObject message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessage.INVESTIGATE_BUG_DONE.img)
			.setUsername(SlackMessage.INVESTIGATE_BUG_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(bugWorkItem.getCreatedByEmail()))
			.setText(String.format(SlackMessage.INVESTIGATE_BUG_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
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
