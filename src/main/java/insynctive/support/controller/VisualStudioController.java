package insynctive.support.controller;

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
import insynctive.support.utils.VisualStudioUtil;
import insynctive.support.utils.slack.SlackMessage;
import insynctive.support.utils.slack.SlackUtil;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;
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
		returnMessage += "- Is Test Bug: "+workItemUpdated.isTestFix();
		returnMessage += "- Is Develop Fix: "+workItemUpdated.isDevelopFix();
		returnMessage += "- Was Change to approved: "+workItemUpdated.wasChangeToApprooved();
		returnMessage += "- Was Change to In Progress: "+workItemUpdated.wasChangeToInProgress();
		returnMessage += "- Was Change to Done: "+workItemUpdated.wasChangeToDone();
		
		if(workItemUpdated.isABug() && workItemUpdated.wasChangeToApprooved()){
			returnMessage = "Create 3 Tasks (Develop Fix - Test Fix - Merge to Master)";
			
			VisualStudioWorkItem developFixField = new VisualStudioWorkItemBuilder()
					.addParent(String.valueOf(workItemUpdated.getWorkItemID()))
					.addTitle("Develop Fix")
					.addStatus(VisualStudioTaskState.TO_DO)
					.addIteration(workItemUpdated.getIteration())
					.build();
			
			VisualStudioWorkItem testFixField = new VisualStudioWorkItemBuilder()
					.addParent(String.valueOf(workItemUpdated.getWorkItemID()))
					.addTitle("Test Fix")
					.addStatus(VisualStudioTaskState.TO_DO)
					.addIteration(workItemUpdated.getIteration())
					.build();
			
			VisualStudioWorkItem mergeToMasterField = new VisualStudioWorkItemBuilder()
					.addParent(String.valueOf(workItemUpdated.getWorkItemID()))
					.addTitle("Merge to Master")
					.addStatus(VisualStudioTaskState.TO_DO)
					.addIteration(workItemUpdated.getIteration())
					.build();
			
			//Check if the task were not assigend to the bug in the past.
			
			if(workItemDao.getByEntityID(workItemUpdated.getWorkItemID().toString(10)) == null){
				
				//Create 3 Tasks If not exists (TODO This will be change to DB track WorkItems creation)
				VisualStudioUtil.createNewTask(developFixField, workItemUpdated.getProject(), account); 
				VisualStudioUtil.createNewTask(testFixField, workItemUpdated.getProject(), account);
				VisualStudioUtil.createNewTask(mergeToMasterField, workItemUpdated.getProject(), account);

				//Save in DB the ID of bug.
				workItemDao.save(String.valueOf(workItemUpdated.getWorkItemID()));
			}
			
		} 
		
		else if(workItemUpdated.isATask() && workItemUpdated.isDevelopFix() && workItemUpdated.wasChangeToInProgress()){
			returnMessage = "Change Bug status to COMMITED.";

			//Get Bug Relation.
			VisualStudioRevisionForm bugRelation = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugRelation.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			//Modified State
			VisualStudioWorkItem bugWorkItem = new VisualStudioWorkItemBuilder()
					.modifiedStatus(VisualStudioWorkItemState.COMMITTED)
					.build();
			
			//Update
			VisualStudioUtil.updateWorkItem(bugWorkItem, String.valueOf(bugRelation.getId()), workItemUpdated.getProject(), account);
		} 
		
		else if(workItemUpdated.isATask() && workItemUpdated.isDevelopFix() && workItemUpdated.wasChangeToDone()){
			returnMessage = "Slack QA for testing.";
			
			//Get Bug Relation.
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			//Get Task named: 'Test Fix'
			VisualStudioRevisionForm testBugWorkItem = bugWorkItem.findTestFixTask(account);
			
			//Slack QA fort testing..
			SlackMessage message = new SlackMessageBuilder()
					.setIconEmoji(":bug:")
					.setUsername("Visual Studio Support")
					.setChannel(SlackUtil.getSlackAccountMentionByEmail(testBugWorkItem.getAssignedToEmail()))
					.setText("<"+workItemUpdated.getFirstRelation().getEditURL()+"| Bug #"+workItemUpdated.getFirstRelation().getRelationID()+"> - Has been fixed and is ready for you to test it.")
					.build();
			SlackUtil.sendMessage(message);
		} 
		
		else if(workItemUpdated.isATask() && workItemUpdated.isDevelopFix() && workItemUpdated.wasChangeFromDoneToToDo()){
			returnMessage = "Slack Developer for Re-Open";
			
			//Get Bug Relation.
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			//Get Task named: 'Develop Fix'
			VisualStudioRevisionForm developBugWorkItem = bugWorkItem.findDevelopFixTask(account);
			
			//Slack QA fort testing..
			SlackMessage message = new SlackMessageBuilder()
					.setIconEmoji(":hammer_and_wrench:")
					.setUsername("Visual Studio Support")
					.setChannel(SlackUtil.getSlackAccountMentionByEmail(developBugWorkItem.getAssignedToEmail()))
					.setText("<"+workItemUpdated.getFirstRelation().getEditURL()+"| Bug #"+workItemUpdated.getFirstRelation().getRelationID()+"> - Has been reopened, please take a look at it for more details.")
					.build();
			SlackUtil.sendMessage(message);
		} 
		
		else if(workItemUpdated.isATask() && workItemUpdated.isTestFix() && workItemUpdated.wasChangeToDone()){
			returnMessage = "Assign 'Merge to Master' to Developer";
			returnMessage +=  " and Slack Developer.";
			
			//Get Bug Relation.
			VisualStudioRevisionForm bugWorkItem = workItemUpdated.getFirstRelationFullObject(account);
			if(!bugWorkItem.isABug()) return "{\"status\" : 200, \"message\": \"Not a Bug\"}";
			
			//Get Task named: 'Develop Fix'
			VisualStudioRevisionForm developFix = bugWorkItem.findDevelopFixTask(account);
			
			//Get Task named: 'Merge to Master'
			String mergeToMasterID = bugWorkItem.findIDOfMergeToMasterTask(account);
			
			//Modified State
			VisualStudioWorkItem mergeToMasterTask= new VisualStudioWorkItemBuilder()
					.addAssignTo(developFix.getFields().getNameOfOwner())
					.build();
			
			//Update
			VisualStudioUtil.updateWorkItem(mergeToMasterTask, mergeToMasterID, workItemUpdated.getProject(), account);
			
			//Slack Developer fort Merge to master..
			SlackMessage message = new SlackMessageBuilder()
					.setIconEmoji(":bulb:")
					.setUsername("Visual Studio Support")
					.setChannel(SlackUtil.getSlackAccountMentionByEmail(developFix.getAssignedToEmail()))
					.setText("<"+workItemUpdated.getFirstRelation().getEditURL()+"| Bug #"+workItemUpdated.getFirstRelation().getRelationID()+"> - Has been tested and is ready for you to merge it to master.")
					.build();
			SlackUtil.sendMessage(message);
		}
		
		
		return "{\"status\" : 200, \"message\": \""+returnMessage+"\"}";
	}
}
