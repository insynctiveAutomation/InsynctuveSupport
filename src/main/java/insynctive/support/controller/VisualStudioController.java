package insynctive.support.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.support.form.vs.VisualStudioChangeFieldForm;
import insynctive.support.form.vs.VisualStudioChangeValue;
import insynctive.support.form.vs.VisualStudioFieldForm;
import insynctive.support.form.vs.VisualStudioForm;
import insynctive.support.form.vs.VisualStudioRelationsForm;
import insynctive.support.form.vs.VisualStudioResourceForm;
import insynctive.support.form.vs.VisualStudioRevisionForm;
import insynctive.support.utils.slack.SlackMessage;
import insynctive.support.utils.slack.SlackUtil;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;
import insynctive.support.utils.vs.VisualStudioField;
import insynctive.support.utils.vs.VisualStudioUtil;

@Controller
@RequestMapping(value = "/vs")
public class VisualStudioController {

	
	@RequestMapping(value = "/updateWorkItem" ,method = RequestMethod.POST)
	@ResponseBody
	public String restToState(@RequestBody VisualStudioForm vsForm) throws Exception{

		String returnMessage = "Is Task: "+vsForm.isATask();
		returnMessage += "- Is Bug: "+vsForm.isABug();
		returnMessage += "- Is Test Bug: "+vsForm.isTestBug();
		returnMessage += "- Is Develop Fix: "+vsForm.isDevelopFix();
		returnMessage += "- Was Change to approved: "+vsForm.wasChangeToApprooved();
		returnMessage += "- Was Change to In Progress: "+vsForm.wasChangeToInProgress();
		returnMessage += "- Was Change to Done: "+vsForm.wasChangeToDone();
		
		if(vsForm.isABug() && vsForm.wasChangeToApprooved()){
			// Create 3 Tasks (Develop Fix - Test Bug - Merge to Master)
			
		} else if(vsForm.isATask() && vsForm.isDevelopFix() && vsForm.wasChangeToInProgress()){
			//Change Bug status to COMMITED.
			
		} else if(vsForm.isATask() && vsForm.isDevelopFix() && vsForm.wasChangeToDone()){
			//"Slack QA for testing.
			returnMessage = "Slack QA for testing.";
			VisualStudioRevisionForm bugWorkItem = vsForm.getFirstRelationFullObject();
			VisualStudioRevisionForm testBugWorkItem = VisualStudioUtil.getWorkItem(bugWorkItem.findIDOfTestBugTask());
			
			SlackMessage message = new SlackMessageBuilder()
					.setIconEmoji(":trollface:")
					.setUsername("Visual Studio Support")
					.setChannel(SlackUtil.getSlackAccountMentionByEmail(testBugWorkItem.getAssignedToEmail()))
					.setText("Please Test this <"+vsForm.getFirstRelation().getEditURL()+"| Bug (#"+vsForm.getFirstRelation().getRelationID()+")> - "+vsForm.getNameOfOwner()+" finish the fix.")
					.build();
			SlackUtil.sendMessage(message);
			
		} else if(vsForm.isATask() && vsForm.isTestBug() && vsForm.wasChangeToDone()){
			// Assign 'Merge to Master' to Developer.
			// Slack Developer.
			
		}
		
		return "{\"status\" : 200, \"message\": \""+returnMessage+"\"}";
	}
	
	public static void main(String[] args) throws Exception {
		VisualStudioChangeValue changeValuestate = new VisualStudioChangeValue();
		changeValuestate.setNewValue("Done");
		changeValuestate.setOldValue("In Progress");
		
		VisualStudioChangeFieldForm fieldform = new VisualStudioChangeFieldForm();
		fieldform.setState(changeValuestate);
		
		VisualStudioFieldForm ff = new VisualStudioFieldForm();
		ff.setAssignedTo("Eugenio Valeiras <evaleiras@insynctive.com>");
		ff.setIteration("Insynctive\\February 2016\\FEB3-2016");
		ff.setState("Done");
		ff.setTitle("Develop Fix");
		ff.setType("Task");
		
		VisualStudioRelationsForm vsRelatioNForm1 = new VisualStudioRelationsForm();
		vsRelatioNForm1.setRel("System.LinkTypes.Hierarchy-Reverse");
		vsRelatioNForm1.setUrl("https://insynctive.visualstudio.com/DefaultCollection/_apis/wit/workItems/345");
		List<VisualStudioRelationsForm> list = new ArrayList<>();
		list.add(vsRelatioNForm1);
		
		VisualStudioRevisionForm revisionForm = new VisualStudioRevisionForm();
		revisionForm.setFields(ff);
		revisionForm.setRelations(list);
		
		VisualStudioResourceForm resource = new VisualStudioResourceForm();
		resource.setWorkItemId(BigInteger.valueOf(788));
		resource.setUrl("https://insynctive.visualstudio.com/DefaultCollection/_apis/wit/workItems/788/updates/8");
		resource.setFields(fieldform);
		resource.setRevision(revisionForm);
		
		VisualStudioForm form = new VisualStudioForm();
		form.setResource(resource);
		
		VisualStudioController controller = new VisualStudioController();
		System.out.println(controller.restToState(form));
		
	}
	
	
	
	
	
	
	
	
	
}
