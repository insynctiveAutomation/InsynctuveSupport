package insynctive.support.utils.slack;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.web.util.UriUtils;

import insynctive.support.utils.UserDetails;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;

public class SlackUtil {

	private static final String token =  "xoxp-2598773363-6987940228-21463692417-b6025bd177";
	
	public static boolean sendMessage(SlackMessageObject message) throws IOException {
		
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
		
		String chatMessageUrl = 
				"https://slack.com/api/chat.postMessage";
		
		String chatMessageParameters = "?"
						+ "token=xoxp-2598773363-6987940228-21463692417-b6025bd177"
						+ "&channel=" + UriUtils.encodeQueryParam(message.getChannel(), "UTF-8")
						+ "&text=" + UriUtils.encodeQueryParam(message.getText(), "UTF-8")
						+ "&username=" + UriUtils.encodeQueryParam(message.getUsername(), "UTF-8")
						+ "&attachments=" + UriUtils.encodeQueryParam(attachments.toJSONString(), "UTF-8")
						+ "&icon_emoji="+UriUtils.encodeQueryParam(message.getIconEmoji(), "UTF-8")
						+ "&link_names=1"
						+ "&pretty=1";
		
		HttpGet httpGet = new HttpGet(chatMessageUrl+chatMessageParameters);
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpGet);
		
		if(response.getStatusLine().getStatusCode() != 200){
			System.out.println("URL: \n"+chatMessageUrl+chatMessageParameters);
			System.out.println("Data: \n"+"{}");
			
			System.out.println("Status: \n"+response.getStatusLine().getStatusCode());
			System.out.println("Response: \n"+response);
		}
		
		return response.getStatusLine().getStatusCode() == 200;
	}
	
	public static Boolean createNewChannel(String channelName) throws ClientProtocolException, IOException{
		String urlString = "https://slack.com/api/channels.create?token="+token+"&name="+channelName+"&pretty=1";
		String encodeUri = UriUtils.encodeQuery(urlString, "UTF-8");
		
		HttpPost httpPost = new HttpPost(encodeUri);
		httpPost.addHeader("Content-Type", "application/json");
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpPost);
		
		System.out.println("URL: \n"+encodeUri);
		System.out.println("Data: \n"+"{}");
		
		System.out.println("Status: \n"+response.getStatusLine().getStatusCode());
		System.out.println("Response: \n"+response);
		
		return response.getStatusLine().getStatusCode() == 200;
		
	}
	
	public static Boolean archiveChannel(String channelName) throws ClientProtocolException, IOException{
		String urlString = "https://slack.com/api/channels.archive?token="+token+"&name="+channelName;
		String encodeUri = UriUtils.encodeQuery(urlString, "UTF-8");
		
		HttpPost httpPost = new HttpPost(encodeUri);
		httpPost.addHeader("Content-Type", "application/json");
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpPost);
		
		System.out.println("URL: \n"+encodeUri);
		System.out.println("Data: \n"+"{}");
		
		System.out.println("Status: \n"+response.getStatusLine().getStatusCode());
		System.out.println("Response: \n"+response);
		
		return response.getStatusLine().getStatusCode() == 200;
		
	}
	
	public static String getSlackAccountMentionByEmail(String email) throws IOException{
		
		UserDetails userDetailFindByEmail = UserDetails.findByEmail(email);
		
		if(userDetailFindByEmail != null){
			return userDetailFindByEmail.slackMention;
		} 
		
		if(email != null && !email.equals("")){
			notifyIfNotExist(email);
		}
		return null;
	}
	
	public static String getSlackAccountMentionByName(String name) throws IOException{
		
		UserDetails userDetailFindByName = UserDetails.findByName(name);
		
		if(userDetailFindByName != null){
			return userDetailFindByName.slackMention;
		} 
		
		if(name != null && !name.equals("")){
			notifyIfNotExist(name);
		}
		return null;
	}

	private static void notifyIfNotExist(String email) throws IOException {
		if(email != null && !email.equals("")){
			SlackMessageObject message = new SlackMessageBuilder()
					.setText(UserDetails.EUGENIO_VALEIRAS.slackMention+" please set "+email+" to match the right user in Slack")
					.setChannel(":heavy_plus_sign:")
					.setUsername("Bot Notify")
					.setChannel("@eugeniovaleiras")
					.build();
			SlackUtil.sendMessage(message);
		}
	}
}
