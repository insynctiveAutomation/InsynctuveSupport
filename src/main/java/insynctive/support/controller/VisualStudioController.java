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
import insynctive.support.model.VisualStudioWorkItemEntity;
import insynctive.support.utils.Property;
import insynctive.support.utils.UserDetails;
import insynctive.support.utils.slack.SlackMessage;
import insynctive.support.utils.slack.SlackMessageObject;
import insynctive.support.utils.slack.SlackUtilInsynctive;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;
import support.form.vs.VisualStudioForm;
import support.form.vs.VisualStudioRevisionForm;
import support.utils.vs.VisualStudioBugState;
import support.utils.vs.VisualStudioTaskData;
import support.utils.vs.VisualStudioTaskState;
import support.utils.vs.VisualStudioWorkItem;
import support.utils.vs.builder.VisualStudioWorkItemBuilder;
import support.utils.vs.master.VisualStudioUtil;

@Controller
@RequestMapping(value = "/vs")
public class VisualStudioController {

	private final WorkItemDao workItemDao;
	private final Property property;
	private VisualStudioUtil vsUtil = new VisualStudioUtil("evaleiras@insynctive.com", "d5bb3o6xbmecnqgsa3vgequknbu3qyv7zf3shdbtijrwrpmhauwq");
	
	
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
		
		//IF is a STORY
		if(workItemUpdated.isAStory()){
			returnMessage = manageUpdateForStory(workItemUpdated, account);
		}

		//IF is a BUG
		if(workItemUpdated.isABug()){
			//Manage Updated For Individual BUG
			if(workItemUpdated.noHaveParent()) {
				returnMessage = manageUpdatedForIndependentBug(workItemUpdated, account, returnMessage);
			}
			
			//Create Slack Channel when bug is moved to CRITICAL
			if(workItemUpdated.changedToCritical()) {
				SlackUtilInsynctive.createNewChannel("Critical-"+workItemUpdated.getTitle());
			}
			
			//Remove Slack Channel when Critical Bug is moved to DONE
			if(workItemUpdated.criticalChangeStateToDone()) {
				SlackUtilInsynctive.archiveChannel("Critical-"+workItemUpdated.getTitle());
			}
		}
		
		//IF is a TASK
		if(workItemUpdated.isATask()) {
			
			VisualStudioRevisionForm parentFullObject = workItemUpdated.getParentFullObject(vsUtil, account);

			//Notify when the task change the assignation.
			if(workItemUpdated.changedAssignation()){
				alertAssignation(workItemUpdated, account); 
			}
			
			//Task in Independent BUG Workflow 
			if(parentFullObject != null && parentFullObject.isABug() && parentFullObject.noHaveParent()){
				returnMessage = manageUpdatedForTaskInIndependentBug(workItemUpdated, account, returnMessage);
			}
			
			//Task in Story BUG Workflow
			if(parentFullObject != null && parentFullObject.isAStory()){
				returnMessage = manageUpdatedForTaskInStory(workItemUpdated, account);
			}
		}
		
		return "{\"status\" : 200, \"message\": \""+returnMessage+"\"}";
	}

	//Independent Bug WorkFlow
	private String manageUpdatedForIndependentBug(VisualStudioForm workItemUpdated, String account, String returnMessage) throws Exception {
			
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
			VisualStudioWorkItemEntity byEntityID = workItemDao.getByEntityID(workItemUpdated.getWorkItemID());
			if(byEntityID == null || byEntityID.isInvestigateBug()){
				VisualStudioWorkItemEntity dbBug = new VisualStudioWorkItemEntity(); 
				dbBug.setWorkItemID(workItemUpdated.getWorkItemID());
				createANewTask(investigateBugWorkItem, project, account, () -> dbBug.setInvestigateBug(true), () -> !dbBug.isInvestigateBug());
				workItemDao.save(dbBug);

			}
			
			//Slack QA for create test strategy..
			SlackMessageObject message = new SlackMessageBuilder()
				.setIconEmoji(SlackMessage.BUG_APPROVED.img)
				.setUsername(SlackMessage.BUG_APPROVED.senderName)
				.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(workItemUpdated.getAssignedToEmail()))
				.setText(String.format(SlackMessage.BUG_APPROVED.message, vsUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), workItemUpdated.getWorkItemID()))
				.build();
			SlackUtilInsynctive.sendMessage(message);
			
		}
		return returnMessage;
	}
	
	//Task of Independent bug Workflow
	private String manageUpdatedForTaskInIndependentBug(VisualStudioForm workItemUpdated, String account, String returnMessage) throws Exception {
		
		//Test Strategy was moved to DONE
		if(workItemUpdated.isInvestigateBug() && workItemUpdated.wasChangedToDoneOrRemoved()) {
			returnMessage = investigateBugDoneOrRemovedProcess(workItemUpdated, account);
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
		else if(workItemUpdated.isReproduceWithAutomatedTest() && workItemUpdated.wasChangedToDoneOrRemoved()){
			returnMessage = reproduceWithAutomatedTestsDoneOrRemovedProcess(workItemUpdated, account);
		}
		
		//Develop Fix was moved to DONE - TODO Need to make the build automatically
		else if (workItemUpdated.isDevelopFix() && workItemUpdated.changedToDone()){
			returnMessage = developFixDoneProcess(workItemUpdated, account); 
		}
		
		//Get Code Review and functional Test were moved to DONE
		else if((workItemUpdated.isGetCodeReview() || workItemUpdated.isFunctionalTest() ) && workItemUpdated.changedToDone()){

			String project = workItemUpdated.getProject();
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			Boolean isGetCodeReview = workItemUpdated.isGetCodeReview();
			Boolean isFunctionalTest = workItemUpdated.isFunctionalTest();

			VisualStudioRevisionForm functionaltest = bugWorkItem.findFunctionalTest(vsUtil, account);
			VisualStudioRevisionForm getCodeReview = bugWorkItem.findGetCodeReview(vsUtil, account);
			
			if((isGetCodeReview && functionaltest.isStateDone()) || (isFunctionalTest && getCodeReview != null && getCodeReview.isStateDone())){
				
				returnMessage = getCodeReviewAndFunctionalTestDoneProcess(account, project, bugWorkItem);
			
			} else if(isFunctionalTest && bugWorkItem.findInvestigateBug(vsUtil, account).isStateRemoved()){
				
				returnMessage = functionalTestDoneAndInvestigateBugRemoved(account, project, bugWorkItem);
				
			} else {
			
				if(isGetCodeReview && !functionaltest.isStateDone()){
					SlackMessageObject message = new SlackMessageBuilder()
						.setIconEmoji(SlackMessage.GET_CODE_REVIEW_DONE_FUNCTIONAL_TEST_NOT.img)
						.setUsername(SlackMessage.GET_CODE_REVIEW_DONE_FUNCTIONAL_TEST_NOT.senderName)
						.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(functionaltest.getAssignedToEmail()))
						.setText(String.format(SlackMessage.GET_CODE_REVIEW_DONE_FUNCTIONAL_TEST_NOT.message, vsUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
						.build();
					SlackUtilInsynctive.sendMessage(message);
				}
	
				if(isFunctionalTest && !getCodeReview.isStateDone()){
					SlackMessageObject message = new SlackMessageBuilder()
							.setIconEmoji(SlackMessage.FUNCTIONAL_TEST_DONE_GET_CODE_REVIEW_NOT.img)
							.setUsername(SlackMessage.FUNCTIONAL_TEST_DONE_GET_CODE_REVIEW_NOT.senderName)
							.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(getCodeReview.getAssignedToEmail()))
							.setText(String.format(SlackMessage.FUNCTIONAL_TEST_DONE_GET_CODE_REVIEW_NOT.message, vsUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
							.build();
						SlackUtilInsynctive.sendMessage(message);
				}
				
			}
			
		}
		
		//Merge to Master was moved to DONE - TODO Run Build 
		else if(workItemUpdated.isMergeToMaster() && workItemUpdated.changedToDone()){
			returnMessage = mergeToMasterBugDoneProcess(workItemUpdated, account);
		}
		
		//Rebase Integration to Master and Test on Master were move to Done
		else if((workItemUpdated.isRebaseIntegrationToMaster() || workItemUpdated.isTestOnMaster())  && workItemUpdated.changedToDone()){
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			Boolean isRebaseIntegrationToMaster = workItemUpdated.isRebaseIntegrationToMaster();
			Boolean isTestOnMaster = workItemUpdated.isTestOnMaster();
			
			if((isRebaseIntegrationToMaster && bugWorkItem.findTestOnMasterBug(vsUtil, account).isStateDone()) || (isTestOnMaster && bugWorkItem.findRebaseIntegrationToMaster(vsUtil, account).isStateDone())){
				rebaseIntegrationToMasterAndTestOnMasterDoneProcess(workItemUpdated, account, bugWorkItem);
			}
		}
		return returnMessage;
	}

	//Story Workflow
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
	
	//Task in Story WorkFlow
	private String manageUpdatedForTaskInStory(VisualStudioForm workItemUpdated, String account) throws Exception {
		
		String returnMessage = "";
		
		//Test Strategy was moved to DONE
		if(workItemUpdated.isEstimateStory() && workItemUpdated.changedToDone()) {
			returnMessage = estimateStoryDoneProcess(workItemUpdated, account);
		}

		//Create a new branch was moved to DONE
		if(workItemUpdated.isCreateANewBranch() && workItemUpdated.changedToDone()) {
			returnMessage = createANewBranchDoneProcess(workItemUpdated, account);
		}
		
		//Develop TDD was moved to DONE
		if(workItemUpdated.isDevelopTDD() && workItemUpdated.changedToDone()) {
			returnMessage = developTDDDoneProcess(workItemUpdated, account);
		}
		
		//Develop Code Story was moved to DONE
		if(workItemUpdated.isDevelopCodeStory() && workItemUpdated.changedToDone()) {
			returnMessage = developCodeStoryDoneProcess(workItemUpdated, account);
		}
		
		//Post story movie or Testing Strategy were moved to DONE
		if((workItemUpdated.isPostStoryMovie() || workItemUpdated.isTestingStrategyStory()) && workItemUpdated.changedToDone()) {
			returnMessage = postStoryMovieOrTestingStrategyDoneProcess(workItemUpdated, account);
		}
		
		//Post Story Movie was moved to DONE
		if(workItemUpdated.isPostStoryMovie() && workItemUpdated.changedToDone()) {
			returnMessage = postStoryMovieDoneProcess(workItemUpdated, account);
		}
		
		//Approve Story movie was moved to DONE
		if(workItemUpdated.isApproveStoryMovie() && workItemUpdated.changedToDone()) {
			returnMessage = approveStoryMovieDoneProcess(workItemUpdated, account);
		}
		
		//Functional test on Integration was moved to DONE
		if((workItemUpdated.isFunctionalTestOnIntegration() || workItemUpdated.isPullRequestForStory() || workItemUpdated.isUIAutomatedTesting()) && workItemUpdated.changedToDone()) {
			returnMessage = pullRequestOrFunctionalTestOnIntegrationOrUiAutomatedtestingDoneProcess(workItemUpdated, account);
		}

		//Merge to master was moved to DONE
		if(workItemUpdated.isMergeToMaster() && workItemUpdated.changedToDone()) {
			returnMessage = mergeToMasterStoryDoneProcess(workItemUpdated, account);
		}

		//Test on Master was moved to DONE
		if(workItemUpdated.isTestOnMasterStory() && workItemUpdated.changedToDone()) {
			returnMessage = testOnMasterDoneProcess(workItemUpdated, account);
		}
		
		//Approve for release or rebase integration on master was moved to DONE
		if((workItemUpdated.isApproveForRelease() || workItemUpdated.isRebaseIntegrationToMaster()) && workItemUpdated.changedToDone()) {
			returnMessage = approveForReleaseOrRebaseIntegrationToMasterDoneProcess(workItemUpdated, account);
		}
		
		return returnMessage;
	}

	private void rebaseIntegrationToMasterAndTestOnMasterDoneProcess(VisualStudioForm workItemUpdated, String account, VisualStudioRevisionForm bugWorkItem) throws IOException, URISyntaxException, Exception {
		//Modified State to DONE
		VisualStudioWorkItem partialBugItem = new VisualStudioWorkItemBuilder()
				.modifiedStatus(VisualStudioBugState.DONE)
				.build();
		//Update
		vsUtil.updateWorkItem(partialBugItem, String.valueOf(bugWorkItem.getId()), workItemUpdated.getProject(), account);
	}

	private String mergeToMasterBugDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception, IOException, URISyntaxException {
		String project = workItemUpdated.getProject();
		
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString()); 

		if(bugWorkItem.findInvestigateBug(vsUtil, account).isStateRemoved()){
			
			//Modified State to DONE
			VisualStudioWorkItem partialBugItem = new VisualStudioWorkItemBuilder()
					.modifiedStatus(VisualStudioBugState.DONE)
					.build();
			//Update
			vsUtil.updateWorkItem(partialBugItem, String.valueOf(bugWorkItem.getId()), workItemUpdated.getProject(), account);
			
			return "Bug change status to DONE";

		} else {
			
			//Find Owner of 'Functional Test'
			VisualStudioRevisionForm functionalTest = bugWorkItem.findFunctionalTest(vsUtil, account);

			VisualStudioWorkItem rebaseIntegrationToMasterTask = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
				.addTitle(VisualStudioTaskData.REBASE_INTEGRATION_TO_MASTER.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle() )
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(workItemUpdated.getIteration())
				.addAssignTo(workItemUpdated.getAssignedToName()) //workItemUpdated = Merge to Master
				.addEstimate(VisualStudioTaskData.REBASE_INTEGRATION_TO_MASTER.defaultEstimate)
				.build();
			
			VisualStudioWorkItem doneDoneTestTask = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
				.addTitle(VisualStudioTaskData.TEST_ON_MASTER_BUG.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(workItemUpdated.getIteration())
				.addAssignTo(functionalTest != null ? functionalTest.getAssignedToName() : bugWorkItem.getCreatedByName())
				.addEstimate(VisualStudioTaskData.TEST_ON_MASTER_BUG.defaultEstimate)
				.build();
	
			createANewTask(rebaseIntegrationToMasterTask, project, account, () -> dbBug.setRebaseIntegrationToMaster(true), () -> !dbBug.isRebaseIntegrationToMaster());
			createANewTask(doneDoneTestTask, project, account, () -> dbBug.setDoneDoneTest(true), () -> !dbBug.isDoneDoneTest());
			workItemDao.saveOrUpdate(dbBug);
			
			//Slack QA for Functional Test
			SlackMessageObject message = new SlackMessageBuilder()
				.setIconEmoji(SlackMessage.MERGE_TO_MASTER_DONE.img)
				.setUsername(SlackMessage.MERGE_TO_MASTER_DONE.senderName)
				.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(functionalTest.getAssignedToEmail()))
				.setText(String.format(SlackMessage.MERGE_TO_MASTER_DONE.message, vsUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
				.build();
			SlackUtilInsynctive.sendMessage(message);
					
			return "Create 'Done done test' and 'Rebase integration to master'";
		}
	}

	private String getCodeReviewAndFunctionalTestDoneProcess(String account, String project, VisualStudioRevisionForm bugWorkItem) throws Exception, IOException, URISyntaxException {
		String returnMessage = "Create 'Merge to Master'";
		
		VisualStudioRevisionForm getCodeReview = bugWorkItem.findGetCodeReview(vsUtil, account);
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
			.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(getCodeReview.getAssignedToEmail()))
			.setText(String.format(SlackMessage.FUNCTIONAL_TEST_AND_CODE_REVIEW_DONE.message, vsUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
			.build();
		SlackUtilInsynctive.sendMessage(message);
				
		return returnMessage;
	}

	private String developFixDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception, IOException, URISyntaxException {
		String returnMessage = "Develop Fix was change to DONE";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString());

		if(bugWorkItem.findInvestigateBug(vsUtil, account).isStateRemoved()){
		
			VisualStudioWorkItem functionalTestWorkItem = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
				.addTitle(VisualStudioTaskData.FUNCTIONAL_TEST.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(bugWorkItem.getIteration())
				.addAssignTo(bugWorkItem.getCreatedByName())
				.addEstimate(VisualStudioTaskData.FUNCTIONAL_TEST.defaultEstimate)
				.build();
			
			createANewTask(functionalTestWorkItem, project, account, () -> dbBug.setFunctionalTest(true), () -> !dbBug.isFunctionalTest());
			workItemDao.saveOrUpdate(dbBug);
			
			//Slack QA for Functional Test
			SlackMessageObject message = new SlackMessageBuilder()
				.setIconEmoji(SlackMessage.DEVELOP_FIX_DONE.img)
				.setUsername(SlackMessage.DEVELOP_FIX_DONE.senderName)
				.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(bugWorkItem.getCreatedByEmail() != null ? bugWorkItem.getCreatedByEmail() : ""))
				.setText(String.format(SlackMessage.DEVELOP_FIX_DONE.message, vsUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
				.build();
			SlackUtilInsynctive.sendMessage(message);
			
		} else {
			
			//Find Owner of 'Test Strategy'
			VisualStudioRevisionForm testStrategy = bugWorkItem.findTestStrategy(vsUtil, account);
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
				.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(testStrategy != null ? testStrategy.getAssignedToEmail() : bugWorkItem.getCreatedByEmail()))
				.setText(String.format(SlackMessage.DEVELOP_FIX_DONE.message, vsUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
				.build();
			SlackUtilInsynctive.sendMessage(message);
		}
		
		return returnMessage;
	}

	private String reproduceWithAutomatedTestsDoneOrRemovedProcess(VisualStudioForm workItemUpdated, String account)
			throws Exception, IOException, URISyntaxException {
		String returnMessage = "Reproduce With automated Test Change to DONE";
		String project = workItemUpdated.getProject();

		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
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
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
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
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
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

		VisualStudioRevisionForm investigateBug = bugWorkItem.findInvestigateBug(vsUtil, account);
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
			.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(bugWorkItem.getAssignedToEmail()))
			.setText(String.format(SlackMessage.TEST_STRATEGY_DONE.message, vsUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
			.build();
		SlackUtilInsynctive.sendMessage(message);
		
		return returnMessage;
	}

	private String investigateBugDoneOrRemovedProcess(VisualStudioForm workItemUpdated, String account) throws Exception, IOException, URISyntaxException {
		String returnMessage = "Investigate Bug moved to Done or Removed";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm bugWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString());
		
		
		//Modified State to COMMITED
		VisualStudioWorkItem updatedItem = new VisualStudioWorkItemBuilder()
			.modifiedStatus(VisualStudioBugState.COMMITTED)
			.build();
		//Update
		vsUtil.updateWorkItem(updatedItem, String.valueOf(bugWorkItem.getId()), workItemUpdated.getProject(), account);
		if(workItemUpdated.changedToDone()){
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
					.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(bugWorkItem.getCreatedByEmail()))
					.setText(String.format(SlackMessage.INVESTIGATE_BUG_DONE.message, vsUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
					.build();
			SlackUtilInsynctive.sendMessage(message);
		} 
		
		if(workItemUpdated.changedToRemoved()){
			VisualStudioWorkItem developFixWorkItem = new VisualStudioWorkItemBuilder()
					.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
					.addTitle(VisualStudioTaskData.DEVELOP_FIX.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
					.addStatus(VisualStudioTaskState.TO_DO)
					.addIteration(bugWorkItem.getIteration())
					.addAssignTo(workItemUpdated.getAssignedToName())
					.addEstimate(VisualStudioTaskData.DEVELOP_FIX.defaultEstimate)
					.build();		
			
			createANewTask(developFixWorkItem, project, account, () -> dbBug.setDevelopFix(true), () -> !dbBug.isDevelopFix());
			workItemDao.saveOrUpdate(dbBug);
		}
		
		
		return returnMessage;
	}
	
	private void createANewTask(VisualStudioWorkItem task, String project, String account, setValue setValueIfSuccess, isValue preChecker) throws IOException, URISyntaxException {
		if(preChecker.evaluate() != null && preChecker.evaluate()){
			Boolean statusCreate = vsUtil.createNewTask(task, project, account);
			if(statusCreate) {
				setValueIfSuccess.evaluate();
			}
		}
	}
	
	private String estimateStoryDoneProcess(VisualStudioForm workItemUpdated, String account) throws Exception {
		String returnMessage = "Estimate Story Done Process";
		String project = workItemUpdated.getProject();
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
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
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm createNewBranch = storyWorkItem.findCreateNewBranch(vsUtil, account);
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
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm developTDD = storyWorkItem.findDevelopTDD(vsUtil, account);
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
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm developCodeForStory = storyWorkItem.findDevelopCodeForStory(vsUtil, account);
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
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
		
		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
		
		boolean isPostStory = workItemUpdated.isPostStoryMovie();
		boolean isTestingStrategy = workItemUpdated.isTestingStrategyStory();
		
		if(
				(isPostStory && storyWorkItem.findTestingStrategy(vsUtil, account).isStateDone())
				||
				isTestingStrategy && storyWorkItem.findPostStoryMovie(vsUtil, account).isStateDone()
		){
			VisualStudioRevisionForm testingStrategy = storyWorkItem.findTestingStrategy(vsUtil, account);
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
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm postStoryMvoie = storyWorkItem.findPostStoryMovie(vsUtil, account);
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
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
		
		VisualStudioRevisionForm postStoryMovie = storyWorkItem.findPostStoryMovie(vsUtil, account);
		VisualStudioRevisionForm approveStoryMovie = storyWorkItem.findApproveStoryMovie(vsUtil, account);
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
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
		
		boolean isFunctionalTestOnIntegration = workItemUpdated.isFunctionalTestOnIntegration();
		boolean isPullRequestForStory = workItemUpdated.isPullRequestForStory();
		boolean isUIAutomatedTesting = workItemUpdated.isUIAutomatedTesting();
		 
		if(
			(isFunctionalTestOnIntegration && storyWorkItem.findPullRequestForStory(vsUtil, account).isStateDone() && storyWorkItem.findUIAutomatedTesting(vsUtil, account).isStateDone())
			||
			(isPullRequestForStory && storyWorkItem.findFunctionalTestOnIntegration(vsUtil, account).isStateDone() && storyWorkItem.findUIAutomatedTesting(vsUtil, account).isStateDone())
			||
			(isUIAutomatedTesting && storyWorkItem.findFunctionalTestOnIntegration(vsUtil, account).isStateDone() && storyWorkItem.findPullRequestForStory(vsUtil, account).isStateDone())
		){
			VisualStudioRevisionForm pullRequestForStory = storyWorkItem.findPullRequestForStory(vsUtil, account);
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
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
				
		VisualStudioRevisionForm mergeToMaster = storyWorkItem.findMergeToMasterStory(vsUtil, account);
		VisualStudioRevisionForm functionalTestOnIntegration = storyWorkItem.findFunctionalTestOnIntegration(vsUtil, account);
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
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
		
		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(storyWorkItem.getId().toString());
		
		VisualStudioRevisionForm testOnMaster = storyWorkItem.findTestOnMasterStory(vsUtil, account);
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
		
		//Get Bug Relation.
		VisualStudioRevisionForm storyWorkItem = workItemUpdated.getParentFullObject(vsUtil, account);
		if(!storyWorkItem.isAStory()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

		boolean isApproveForRelease = workItemUpdated.isApproveForRelease();
		boolean isRebaseIntegrationToMaster = workItemUpdated.isRebaseIntegrationToMaster();
		
		if(
				(isApproveForRelease && storyWorkItem.findRebaseIntegrationToMasterStory(vsUtil, account).isStateDone())
				||
				(isRebaseIntegrationToMaster &&  storyWorkItem.findApproveForRelease(vsUtil, account).isStateDone())
		){
			//Modified State to DONE
			VisualStudioWorkItem partialStoryItem = new VisualStudioWorkItemBuilder()
					.modifiedStatus(VisualStudioBugState.DONE)
					.build();
			//Update
			vsUtil.updateWorkItem(partialStoryItem, String.valueOf(storyWorkItem.getId()), workItemUpdated.getProject(), account);
		}
		
		return "Is Approved for Release & Rebase Integration to Master done process";
	}

	private String functionalTestDoneAndInvestigateBugRemoved(String account, String project, VisualStudioRevisionForm bugWorkItem) throws Exception {
		
		VisualStudioRevisionForm functionalTest = bugWorkItem.findFunctionalTest(vsUtil, account);
		VisualStudioRevisionForm developFix = bugWorkItem.findDevelopFix(vsUtil, account);
		
		//Get entity of DB
		VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityIDIfNotExistCreate(bugWorkItem.getId().toString());
		
		VisualStudioWorkItem functionalTestWorkItem = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(bugWorkItem.getId()), property.getVSAccount())
				.addTitle(VisualStudioTaskData.MERGE_TO_MASTER_BUG.value + " - B" + bugWorkItem.getId() + " - " + bugWorkItem.getTitle())
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(functionalTest.getIteration())
				.addAssignTo(developFix.getAssignedToName())
				.addEstimate(VisualStudioTaskData.FUNCTIONAL_TEST.defaultEstimate)
				.build();
			
			createANewTask(functionalTestWorkItem, project, account, () -> dbBug.setMergeToMaster(true), () -> !dbBug.isMergeToMaster());
			workItemDao.saveOrUpdate(dbBug);
			
			//Slack QA for Functional Test
			SlackMessageObject message = new SlackMessageBuilder()
				.setIconEmoji(SlackMessage.FUNCTIONAL_TEST_DONE.img)
				.setUsername(SlackMessage.FUNCTIONAL_TEST_DONE.senderName)
				.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(bugWorkItem.getCreatedByEmail() != null ? bugWorkItem.getCreatedByEmail() : ""))
				.setText(String.format(SlackMessage.FUNCTIONAL_TEST_DONE.message, vsUtil.getVisualWorkItemUrl(bugWorkItem.getId().toString(), project, account), bugWorkItem.getId()))
				.build();
			SlackUtilInsynctive.sendMessage(message);
			
		return "Functional Test Done when Investigate Bug was removed";
	}
	
	
	//Alert on Slack when Assign a task.
	private void alertAssignation(VisualStudioForm workItemUpdated, String account) throws IOException, Exception {
		//Slack Assigned..
		String project = workItemUpdated.getProject();
		String text = workItemUpdated.getOldAssigned() != null ?
				String.format(SlackMessage.ASSIGNED_TO_WORK_ITEM.message, 
						vsUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), 
						workItemUpdated.getType(), 
						workItemUpdated.getWorkItemID(), 
						workItemUpdated.getOldAssignedName()) 
				:
				String.format(SlackMessage.NEW_ASSIGNED_TO_WORK_ITEM.message, 
						vsUtil.getVisualWorkItemUrl(workItemUpdated.getWorkItemID().toString(), project, account), 
						workItemUpdated.getType(), 
						workItemUpdated.getWorkItemID());
		
		SlackMessageObject message = new SlackMessageBuilder()
			.setIconEmoji(SlackMessage.ASSIGNED_TO_WORK_ITEM.img)
			.setUsername(SlackMessage.ASSIGNED_TO_WORK_ITEM.senderName)
			.setChannel(SlackUtilInsynctive.getSlackAccountMentionByEmail(workItemUpdated.getAssignedToEmail()))
			.setText(text)
			.build();
		SlackUtilInsynctive.sendMessage(message);
	}
	
	//Lambda Methods
	interface setValue {
			void evaluate();
		}
	interface isValue {
			Boolean evaluate();
		}
}
