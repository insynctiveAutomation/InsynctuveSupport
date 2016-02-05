package insynctive.support.utils.vs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.simple.JSONArray;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import insynctive.support.form.vs.VisualStudioForm;
import insynctive.support.form.vs.VisualStudioRevisionForm;

public class VisualStudioUtil {

	private static final String DEFAULT_PROJECT = "Insynctive";
	private static final String ACCOUNT = "insynctive";
	private static final String username = "eugeniovaleiras";
	private static final String password = "Benefits123";
	private static final String token = "ah5w4kyrjsps4qjcoi5pyteahdq7jec5vzb3jewz5q5jz3eypvra";
	private static final String encoding = Base64.encodeBase64String((username+":"+password).getBytes());
	private static final ObjectMapper mapper = new ObjectMapper();
	
	
	public static void createNewTask(VisualStudioWorkItem workItem) throws IOException{
		createNewTask(workItem, null);
	}
	
	public static VisualStudioRevisionForm getWorkItem(String id) throws Exception{

		HttpGet httpGet = new HttpGet(getWorkItemUrl(id));
		httpGet.addHeader("Authorization", "Basic " + encoding);
		
		//Run Patch
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpGet);
		String json = EntityUtils.toString(response.getEntity());
		
		System.out.println(json);
		VisualStudioRevisionForm ObjectResponse = mapper.readValue(json, VisualStudioRevisionForm.class); 
		return ObjectResponse;
	}
	
	public static void createNewTask(VisualStudioWorkItem workItem, String project) throws IOException{
		String urlString = getCreateTaskUrl(project, "Task");

		JSONArray fields = new JSONArray();
		for(VisualStudioField field : workItem.fields){
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("op", field.getOp());
			jsonObj.put("path", field.getPath());
			jsonObj.put("value", field.getValue());
			fields.add(jsonObj);
		}
		for(VisualStudioRelation relation : workItem.relations){
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("op", relation.getOp());
			jsonObj.put("path", relation.getPath());
			jsonObj.put("value", relation.getValue().asJson());
			fields.add(jsonObj);
		}
		
		//Create Data JSON
		StringEntity entity = new StringEntity(fields.toJSONString(), "UTF-8");
		entity.setContentType("application/json");
		
		//Create Patch
		HttpPatch httpPatch = new HttpPatch(urlString);
		httpPatch.addHeader("Content-Type", "application/json-patch+json");
		httpPatch.addHeader("Authorization", "Basic " + encoding);
		httpPatch.setEntity(entity);
		
		//Run Patch
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpPatch);
		
		System.out.println("Status: \n"+response.getStatusLine().getStatusCode());
		System.out.println("Response: \n"+response);
	}
	
	public static String getCreateTaskUrl(String project, String type){
		return "https://"+ACCOUNT+".visualstudio.com/DefaultCollection/"+(project != null ? project : DEFAULT_PROJECT)+"/_apis/wit/workitems/$"+type+"?api-version=1.0";
	}
	
	public static String getWorkItemUrl(String id){
		return "https://"+ACCOUNT+".visualstudio.com/DefaultCollection/_apis/wit/workitems/"+id+"?api-version=1.0&$expand=relations";
	}
}
