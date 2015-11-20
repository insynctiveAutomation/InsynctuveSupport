package insynctive.controller;

import java.io.IOException;

import javax.inject.Inject;

import org.json.JSONException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.dao.RunIDDao;
import insynctive.dao.TargetProcessAndIntercomDao;
import insynctive.form.IntercomNote;
import insynctive.form.TargetProcessRequest;
import insynctive.model.TargetProcessIntercomEntity;
import io.intercom.api.Admin;
import io.intercom.api.AdminCollection;
import io.intercom.api.AdminReply;
import io.intercom.api.Conversation;
import io.intercom.api.Intercom;

@Controller
@Scope("session")
public class IntercomController {

	private final TargetProcessAndIntercomDao tpDao;
	private final String supportID = "177656";
	
	@Inject
	public IntercomController(TargetProcessAndIntercomDao tpDao) {
		this.tpDao = tpDao;
	}
	
	@RequestMapping(value = "/intercom/newRequest" ,method = RequestMethod.POST)
	@ResponseBody
	public String newRequest(@RequestBody TargetProcessRequest form) throws JSONException, IOException{
		TargetProcessIntercomEntity entity = new TargetProcessIntercomEntity();
		
		entity.addValuesFromTPRequest(form);
		tpDao.save(entity);
		
		return "{\"status\" : 200}";
	}
	
	@RequestMapping(value = "/intercom/createNote" ,method = RequestMethod.POST)
	@ResponseBody
	public String createNote(@RequestBody IntercomNote form) throws JSONException, IOException{
		TargetProcessIntercomEntity entity = tpDao.getByEntityID(form.getEntityID());
		
		if(entity != null){
			makeANoteInIntercom(form.getBody(), entity.getIntercomID());
			return "{\"status\" : 200}";
		} else {
			return "{\"status\" : 404, \"cause\" : \"No entity with ID "+form.getEntityID()+"\"}";
		}
	}

	@RequestMapping(value = "/intercom/changeStatus" ,method = RequestMethod.POST)
	@ResponseBody
	public String changeStatus(@RequestBody TargetProcessRequest form) throws JSONException, IOException{
		TargetProcessIntercomEntity entity = tpDao.getByEntityID(form.getEntityID());
		
		boolean isEntityInDB = entity != null;
		boolean isStatusChange = isEntityInDB && !entity.getStatus().equals(form.getStatus());
		
		if(isEntityInDB && isStatusChange){
			String oldStatus = entity.getStatus();
			tpDao.updateStatus(entity, form.getStatus());
			
			makeANoteInIntercom("The state changed from: "+oldStatus+" to "+form.getStatus(), entity.getIntercomID());
			
			return "{\"status\" : 200}";
		} else if(!isEntityInDB){
			return "{\"status\" : 404, \"cause\" : \"No entity with ID "+form.getEntityID()+"\"}";
		} else if(!isStatusChange){
			return "{\"status\" : 404, \"cause\" : \"The entity not change the status\"}";
		} else {
			return "{\"status\" : 404}";
		}
	}
	
	@RequestMapping(value = "/intercom/admins" ,method = RequestMethod.GET)
	@ResponseBody
	public AdminCollection getADmins() throws JSONException, IOException{
		return getAdmins();
	}
	
	@RequestMapping(value = "/intercom/conversation/{id}" ,method = RequestMethod.GET)
	@ResponseBody
	public String getConversationByID(@PathVariable("id") String id) throws JSONException, IOException{
		return "{\"status\" : 200, \"body\" : \""+findIntercomConversationByID(id).getConversationMessage().getBody()+"\"}";
	}
	
	//INTERCOM
	private void makeANoteInIntercom(String body, String intercomID) {
		Intercom.setApiKey("3bc8856e5ad21f83cfbd372ffa5b182388290d09");
		Intercom.setAppID("h9ti7xcp");
		
		Admin admin = new Admin().setId(supportID);
		AdminReply adminReply = new AdminReply(admin);
		adminReply.setBody(body);

		Conversation.reply(intercomID, adminReply);
	}
	
	private AdminCollection getAdmins() {
		Intercom.setApiKey("3bc8856e5ad21f83cfbd372ffa5b182388290d09");
		Intercom.setAppID("h9ti7xcp");
		
		AdminCollection admins = Admin.list();
		
		return admins;
	}
	
	private Conversation findIntercomConversationByID(String id){
		Intercom.setApiKey("3bc8856e5ad21f83cfbd372ffa5b182388290d09");
		Intercom.setAppID("h9ti7xcp");
		final Conversation conversation = Conversation.find(id);
		return conversation;
	}
	
}
