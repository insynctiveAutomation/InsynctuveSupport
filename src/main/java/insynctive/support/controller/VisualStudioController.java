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


	//Manage Task Upadated
	private String manageUpdatedForTask(VisualStudioForm workItemUpdated, String account, String returnMessage) throws Exception {
		
		//Check if it was 'Test Strategy'
		if(workItemUpdated.isTestStrategy() && workItemUpdated.wasChangeToDone()) {

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
			String ownserOFAddAcceptanceCriteria = bugWorkItem.findTestStrategy(account).getNameOfOwner();
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
				.addAssignTo(bugWorkItem.getNameOfOwner() != null ? bugWorkItem.getNameOfOwner() : "")
				.build();
			
			createANewTask(dbBug, addAcceptanceCriteriaTask, project, account, () -> dbBug.setAddAcceptanceCriteria(true), () -> !dbBug.isAddAcceptanceCriteria());
			createANewTask(dbBug, createNewBranchWorkItem, project, account, () -> dbBug.setCreateANewBranch(true), () -> !dbBug.isCreateANewBranch());
			workItemDao.saveOrUpdate(dbBug);
			
		}
		
		//Create a New Branch was moved to DONE
		else if(workItemUpdated.isCreateANewBranch() && workItemUpdated.wasChangeToDone()){
			
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
				.addAssignTo(bugWorkItem.getNameOfOwner())
				.build();
			
			createANewTask(dbBug, reproduceWithAutomatedTestWorkItem, project, account, () -> dbBug.setReproduceWithAutomatedTest(true), () -> !dbBug.isReproduceWithAutomatedTest());
			workItemDao.saveOrUpdate(dbBug);
		}
		
		
		//Reproduce with automated tests moved to DONE
		else if(workItemUpdated.isReproduceWithAutomatedTest() && workItemUpdated.wasChangeToDone()){
			
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
				.addAssignTo(bugWorkItem.getNameOfOwner())
				.build();
			createANewTask(dbBug, developFixTask, project, account, () -> dbBug.setDevelopFix(true), () -> !dbBug.isDevelopFix());
			workItemDao.saveOrUpdate(dbBug);
			
		}
		
		//Develop Fix moved to DONE - TODO Need to make the build automatically
		else if (workItemUpdated.isDevelopFix() && workItemUpdated.wasChangeToDone()){
			returnMessage = "Develop Fix was change to DONE";
			String project = workItemUpdated.getProject();

			//Get Bug Relation.
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";

			//Get entity of DB
			VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString());
			
			//Find Owner of 'Find Test Strategy'
			String ownserOFAddAcceptanceCriteria = bugWorkItem.findTestStrategy(account).getNameOfOwner();
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
				.addAssignTo(bugWorkItem.getNameOfOwner())
				.addIteration(bugWorkItem.getIteration())
				.build();
			
			createANewTask(dbBug, functionalTestTask, project, account, () -> dbBug.setFunctionalTest(true), () -> !dbBug.isFunctionalTest());
			createANewTask(dbBug, getCodeReviewTask, project, account, () -> dbBug.setGetCodeReview(true), () -> !dbBug.isGetCodeReview());
			workItemDao.saveOrUpdate(dbBug);
		}
		
		//Get Code Review and functional Test were moved to DONE
		else if((workItemUpdated.isGetCodeReview() || workItemUpdated.isFunctionalTest() ) && workItemUpdated.wasChangeToDone()){

			String project = workItemUpdated.getProject();
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			Boolean isGetCodeReview = workItemUpdated.isGetCodeReview();
			Boolean isFunctionalTest = workItemUpdated.isFunctionalTest();
			
			if((isGetCodeReview && bugWorkItem.findFunctionalTest(account).isStateDone()) || (isFunctionalTest && bugWorkItem.findGetCodeReview(account).isStateDone())){
				
				returnMessage = "Create 'Merge to Master'";
				
				VisualStudioWorkItem mergeToMasterTask = new VisualStudioWorkItemBuilder()
					.addParent(String.valueOf(bugWorkItem.getId()))
					.addTitle(VisualStudioTaskName.MERGE_TO_MASTER.value)
					.addStatus(VisualStudioTaskState.TO_DO)
					.addAssignTo(bugWorkItem.getNameOfOwner())
					.addIteration(bugWorkItem.getIteration())
					.build();

				VisualStudioWorkItemEntity dbBug = workItemDao.getByEntityID(bugWorkItem.getId().toString()); 
				createANewTask(dbBug, mergeToMasterTask, project, account, () -> dbBug.setMergeToMaster(true), () -> !dbBug.isMergeToMaster());
				workItemDao.saveOrUpdate(dbBug);
			}
		}
		
		//Merge to Master was moved to DONE - TODO Run Build 
		else if(workItemUpdated.isMergeToMaster() && workItemUpdated.wasChangeToDone()){
			
			returnMessage = "Create 'Done done test' and 'Rebase integration to master'";
			String project = workItemUpdated.getProject();
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			VisualStudioWorkItem rebaseIntegrationToMasterTask = new VisualStudioWorkItemBuilder()
				.addParent(String.valueOf(bugWorkItem.getId()))
				.addTitle(VisualStudioTaskName.REABASE_INTEGRATION_TO_MASTER.value)
				.addStatus(VisualStudioTaskState.TO_DO)
				.addIteration(bugWorkItem.getIteration())
				.addAssignTo(bugWorkItem.getNameOfOwner())
				.build();
			
			
			//Find Owner of 'Test Strategy'
			String ownserOFAddAcceptanceCriteria = bugWorkItem.findTestStrategy(account).getNameOfOwner();
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
		}
		
		else if((workItemUpdated.isRebaseIntegrationToMaster() || workItemUpdated.isTestOnMaster())  && workItemUpdated.wasChangeToDone()){
			
			String project = workItemUpdated.getProject();
			
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			Boolean isRebaseIntegrationToMaster = workItemUpdated.isRebaseIntegrationToMaster();
			Boolean isTestOnMaster = workItemUpdated.isTestOnMaster();
			
			if((isRebaseIntegrationToMaster && bugWorkItem.findDoneDoneTest(account).isStateDone()) || (isTestOnMaster && bugWorkItem.findRebaseIntegrationToMaster(account).isStateDone())){
				//Modified State to DONE
				VisualStudioWorkItem partialBugItem = new VisualStudioWorkItemBuilder()
						.modifiedStatus(VisualStudioWorkItemState.DONE)
						.build();
				//Update
				VisualStudioUtil.updateWorkItem(partialBugItem, String.valueOf(bugWorkItem.getId()), workItemUpdated.getProject(), account);
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
			
			returnMessage = "Create 2 Tasks (Create a new Branch - Test Strategy)";
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
				//Save in DB the ID of bug.
			}
		}
		return returnMessage;
	}
	
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
