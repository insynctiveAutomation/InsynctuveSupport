package insynctive.support.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.support.dao.TargetProcessAndIntercomDao;
import insynctive.support.form.intercom.IntercomForm;
import insynctive.support.form.tp.TargetProcessForm;
import insynctive.support.model.TargetProcessIntercomEntity;
import insynctive.support.utils.IntercomUtil;
import insynctive.support.utils.UserDetails;
import insynctive.support.utils.VisualStudioUtil;
import insynctive.support.utils.intercom.IntercomNote;
import insynctive.support.utils.vs.VisualStudioBugState;
import insynctive.support.utils.vs.VisualStudioTaskName;
import insynctive.support.utils.vs.VisualStudioTaskState;
import insynctive.support.utils.vs.VisualStudioWorkItem;
import insynctive.support.utils.vs.builder.VisualStudioWorkItemBuilder;
import io.intercom.api.AdminCollection;
import io.intercom.api.ConversationPart;
import io.intercom.api.ConversationPartCollection;

@Controller
@RequestMapping("/intercom")
public class IntercomController {

	private final TargetProcessAndIntercomDao tpDao;
	
	@Inject
	public IntercomController(TargetProcessAndIntercomDao tpDao) {
		this.tpDao = tpDao;
	}
	
	@RequestMapping(value = "/newRequest" ,method = RequestMethod.POST)
	@ResponseBody
	public String newRequest(@RequestBody TargetProcessForm form) throws JSONException, IOException{
		
		TargetProcessIntercomEntity entity = new TargetProcessIntercomEntity();
		entity.addValuesFromTPRequest(form);
		tpDao.save(entity);
		
		IntercomUtil.makeACommentInConversation("Your request was submitted to QA for Triage under the number: "+form.getEntityID(), form.getIntercomID());
		IntercomUtil.makeANoteInConversation("A request was created in TP.  https://insynctive.tpondemand.com/entity/"+form.getEntityID(), form.getIntercomID());
		
		return "{\"status\" : 200}";
	}
	
	@RequestMapping(value = "/createNote" ,method = RequestMethod.POST)
	@ResponseBody
	public String createNote(@RequestBody IntercomNote form) throws JSONException, IOException{

		TargetProcessIntercomEntity entity = tpDao.getByEntityID(form.getEntityID());
		if(entity != null){
			IntercomUtil.makeANoteWithAuthor(form.getBody(), entity.getIntercomID(), IntercomUtil.findAdminByEmail(form.getUserEmail()));
			return "{\"status\" : 200}";
		} else {
			return "{\"status\" : 404, \"cause\" : \"No entity with ID "+form.getEntityID()+"\"}";
		}
	}
	
	@RequestMapping(value = "/changeStatus" ,method = RequestMethod.POST)
	@ResponseBody
	public String changeStatus(@RequestBody TargetProcessForm form) throws JSONException, IOException{
		TargetProcessIntercomEntity entity = tpDao.getByEntityID(form.getEntityID());
		
		boolean isEntityInDB = entity != null;
		boolean isStatusChange = isEntityInDB && !entity.getStatus().equals(form.getStatus());
		
		if(isEntityInDB && isStatusChange){
			tpDao.updateStatus(entity, form.getStatus());

			IntercomUtil.makeACommentInConversation("Your request changed status to "+form.getStatus(), entity.getIntercomID());
			
			return "{\"status\" : 200}";
		} else if(!isEntityInDB){
			return "{\"status\" : 404, \"cause\" : \"No entity with ID "+form.getEntityID()+"\"}";
		} else if(!isStatusChange){
			return "{\"status\" : 404, \"cause\" : \"The entity not change the status\"}";
		} else {
			return "{\"status\" : 404}";
		}
	}
	
	@RequestMapping(value = "/report/{account}/{project}" ,method = RequestMethod.POST)
	@ResponseBody
	public String report(@PathVariable String account, @PathVariable String project, @RequestBody IntercomForm form) throws JSONException, IOException, URISyntaxException{
		
		ConversationPartCollection conversationParts = form.getConversation().getConversationParts();
		List<ConversationPart> page = conversationParts.getPage();
		for(ConversationPart part : page){
			Document htmlConversationSubject = Jsoup.parse(form.getConversation().getConversationMessage().getSubject());
			
			Document htmlNoteBody = Jsoup.parse(part.getBody());
			String noteBody = htmlNoteBody.getElementsByTag("p").text();

			String body = form.getConversation().getConversationMessage().getBody();
			String DescriptionOfBug = "Subject: "+(htmlConversationSubject != null ? htmlConversationSubject.text() : "No subject provided") + "<br>Content: " + body;
			
			if(noteBody.toLowerCase().contains("/report")){
				String[] reportSplit = noteBody.split("/report "); 
				UserDetails createdBy = UserDetails.findByContainStringInName(part.getAuthor().getName());
				
				VisualStudioWorkItem workItem = new VisualStudioWorkItemBuilder()
				.addTitle("Incident: "+((reportSplit.length > 1) ? reportSplit[1] : htmlConversationSubject.getElementsByTag("p").text()))
				.addStatus(VisualStudioBugState.NEW.value)
				.addIteration(VisualStudioUtil.getCurrentIteration(project, account))
				.addIntercomConversation(form.getConversationUrl())
				.addIsIncident(true)
				.addCreatedBy(createdBy != null ? createdBy.name : "Eugenio Valeiras")
				.addDescription(DescriptionOfBug) 
				.build();
				
				Integer bugId = VisualStudioUtil.createNewBug(workItem, project, account);
				
				IntercomUtil.makeANoteInConversation("Bug created "+VisualStudioUtil.getVisualWorkItemUrlEncoded(bugId.toString(), project, account), form.getConversation().getId());
			}
		}
		
		return "{\"status\" : 200}";
	}
	
	@RequestMapping(value = "/admins" ,method = RequestMethod.GET)
	@ResponseBody
	public AdminCollection getADmins() throws JSONException, IOException{
		return IntercomUtil.getAdmins();
	}
	
	@RequestMapping(value = "/conversation/{id}" ,method = RequestMethod.GET)
	@ResponseBody
	public String getConversationByID(@PathVariable("id") String id) throws JSONException, IOException{
		return "{\"status\" : 200, \"body\" : \""+IntercomUtil.findConversationByID(id).getConversationMessage().getBody()+"\"}";
	}
	
}
