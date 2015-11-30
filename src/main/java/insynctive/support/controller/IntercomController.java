package insynctive.support.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.json.JSONException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.support.dao.RunIDDao;
import insynctive.support.dao.TargetProcessAndIntercomDao;
import insynctive.support.form.IntercomNote;
import insynctive.support.form.TargetProcessRequest;
import insynctive.support.model.TargetProcessIntercomEntity;
import insynctive.support.utils.IntercomUtil;
import io.intercom.api.Admin;
import io.intercom.api.AdminCollection;
import io.intercom.api.AdminReply;
import io.intercom.api.Author;
import io.intercom.api.Conversation;
import io.intercom.api.Intercom;
import io.intercom.api.Note;
import io.intercom.api.User;
import io.intercom.api.UserReply;

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
	public String newRequest(@RequestBody TargetProcessRequest form) throws JSONException, IOException{
		TargetProcessIntercomEntity entity = new TargetProcessIntercomEntity();
		
		entity.addValuesFromTPRequest(form);
		tpDao.save(entity);
		
		IntercomUtil.makeACommentInConversationIntercom("You request was submitted to QA for Triage under the number: "+form.getEntityID(), form.getIntercomID());
		IntercomUtil.makeANoteInIntercom("A request was created in TP.  https://insynctive.tpondemand.com/entity/"+form.getEntityID(), form.getIntercomID(), null);
		
		return "{\"status\" : 200}";
	}
	
	@RequestMapping(value = "/createNote" ,method = RequestMethod.POST)
	@ResponseBody
	public String createNote(@RequestBody IntercomNote form) throws JSONException, IOException{
		TargetProcessIntercomEntity entity = tpDao.getByEntityID(form.getEntityID());
		
		if(entity != null){
			IntercomUtil.makeANoteInIntercomWithAuthor(form.getBody(), entity.getIntercomID(), IntercomUtil.findAdminByEmail(form.getUserEmail()));
			return "{\"status\" : 200}";
		} else {
			return "{\"status\" : 404, \"cause\" : \"No entity with ID "+form.getEntityID()+"\"}";
		}
	}
	
	@RequestMapping(value = "/changeStatus" ,method = RequestMethod.POST)
	@ResponseBody
	public String changeStatus(@RequestBody TargetProcessRequest form) throws JSONException, IOException{
		TargetProcessIntercomEntity entity = tpDao.getByEntityID(form.getEntityID());
		
		boolean isEntityInDB = entity != null;
		boolean isStatusChange = isEntityInDB && !entity.getStatus().equals(form.getStatus());
		
		if(isEntityInDB && isStatusChange){
			String oldStatus = entity.getStatus();
			tpDao.updateStatus(entity, form.getStatus());

			IntercomUtil.makeACommentInConversationIntercom("Your request changed status to "+form.getStatus(), entity.getIntercomID());
			
			return "{\"status\" : 200}";
		} else if(!isEntityInDB){
			return "{\"status\" : 404, \"cause\" : \"No entity with ID "+form.getEntityID()+"\"}";
		} else if(!isStatusChange){
			return "{\"status\" : 404, \"cause\" : \"The entity not change the status\"}";
		} else {
			return "{\"status\" : 404}";
		}
	}
	
	@RequestMapping(value = "/admins" ,method = RequestMethod.GET)
	@ResponseBody
	public AdminCollection getADmins() throws JSONException, IOException{
		return IntercomUtil.getAdmins();
	}
	
	@RequestMapping(value = "/conversation/{id}" ,method = RequestMethod.GET)
	@ResponseBody
	public String getConversationByID(@PathVariable("id") String id) throws JSONException, IOException{
		return "{\"status\" : 200, \"body\" : \""+IntercomUtil.findIntercomConversationByID(id).getConversationMessage().getBody()+"\"}";
	}
}
