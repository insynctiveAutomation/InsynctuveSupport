package insynctive.support.utils.slack;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.json.simple.JSONArray;

import insynctive.support.utils.slack.builder.SlackMessageBuilder;

public class SlackUtil {

	public static void sendMessage(SlackMessage message) throws IOException{
		URL u = new URL(message.getUrl());
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		JSONObject payload = new JSONObject();
		payload.put("icon_emoji", message.getIconEmoji());
		payload.put("username", message.getUsername());
		payload.put("text", message.getText());
		payload.put("channel", message.getChannel());
		payload.put("link_names", true);
		
		JSONArray attachments = new JSONArray();
		for(SlackAttachment attach : message.getAttachments()){
			JSONObject attachment = new JSONObject(); 
			attachment.put("fallback", attach.getFallback());
			attachment.put("pretext", attach.getPretext());
			attachment.put("color", attach.getColor()); //AA3939
			JSONArray fields = new JSONArray();
			for(SlackField attcField : attach.getFields()){
				JSONObject field = new JSONObject();
				field.put("title", attcField.getTitle());
				field.put("value", attcField.getValue());
				field.put("short", false);
				fields.add(field);
			}
			
			attachment.put("fields", fields);
			attachments.add(attachment);
		}
		
		payload.put("attachments", attachments);
		
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(5000);
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(payload.toString());
		wr.flush();
        wr.close();
        
		InputStream is = conn.getInputStream();
		System.out.println(is);
	}
	
	public static String getSlackAccountMentionByEmail(String email) throws IOException{
		
		switch (email) {
			case "evaleiras@insynctive.com":
				return "@eugeniovaleiras";
			
			case "rgonzalez@insynctive.com":
				return "@rgonzalez";
				
			case "bpetrovski@insynctive.com":
				return "@bpetrovski";
			
			case "mjaneva@insynctive.com":
				return "@mjaneva";
		
			case "atodorovski@insynctive.com":
				return "@atanast";
		
			case "cpettibone@insynctive.com":
				return "@cpettibone";
		
			case "cbota@insynctive.com":
				return "@cristibota";
		
			case "mdjonov@insynctive.com":
				return "@djonov";
		
			case "dtravieso@insynctive.com":
				return "@dtravieso";
		
			case "ekish@insynctive.com":
				return "@erickish";
			
			case "ignaciof6@gmail.com":
				return "@ignacio";
			
			case "lsulev@insynctive.com":
				return "@ljupco.sulev";
		
			case "roland.andrei@gmail.com":
				return "@roland";
		
			case "slytvyn@insynctive.com":
				return "@serhii";
		
			case "svaz@insynctive.com":
				return "@simon";
		
			case "tstojkovski@insynctive.com":
				return "@tstojkovski";
		
			case "vlatko_n3@hotmail.com":
				return "@vlatko_n3";
			
			case "vojches@gmail.com":
				return "@vojche";
		}
		
		notifyIfNotExist(email);
		return null;
	}

	private static void notifyIfNotExist(String email) throws IOException {
		if(email != null && !email.equals("")){
			SlackMessage message = new SlackMessageBuilder()
					.setText("@eugeniovaleiras please set "+email+" to match the right user in Slack")
					.setChannel(":heavy_plus_sign:")
					.setUsername("Bot Notify")
					.setChannel("@eugeniovaleiras")
					.build();
			SlackUtil.sendMessage(message);
		}
	}
}
