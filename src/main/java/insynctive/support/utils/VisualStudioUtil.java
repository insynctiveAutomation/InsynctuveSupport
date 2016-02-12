package insynctive.support.utils;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import insynctive.support.form.vs.VisualStudioRevisionForm;
import insynctive.support.utils.vs.VisualStudioField;
import insynctive.support.utils.vs.VisualStudioRelation;
import insynctive.support.utils.vs.VisualStudioWorkItem;

public class VisualStudioUtil {

	private static final String username = " evaleiras@insynctive.com";
	private static final String password = "Benefits123";
	
	private static final String encoding = Base64.encodeBase64String((username+":"+password).getBytes());
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static VisualStudioRevisionForm getWorkItem(String id, String account) throws Exception{

		HttpGet httpGet = new HttpGet(getWorkItemUrl(id, account));
		httpGet.addHeader("Authorization", "Basic " + encoding);
		
		//Run Patch
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpGet);
		String json = EntityUtils.toString(response.getEntity());
		
		System.out.println(json);
		VisualStudioRevisionForm ObjectResponse = mapper.readValue(json, VisualStudioRevisionForm.class); 
		return ObjectResponse;
	}
	
	public static Boolean createNewTask(VisualStudioWorkItem workItem, String project, String account) throws IOException, URISyntaxException{
		String urlString = getCreateWorkItemUrl(project, "Task", account);

		JSONArray fields = new JSONArray();
		for(VisualStudioField field : workItem.getFields()){
			fields.add(field.asJson());
		}
		for(VisualStudioRelation relation : workItem.getRelations()){
			fields.add(relation.asJson());
		}
		
		return sendPatch(urlString, fields);
	}

	public static Boolean updateWorkItem(VisualStudioWorkItem workItem, String id, String project, String account) throws IOException, URISyntaxException{
		String urlString = getModifiedWorkItemUrl(project, id, account);

		JSONArray fields = new JSONArray();
		for(VisualStudioField field : workItem.getFields()){
			fields.add(field.asJson());
		}
		for(VisualStudioRelation relation : workItem.getRelations()){
			fields.add(relation.asJson());	
		} 
		
		return sendPatch(urlString, fields);
	}

	private static Boolean sendPatch(String urlString, JSONArray fields) throws IOException, ClientProtocolException, URISyntaxException {
		//Create Data JSON
		StringEntity entity = new StringEntity(fields.toJSONString(), "UTF-8");
		entity.setContentType("application/json");
		
		//Create Patch
		String encode = UriUtils.encodeQuery(urlString, "UTF-8");
		HttpPatch httpPatch = new HttpPatch(encode);
		httpPatch.addHeader("Content-Type", "application/json-patch+json");
		httpPatch.addHeader("Authorization", "Basic " + encoding);
		httpPatch.setEntity(entity);
		
		//Run Patch
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpPatch);
		
		System.out.println("URL: \n"+encode);
		System.out.println("Data: \n"+fields.toJSONString());
		
		System.out.println("Status: \n"+response.getStatusLine().getStatusCode());
		System.out.println("Response: \n"+response);
		
		return response.getStatusLine().getStatusCode() == 200;
	}
	
	private static String getModifiedWorkItemUrl(String project, String id, String account){
		return "https://"+account+".visualstudio.com/DefaultCollection/_apis/wit/workitems/"+id+"?api-version=1.0";
	}
	
	private static String getCreateWorkItemUrl(String project, String type, String account){
		return "https://"+account+".visualstudio.com/DefaultCollection/"+(project)+"/_apis/wit/workitems/$"+type+"?api-version=1.0";
	}
	
	private static String getWorkItemUrl(String id, String account){
		return "https://"+account+".visualstudio.com/DefaultCollection/_apis/wit/workitems/"+id+"?api-version=1.0&$expand=relations";
	}
	
	public static String getVisualWorkItemUrl(String id, String project, String account) {
		// Engineering%20in%20Productivity%20Team/_workitems?_a=edit&id=189
		return "https://"+(account)+".visualstudio.com/DefaultCollection/"+(project)+"/_workitems?_a=edit&id="+id;
	}
}
