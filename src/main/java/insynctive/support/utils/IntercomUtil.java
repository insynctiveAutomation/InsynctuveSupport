package insynctive.support.utils;

import java.util.HashMap;
import java.util.Map;

import io.intercom.api.Admin;
import io.intercom.api.AdminCollection;
import io.intercom.api.AdminReply;
import io.intercom.api.Conversation;
import io.intercom.api.Intercom;
import io.intercom.api.User;

public class IntercomUtil {

	private static final String supportID = "177656";
	private static final String intercomAppID = "h9ti7xcp";
	private static final String intercomApiKey = "3bc8856e5ad21f83cfbd372ffa5b182388290d09";
	
	static {
		Intercom.setApiKey(intercomApiKey);
		Intercom.setAppID(intercomAppID);
	}

	public static void makeANoteInIntercom(String body, String intercomID) {
		makeANoteInConversation(body, intercomID, null);
	}
	
	public static void makeANoteWithAuthor(String body, String intercomID, Admin admin) {
		makeANoteInConversation(body, intercomID, admin != null ? admin.getId() : null);
	}
	
	public static void makeANoteInConversation(String body, String intercomID) {
		makeANoteInConversation(body, intercomID, null);
	}
	
	public static void makeANoteInConversation(String body, String intercomID, String userID) {
		Admin admin = new Admin().setId(userID != null ? userID : supportID);
		AdminReply adminReply = new AdminReply(admin);
		adminReply.setMessageType("note");
		adminReply.setBody(body);

		Conversation.reply(intercomID, adminReply);
	}
	
	public static void makeACommentInConversation(String body, String intercomID) {
		Admin admin = new Admin().setId(supportID);
		AdminReply adminReply = new AdminReply(admin);
		adminReply.setBody(body);

		Conversation.reply(intercomID, adminReply);
	}
	
	public static AdminCollection getAdmins() {
		AdminCollection admins = Admin.list();
		
		return admins;
	}
	
	public static Conversation findConversationByID(String id){
		final Conversation conversation = Conversation.find(id);
		
		return conversation;
	}
	
	public static User findUserByEmail(String email){
		Map<String, String> params = new HashMap<String, String>();
		params.put("email", email);
		return User.find(params);
	}
	
	public static Admin findAdminByEmail(String email){
		AdminCollection admins = Admin.list();
		while(admins.hasNext()){
			Admin admin = admins.next();
			if(admin.getEmail() != null && admin.getEmail().equals(email)){
					return admin;
				}
			}
		return null;
	}
}
