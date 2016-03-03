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
import insynctive.support.form.vs.VisualStudioRelationsForm;
import insynctive.support.form.vs.VisualStudioRevisionForm;
import insynctive.support.model.VisualStudioWorkItemEntity;
import insynctive.support.utils.Property;
import insynctive.support.utils.UserDetails;
import insynctive.support.utils.VisualStudioUtil;
import insynctive.support.utils.slack.SlackMessageObject;
import insynctive.support.utils.slack.SlackMessage;
import insynctive.support.utils.slack.SlackUtil;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;
import insynctive.support.utils.vs.VisualStudioTaskData;
import insynctive.support.utils.vs.VisualStudioTaskState;
import insynctive.support.utils.vs.VisualStudioWorkItem;
import insynctive.support.utils.vs.VisualStudioBugState;
import insynctive.support.utils.vs.builder.VisualStudioWorkItemBuilder;

@Controller
@RequestMapping(value = "/vs")
public class VisualStudioController {

	private final WorkItemDao workItemDao;
	private final Property property;
	
	@Inject
	public VisualStudioController(WorkItemDao workItemDao, Property property) {
		this.property = property;
		this.workItemDao = workItemDao;
	}
	
	@RequestMapping(value = "/updateWorkItem/{account}" ,method = RequestMethod.POST)
	@ResponseBody
	public String restToState(@RequestBody VisualStudioForm workItemUpdated, @PathVariable String account) throws Exception{

		//Default return message
		String returnMessage = "Is Task: "+workItemUpdated.isATask();
		returnMessage += "- Is Bug: "+workItemUpdated.isABug();
		returnMessage += "- Is Develop Fix: "+workItemUpdated.isDevelopFix();
		returnMessage += "- Was Change to approved: "+workItemUpdated.changedToApprooved();
		returnMessage += "- Was Change to In Progress: "+workItemUpdated.changedToInProgress();
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
			if(workItemUpdated.criticalChangeStateToDone()) {
				SlackUtil.archiveChannel("Critical-"+workItemUpdated.getTitle());
			}
		}
		
		//IF is a TASK
		if(workItemUpdated.isATask()) {
			
			if(workItemUpdated.changedAssignation()){
				alertAssignation(workItemUpdated, account); 
			}
			
			VisualStudioRevisionForm firstRelation = workItemUpdated.getParentFullObject(account);
			if(firstRelation.isABug() && firstRelation.noHaveParent()){
				returnMessage = manageUpdatedForTaskInIndependentBug(workItemUpdated, account, returnMessage);
			}
			
			if(firstRelation.isAStory()){
				returnMessage = manageUpdatedForTaskInStory(workItemUpdated, account);
			}
		}
		
		//IF is a STORY
		if(workItemUpdated.isAStory()){
			returnMessage = manageUpdateForStory(workItemUpdated, account);
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
	private String manageUpdatedForTaskInIndependentBug(VisualStudioForm workItemUpdated, String account, String returnMessage) throws Exception {
		
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
		else if(workItemUpdated.isReproduceWithAutomatedTest() && (workItemUpdated.changedToDone() || workItemUpdated.changedToRemoved())){
			returnMessage = reproduceWithAutomatedTestsDoneOrRemovedProcess(workItemUpdated, account);
		}
		
		//Develop Fix was moved to DONE - TODO Need to make the build automatically
		else if (workItemUpdated.isDevelopFix() && workItemUpdated.changedToDone()){
			returnMessage = developFixDoneProcess(workItemUpdated, account);
		}
		
		//Get Code Review and functional Test were moved to DONE
		else if((workItemUpdated.isGetCodeReview() || workItemUpdated.isFunctionalTest() ) && workItemUpdated.changedToDone()){

			String project = workItemUpdated.getProject();
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			Boolean isGetCodeReview = workItemUpdated.isGetCodeReview();
			Boolean isFunctionalTest = workItemUpdated.isFunctionalTest();

			VisualStudioRevisionForm functionaltest = bugWorkItem.findFunctionalTest(account);
			VisualStudioRevisionForm getCodeReview = bugWorkItem.findGetCodeReview(account);
			
			if((isGetCodeReview && functionaltest.isStateDone()) || (isFunctionalTest && getCodeReview.isStateDone())){
				
				returnMessage = getCodeReviewanFunctionalTestDoneProcess(account, project, bugWorkItem);
			
			} else {
			
				if(isGetCodeReview && !functionaltest.isStateDone()){
					SlackMessageObject message = new SlackMessageBuilder()
						.setIconEmoji(SlackMessage.GET_CODE_REVIEW_DONE_FUNCTIONAL_TEST_NOT.img)
						.setUsername(SlackMessage.GET_CODE_REVIEW_DONE_FUNCTIONAL_TEST_NOT.senderName)
						.setChannel(SlackUtil.getSlackAccountMentionByEmail(functionaltest.getAssignedToEmail()))
						.setText(String.format(SlackMessage.GET_CODE_REVIEW_DONE_FUNCTIONAL_TEST_NOT.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
						.build();
					SlackUtil.sendMessage(message);
				}
	
				if(isFunctionalTest && !getCodeReview.isStateDone()){
					SlackMessageObject message = new SlackMessageBuilder()
							.setIconEmoji(SlackMessage.FUNCTIONAL_TEST_DONE_GET_CODE_REVIEW_NOT.img)
							.setUsername(SlackMessage.FUNCTIONAL_TEST_DONE_GET_CODE_REVIEW_NOT.senderName)
							.setChannel(SlackUtil.getSlackAccountMentionByEmail(getCodeReview.getAssignedToEmail()))
							.setText(String.format(SlackMessage.FUNCTIONAL_TEST_DONE_GET_CODE_REVIEW_NOT.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
							.build();
						SlackUtil.sendMessage(message);
				}
				
			}
			
		}
		
		//Merge to Master was moved to DONE - TODO Run Build 
		else if(workItemUpdated.isMergeToMaster() && workItemUpdated.changedToDone()){
			returnMessage = mergeToMasterBugDoneProcess(workItemUpdated, account);
		}
		
		//Rebase Integration to Master and Test on Master were move to Done
		else if((workItemUpdated.isRebaseIntegrationToMaster() || workItemUpdated.isTestOnMaster())  && workItemUpdated.changedToDone()){
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			Boolean isRebaseIntegrationToMaster = workItemUpdated.isRebaseIntegrationToMaster();
			Boolean isTestOnMaster = workItemUpdated.isTestOnMaster();
			
			if((isRebaseIntegrationToMaster && bugWorkItem.findTestOnMasterBug(account).isStateDone()) || (isTestOnMaster && bugWorkItem.findRebaseIntegrationToMaster(account).isStateDone())){
				rebaseIntegrationToMasterAndTestOnMasterDoneProcess(workItemUpdated, account, bugWorkItem);
			}
		}
		return returnMessage;
	}

	private String manageUpdateForStory(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "";
		
		if(workItemUpdated.changedToApprooved()) {
			
			returnMessage = "Story was changed to approved";
			String project = workItemUpdated.getProject();

			VisualStudioWorkItem estimateStory = new VisualStudioWorkItemBuilder()
					.addParent(String.valueOf(workItemUpdated.getWorkItemID()), property.getVSAccount())
					.addTitle(VisualStudioTaskData.ESTIMATE_STORY.value + " - S" + workItemUpdated.getWorkItemID() + " - " + workItemUpdated.getTitle())
					.addStatus(VisualStudioTaskState.TO_DO)
					.addIteration(workItemUpdated.getIteration())
					.addAssignTo(workItemUpdated.getAssignedToName() != null ? workItemUpdated.getAssignedToName() : "")
					.addEstimate(VisualStudioTaskData.ESTIMATE_STORY.defaultEstimate)
					.build();
			
			//Check if the task were not created.
			if(workItemDao.getByEntityID(workItemUpdated.getWorkItemID()) == null){
				VisualStudioWorkItemEntity dbBug = new VisualStudioWorkItemEntity(); 
				dbBug.setWorkItemID(workItemUpdated.getWorkItemID());
				createANewTask(estimateStory, project, account, () -> dbBug.setEstimateStory(true), () -> !dbBug.isEstimateStory());
				workItemDao.save(dbBug);

			}
			
		}
		
		return returnMessage;
	}
	
	private String manageUpdatedForTaskInStory(VisualStudioForm workItemUpdated, String account) throws Exception {
		
		String returnMessage = "";
		
		//Test Strategy was moved to DONE
		if(workItemUpdated.isEstimateStory() && workItemUpdated.changedToDone()) {
			returnMessage = estimateStoryDoneProcess(workItemUpdated, account);
		}

		if(workItemUpdated.isCreateANewBranch() && workItemUpdated.changedToDone()) {
			returnMessage = createANewBranchDoneProcess(workItemUpdated, account);
		}
		
		if(workItemUpdated.isDevelopTDD() && workItemUpdated.changedToDone()) {
			returnMessage = developTDDDoneProcess(workItemUpdated, account);
		}
		
		if(workItemUpdated.isDevelopCodeStory() && workItemUpdated.changedToDone()) {
			returnMessage = developCodeStoryDoneProcess(workItemUpdated, account);
		}
		
		if((workItemUpdated.isPostStoryMovie() || workItemUpdated.isTestingStrategyStory()) && workItemUpdated.changedToDone()) {
			returnMessage = postStoryMovieOrTestingStrategyDoneProcess(workItemUpdated, account);
		}
		
		if(workItemUpdated.isPostStoryMovie() && workItemUpdated.changedToDone()) {
			returnMessage = postStoryMovieDoneProcess(workItemUpdated, account);
		}
		
		if(workItemUpdated.isApproveStoryMovie() && workItemUpdated.changedToDone()) {
			returnMessage = approveStoryMovieDoneProcess(workItemUpdated, account);
		}
		
		if((workItemUpdated.isFunctionalTestOnIntegration() || workItemUpdated.isPullRequestForStory() || workItemUpdated.isUIAutomatedTesting()) && workItemUpdated.changedToDone()) {
			returnMessage = pullRequestOrFunctionalTestOnIntegrationOrUiAutomatedtestingDoneProcess(workItemUpdated, account);
		}

		if(workItemUpdated.isMergeToMaster() && workItemUpdated.changedToDone()) {
			returnMessage = mergeToMasterStoryDoneProcess(workItemUpdated, account);
		}

		if(workItemUpdated.isTestOnMasterStory() && workItemUpdated.changedToDone()) {
			returnMessage = testOnMasterDoneProcess(workItemUpdated, account);
		}
		
		if((workItemUpdated.isApproveForRelease() || workItemUpdated.isApproveForRelease()) && workItemUpdated.changedToDone()) {
			returnMessage = approveForReleaseOrRebaseIntegrationToMasterDoneProcess(workItemUpdated, account);
		}
		
		return returnMessage;
	}

	//Manage Bug Updated
	private String manageUpdatedForIndividualBug(VisualStudioForm workItemUpdated, String account, String returnMessage) throws Exception {
			
			//Check if the bug was change to Approved
			if(workItemUpdated.changedToApprooved()){
				
				returnMessage = "Create Test Strategy - Slack to QA. ";
				String project = workItemUpdated.getProject();

				VisualStudioWorkItem investigateBugWorkItem = new VisualStudioWorkItemBuilder()
						.addParent(String.valueOf(workItemUpdated.getWorkItemID()), property.getVSAccount())
						.addTitle(VisualStudioTaskData.INVESTIGATE_BUG.value + " - B" + workItemUpdated.getWorkItemID() + " - " + workItemUpdated.getTitle())
						.addStatus(VisualStudioTaskState.TO_DO)
						.addIteration(workItemUpdated.getIteration())
						.addAssignTo(workItemUpdated.getAssignedToName() != null ? workItemUpdated.getAssignedToName() : "")
						.addEstimate(VisualStudioTaskData.INVESTIGATE_BUG.defaultEstimate)
						.build();
				
				//Check if the task were not created.
				if(workItemDao.getByEntityID(workItemUpdated.getWorkItemID()) == null){
					VisualStudioWorkItemEntity dbBug = new VisualStudioWorkItemEntity(); 
					dbBug.setWorkItemID(workItemUpdated.getWorkItemID());
					createANewTask(investigateBugWorkItem, project, account, () -> dbBug.setInvestigateBug(true), () -> !dbBug.isInvestigateBug());
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
		
	private void rebaseIntegrationToMasterAndTestOnMasterDoneProcess(VisualStudioForm workItemUpdated, String account, VisualStudioRevisionForm bugWorkItem) throws IOException, URISyntaxException, Exception {
		//Modified State to DONE
		VisualStudioWorkItem partialBugItem = new VisualStudioWorkItemBuilder()
				.modifiedStatus(VisualStudioBugState.DONE)
				.build();
		//Update
		VisualStudioUtil.updateWorkItem(partialBugItem, String.valueOf(bugWorkItem.getId()), workItemUpdated.getProject(), account);
	}

	private String mergeToMasterBugDoneProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage;
		returnMessage = "Create 'Done done test' and 'Rebase integration to master'";
		String project = workItemUpdated.getProject();
		
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
		
		VisualStudioWorkItem rebaseIntegrationToMasterTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.REBASE_INTEGRATION_TO_MASTER.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle() )
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(workItemUpdated.getIteration())
			.addAssignTo(workItemUpdated.getAssignedToName()) //workItemUpdated = Merge to Master
			.addEstimate(VisualStudioTaskData.REBASE_INTEGRATION_TO_MASTER.defaultEstimate)
			.build();
		
		
		//Find Owner of 'Functional Test'
		VisualStudioRevisionForm functionalTest = bugWorkItem.findFunctionalTest(account);
		VisualStudioWorkItem doneDoneTestTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.TEST_ON_MASTER_BUG.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(workItemUpdated.getIteration())
			.addAssignTo(functionalTest != null ? functionalTest.getAssignedToName() : bugWorkItem.getCreatedByName())
			.addEstimate(VisualStudioTaskData.TEST_ON_MASTER_BUG.defaultEstimate)
			.build();

		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString()); 
		createANewTask(rebaseIntegrationToMasterTask, project, account, () -> dbBug.setRebaseIntegrationToMaster(true), () -> !dbBug.isRebaseIntegrationToMaster());
		createANewTask(doneDoneTestTask, project, account, () -> dbBug.setDoneDoneTest(true), () -> !dbBug.isDoneDoneTest());
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
		String returnMessage = "Create 'Merge to Master'";
		
		VisualStudioRevisionForm getCodeReview = bugWorkItem.findGetCodeReview(account);
		VisualStudioWorkItem mergeToMasterTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.MERGE_TO_MASTER_BUG.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addAssignTo(getCodeReview != null ? getCodeReview.getAssignedToName() : bugWorkItem.getAssignedToName())
			.addIteration(getCodeReview != null ? getCodeReview.getIteration() : bugWorkItem.getIteration())
			.addEstimate(VisualStudioTaskData.MERGE_TO_MASTER_BUG.defaultEstimate)
			.build();

		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString()); 
		createANewTask(mergeToMasterTask, project, account, () -> dbBug.setMergeToMaster(true), () -> !dbBug.isMergeToMaster());
		workItemDao.saveOrUpdate(dbBug);
		 
		//Slack QA for Functional Test
		SlackMessageObject message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessage.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.img)
			.setUsername(SlackMessage.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(getCodeReview.getAssignedToEmail()))
			.setText(String.format(SlackMessage.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
			.build();
		SlackUtil.sendMessage(message);
				
		return returnMessage;
	}


	private String developFixDoneProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage = "Develop Fix was change to DONE";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString());
		
		//Find Owner of 'Test Strategy'
		VisualStudioRevisionForm testStrategy = bugWorkItem.findTestStrategy(account);
		VisualStudioWorkItem functionalTestTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.FUNCTIONAL_TEST.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addAssignTo(testStrategy != null ? testStrategy.getAssignedToName() : bugWorkItem.getCreatedByName())
			.addIteration(workItemUpdated.getIteration())
			.addEstimate(VisualStudioTaskData.FUNCTIONAL_TEST.defaultEstimate) 
			.build();

		VisualStudioWorkItem getCodeReviewTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.GET_CODE_REVIEW.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(workItemUpdated.getIteration())
			.addAssignTo(workItemUpdated.getAssignedToName()) //workItemUpdated = Develop Fix
			.addEstimate(VisualStudioTaskData.GET_CODE_REVIEW.defaultEstimate)
			.build();
		
		createANewTask(functionalTestTask, project, account, () -> dbBug.setFunctionalTest(true), () -> !dbBug.isFunctionalTest());
		createANewTask(getCodeReviewTask, project, account, () -> dbBug.setGetCodeReview(true), () -> !dbBug.isGetCodeReview()); 
		workItemDao.saveOrUpdate(dbBug);
		 
		//Slack QA for Functional Test
		SlackMessageObject message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessage.DEVELOP_FIX_DONE.img)
			.setUsername(SlackMessage.DEVELOP_FIX_DONE.senderName)
			.setChannel(SlackUtil.getSlackAccountMentionByEmail(testStrategy != null ? testStrategy.getAssignedToEmail() : bugWorkItem.getCreatedByEmail()))
			.setText(String.format(SlackMessage.DEVELOP_FIX_DONE.message, VisualStudioUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
			.build();
		SlackUtil.sendMessage(message);
		
		return returnMessage;
	}


	private String reproduceWithAutomatedTestsDoneOrRemovedProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage = "Reproduce With automated Test Change to DONE";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString());
		
		VisualStudioWorkItem developFixTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.DEVELOP_FIX.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(workItemUpdated.getIteration())
			.addAssignTo(workItemUpdated.getAssignedToName()) //workItemUpdated = reproduce with automated tests
			.addEstimate(VisualStudioTaskData.DEVELOP_FIX.defaultEstimate)
			.build();
		createANewTask(developFixTask, project, account, () -> dbBug.setDevelopFix(true), () -> !dbBug.isDevelopFix());
		workItemDao.saveOrUpdate(dbBug);
		return returnMessage;
	}


	private String createNewBranchDoneProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage = "Create a new Branch moved to Done";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString());
		//Create Reproduce with automated test TASK
		VisualStudioWorkItem reproduceWithAutomatedTestWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.REPRODUCE_WITH_AUTOMATED_TESTS.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(workItemUpdated.getIteration())
			.addAssignTo(workItemUpdated.getAssignedToName()) //workItemUpdated = Create New Branch
			.addEstimate(VisualStudioTaskData.REPRODUCE_WITH_AUTOMATED_TESTS.defaultEstimate)
			.build();
		
		createANewTask(reproduceWithAutomatedTestWorkItem, project, account, () -> dbBug.setReproduceWithAutomatedTest(true), () -> !dbBug.isReproduceWithAutomatedTest());
		workItemDao.saveOrUpdate(dbBug);
		return returnMessage;
	}


	private String testStrategyDoneProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage = "Test Strategy moved to Done";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString());
		
		//Create Add Acceptance Criteria
		VisualStudioWorkItem addAcceptanceCriteriaTask = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.ADD_ACCEPTANCE_CRITERIA.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(workItemUpdated.getIteration())
			.addAssignTo(workItemUpdated.getAssignedToName() != null ? workItemUpdated.getAssignedToName() : bugWorkItem.getCreatedByName()) //workItemUpdated = 'Test Strategy'
			.addEstimate(VisualStudioTaskData.ADD_ACCEPTANCE_CRITERIA.defaultEstimate)
			.build();

		VisualStudioRevisionForm investigateBug = bugWorkItem.findInvestigateBug(account);
		VisualStudioWorkItem createNewBranchWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.CREATE_A_NEW_BRANCH.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(workItemUpdated.getIteration())
			.addAssignTo(investigateBug != null ? investigateBug.getAssignedToName() : bugWorkItem.getAssignedToName())
			.addEstimate(VisualStudioTaskData.CREATE_A_NEW_BRANCH.defaultEstimate)
			.build();
		
		createANewTask(addAcceptanceCriteriaTask, project, account, () -> dbBug.setAddAcceptanceCriteria(true), () -> !dbBug.isAddAcceptanceCriteria());
		createANewTask(createNewBranchWorkItem, project, account, () -> dbBug.setCreateANewBranch(true), () -> !dbBug.isCreateANewBranch());
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
		String returnMessage = "Investigate Bug moved to Done";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString());
		
		
		//Modified State to COMMITED
		VisualStudioWorkItem updatedItem = new VisualStudioWorkItemBuilder()
			.modifiedStatus(VisualStudioBugState.COMMITTED)
			.build();
		//Update
		VisualStudioUtil.updateWorkItem(updatedItem, String.valueOf(bugWorkItem.getId()), workItemUpdated.getProject(), account);
		
		VisualStudioWorkItem testStrategyWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.TEST_STRATEGY_BUG.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(bugWorkItem.getIteration())
			.addAssignTo(bugWorkItem.getCreatedByName())
			.addEstimate(VisualStudioTaskData.TEST_STRATEGY_BUG.defaultEstimate)
			.build();		

		createANewTask(testStrategyWorkItem, project, account, () -> dbBug.setTestStrategy(true), () -> !dbBug.isTestStrategy());
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
	
	private void createANewTask(VisualStudioWorkItem task, String project, String account, setValue setValueIfSuccess, isValue preChecker) throws IOException, URISyntaxException {
		if(preChecker.evaluate() != null && preChecker.evaluate()){
			Boolean statusCreate = VisualStudioUtil.createNewTask(task, project, account);
			if(statusCreate) {
				setValueIfSuccess.evaluate();
			}
		}
	}
	
	private String estimateStoryDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Estimate Story Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioWorkItem createANewBrachWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.CREATE_A_NEW_BRANCH.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(storyWorkItem.getIteration())
			.addAssignTo(storyWorkItem.getAssignedToName())
			.addEstimate(VisualStudioTaskData.CREATE_A_NEW_BRANCH.defaultEstimate)
			.build();		

		VisualStudioWorkItem testingStrategy = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
				.addTitle(VisualStudioTaskData.TEST_STRATEGY_STORY.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(storyWorkItem.getIteration())
				.addAssignTo(storyWorkItem.getCreatedByName())
				.addEstimate(VisualStudioTaskData.TEST_STRATEGY_STORY.defaultEstimate)
				.build();
		
		createANewTask(createANewBrachWorkItem, project, account, () -> dbBug.setCreateANewBranchStory(true), () -> !dbBug.isCreateANewBranchStory());
		createANewTask(testingStrategy, project, account, () -> dbBug.setTestingStrategyStory(true), () -> !dbBug.isTestingStrategyStory());
		
		workItemDao.saveOrUpdate(dbBug);
	
		return returnMessage;
	}

	private String createANewBranchDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Create a New Branch Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm createNewBranch = storyWorkItem.findCreateNewBranch(account);
		VisualStudioWorkItem createANewBrachWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.DEVELOP_TDD_INTEGRATION_TESTS.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(createNewBranch.getIteration())
			.addAssignTo(createNewBranch.getAssignedToName())
			.addEstimate(VisualStudioTaskData.DEVELOP_TDD_INTEGRATION_TESTS.defaultEstimate)
			.build();		

		createANewTask(createANewBrachWorkItem, project, account, () -> dbBug.setDevelopTDD(true), () -> !dbBug.isDevelopTDD());
		workItemDao.saveOrUpdate(dbBug);
	
		return returnMessage;
	}

	private String developTDDDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Create a New Branch Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm developTDD = storyWorkItem.findDevelopTDD(account);
		VisualStudioWorkItem createANewBrachWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.DEVELOP_CODE_FOR_STORY.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(developTDD.getIteration())
			.addAssignTo(developTDD.getAssignedToName())
			.addEstimate(VisualStudioTaskData.DEVELOP_CODE_FOR_STORY.defaultEstimate)
			.build();		

		createANewTask(createANewBrachWorkItem, project, account, () -> dbBug.setDevelopCodeForStory(true), () -> !dbBug.isDevelopCodeForStory());
		workItemDao.saveOrUpdate(dbBug);
	
		return returnMessage;
	}

	private String developCodeStoryDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Develop Code Story Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm developCodeForStory = storyWorkItem.findDevelopCodeForStory(account);
		VisualStudioWorkItem createANewBrachWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.POST_STORY_MOVIE.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(developCodeForStory.getIteration())
			.addAssignTo(developCodeForStory.getAssignedToName())
			.addEstimate(VisualStudioTaskData.POST_STORY_MOVIE.defaultEstimate)
			.build();		

		createANewTask(createANewBrachWorkItem, project, account, () -> dbBug.setPostStoryMovie(true), () -> !dbBug.isPostStoryMovie());
		workItemDao.saveOrUpdate(dbBug);
	
		return returnMessage;
	}
	
	private String postStoryMovieOrTestingStrategyDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Post Story and Testing Strategy Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
		
		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
		
		boolean isPostStory = workItemUpdated.isPostStoryMovie();
		boolean isTestingStrategy = workItemUpdated.isTestingStrategyStory();
		
		if(
				(isPostStory && storyWorkItem.findTestingStrategy(account).isStateDone())
				||
				isTestingStrategy && storyWorkItem.findPostStoryMovie(account).isStateDone()
		){
			VisualStudioRevisionForm testingStrategy = storyWorkItem.findTestingStrategy(account);
			VisualStudioWorkItem functionalTestOnIntegration = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
				.addTitle(VisualStudioTaskData.FUNCTIONAL_TEST_ON_INTEGRATION.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(testingStrategy.getIteration())
				.addAssignTo(testingStrategy.getAssignedToName())
				.addEstimate(VisualStudioTaskData.FUNCTIONAL_TEST_ON_INTEGRATION.defaultEstimate)
				.build();		
			
			VisualStudioWorkItem uiAutomatedTesting = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
				.addTitle(VisualStudioTaskData.UI_AUTOMATED_TESTING.value + " - B" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(testingStrategy.getIteration())
				.addAssignTo(testingStrategy.getAssignedToName())
				.addEstimate(VisualStudioTaskData.UI_AUTOMATED_TESTING.defaultEstimate)
				.build();	
			
			createANewTask(functionalTestOnIntegration, project, account, () -> dbBug.setFunctionalTestOnIntegration(true), () -> !dbBug.isFunctionalTestOnIntegration());
			createANewTask(uiAutomatedTesting, project, account, () -> dbBug.setUiAutomatedTesting(true), () -> !dbBug.isUiAutomatedTesting());
			workItemDao.saveOrUpdate(dbBug);
		}
		
		
		return returnMessage;
	}

	private String postStoryMovieDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Post Story movie Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm postStoryMvoie = storyWorkItem.findPostStoryMovie(account);
		VisualStudioWorkItem createANewBrachWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.APPROVE_STORY_MOVIE.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(postStoryMvoie.getIteration())
			.addAssignTo(UserDetails.ERIC_KISH.name)
			.addEstimate(VisualStudioTaskData.APPROVE_STORY_MOVIE.defaultEstimate)
			.build();		

		createANewTask(createANewBrachWorkItem, project, account, () -> dbBug.setApproveStoryMovie(true), () -> !dbBug.isApproveStoryMovie());
		workItemDao.saveOrUpdate(dbBug);
	
		return returnMessage;
	}
	
	private String approveStoryMovieDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Approve Story movie Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
		
		VisualStudioRevisionForm postStoryMovie = storyWorkItem.findPostStoryMovie(account);
		VisualStudioRevisionForm approveStoryMovie = storyWorkItem.findApproveStoryMovie(account);
		VisualStudioWorkItem createANewBrachWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.PULL_REQUEST_FOR_STORY.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(approveStoryMovie.getIteration())
			.addAssignTo(postStoryMovie.getAssignedToName())
			.addEstimate(VisualStudioTaskData.PULL_REQUEST_FOR_STORY.defaultEstimate)
			.build();		

		createANewTask(createANewBrachWorkItem, project, account, () -> dbBug.setPullRequestForStory(true), () -> !dbBug.isPullRequestForStory());
		workItemDao.saveOrUpdate(dbBug);
	
		return returnMessage;
	}

	private String pullRequestOrFunctionalTestOnIntegrationOrUiAutomatedtestingDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Merge To master Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
		
		boolean isFunctionalTestOnIntegration = workItemUpdated.isFunctionalTestOnIntegration();
		boolean isPullRequestForStory = workItemUpdated.isPullRequestForStory();
		boolean isUIAutomatedTesting = workItemUpdated.isUIAutomatedTesting();
		 
		if(
			(isFunctionalTestOnIntegration && storyWorkItem.findPullRequestForStory(account).isStateDone() && storyWorkItem.findUIAutomatedTesting(account).isStateDone())
			||
			(isPullRequestForStory && storyWorkItem.findFunctionalTestOnIntegration(account).isStateDone() && storyWorkItem.findUIAutomatedTesting(account).isStateDone())
			||
			(isUIAutomatedTesting && storyWorkItem.findFunctionalTestOnIntegration(account).isStateDone() && storyWorkItem.findPullRequestForStory(account).isStateDone())
		){
			VisualStudioRevisionForm pullRequestForStory = storyWorkItem.findPullRequestForStory(account);
			VisualStudioWorkItem mergeToMaster = new VisualStudioWorkItemBuilder()
					.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
					.addTitle(VisualStudioTaskData.MERGE_TO_MASTER_STORY.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
					.addStatus(VisualStudioTaskState.TO_DO)
					.addIteration(pullRequestForStory.getIteration())
					.addAssignTo(pullRequestForStory.getAssignedToName())
					.addEstimate(VisualStudioTaskData.MERGE_TO_MASTER_STORY.defaultEstimate)
					.build();
			createANewTask(mergeToMaster, project, account, () -> dbBug.setMergeToMasterStory(true), () -> !dbBug.isMergeToMasterStory()); 
		}
		
		return returnMessage;
	}

	private String mergeToMasterStoryDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Merge To master Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm mergeToMaster = storyWorkItem.findMergeToMasterStory(account);
		VisualStudioRevisionForm functionalTestOnIntegration = storyWorkItem.findFunctionalTestOnIntegration(account);
		VisualStudioWorkItem createANewBrachWorkItem = new VisualStudioWorkItemBuilder()
			.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
			.addTitle(VisualStudioTaskData.REBASE_INTEGRATION_TO_MASTER.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
			.addStatus(VisualStudioTaskState.TO_DO)
			.addIteration(mergeToMaster.getIteration())
			.addAssignTo(mergeToMaster.getAssignedToName())
			.addEstimate(VisualStudioTaskData.REBASE_INTEGRATION_TO_MASTER.defaultEstimate)
			.build();		
		
		VisualStudioWorkItem testOnMaster = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
				.addTitle(VisualStudioTaskData.TEST_ON_MASTER_STORY.value + " - B" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(functionalTestOnIntegration.getIteration())
				.addAssignTo(functionalTestOnIntegration.getAssignedToName())
				.addEstimate(VisualStudioTaskData.TEST_ON_MASTER_STORY.defaultEstimate)
				.build();		

		createANewTask(createANewBrachWorkItem, project, account, () -> dbBug.setRebaseIntegrationToMasterStory(true), () -> !dbBug.isRebaseIntegrationToMasterStory());
		createANewTask(testOnMaster, project, account, () -> dbBug.setTestOnMaster(true), () -> !dbBug.isTestOnMaster());
		
		workItemDao.saveOrUpdate(dbBug);
	
		return returnMessage;
	}
	
	private String testOnMasterDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Test on master Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
		
		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
		
		VisualStudioRevisionForm testOnMaster = storyWorkItem.findTestOnMasterStory(account);
		VisualStudioWorkItem createANewBrachWorkItem = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(storyWorkItem.getId()), property.getVSAccount())
				.addTitle(VisualStudioTaskData.APPROVE_FOR_RELEASE.value + " - S" + storyWorkItem.getId() + " - " + storyWorkItem.getTitle())
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(testOnMaster.getIteration())
				.addEstimate(VisualStudioTaskData.APPROVE_FOR_RELEASE.defaultEstimate)
				.build();		
		
		createANewTask(createANewBrachWorkItem, project, account, () -> dbBug.setApproveForRelease(true), () -> !dbBug.isApproveForRelease());
		
		workItemDao.saveOrUpdate(dbBug);
		
		return returnMessage;
	}

	private String approveForReleaseOrRebaseIntegrationToMasterDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Is Approved for Rlease & Rebase Integration to Master done process";
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		boolean isApproveForRelease = workItemUpdated.isApproveForRelease();
		boolean isRebaseIntegrationToMaster = workItemUpdated.isRebaseIntegrationToMaster();
		
		if(
				(isApproveForRelease && storyWorkItem.findRebaseIntegrationToMasterStory(account).isStateDone())
				||
				(isRebaseIntegrationToMaster &&  storyWorkItem.findApproveForRelease(account).isStateDone())
		){
			//Modified State to DONE
			VisualStudioWorkItem partialStoryItem = new VisualStudioWorkItemBuilder()
					.modifiedStatus(VisualStudioBugState.DONE)
					.build();
			//Update
			VisualStudioUtil.updateWorkItem(partialStoryItem, String.valueOf(storyWorkItem.getId()), workItemUpdated.getProject(), account);
		}
		
		return returnMessage;
	}
	
}
