package insynctive.controller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import insynctive.dao.RunIDDao;
import insynctive.model.ListOfItems;
import insynctive.model.TargetProcessItem;

@Controller
@Scope("session")
public class AppController {
	
	private final String targetProcessURL = "https://insynctive.tpondemand.com";
	private final String tokenParam = "token=ODE6MjcyNUFBMzZBQkRGNkE0N0FGRDAyMzI2MDUyMTY1MzA=";
	
	private final RunIDDao runIDDao;
	
	@Inject
	public AppController(RunIDDao runIDDao) {
		this.runIDDao = runIDDao;
	}
	
	@RequestMapping(value = "/view/to/{state}" ,method = RequestMethod.POST)
	public ModelAndView toState(@PathVariable("state") String state, @RequestBody ListOfItems items) throws JSONException, IOException{
		String response = "";
		for(TargetProcessItem item : items.getItems()){
			response += changeStates(item, Status.getID(state))+"\n\n\n";
		}
		ModelAndView model = new ModelAndView();
		model.setViewName("to_state");
		model.addObject("response", response);
		
		return model;
	}

	@RequestMapping(value = "/to/{state}" ,method = RequestMethod.POST)
	@ResponseBody
	public String restToState(@PathVariable("state") String state, @RequestBody ListOfItems items) throws JSONException, IOException{
		for(TargetProcessItem item : items.getItems()){
			changeStates(item, Status.getID(state));
		}
		return "{\"status\" : 200}";
	}
	
	@RequestMapping(value = "/get/{type}" ,method = RequestMethod.GET)
	@ResponseBody
	public String restToState(@PathVariable("type") String type) throws JSONException, IOException, URISyntaxException{
		return getAll(type);
	}
	
	@RequestMapping(value = "/runID" ,method = RequestMethod.GET)
	@ResponseBody
	public String getRunID() throws JSONException, IOException, URISyntaxException{
		Integer newRunID = runIDDao.getNextRunID();
		return "{\"status\" : 200, \"runID\" : "+newRunID+"}";
		
	}
	
	
	private String getAll(String type) throws IOException, URISyntaxException {
		URL changeStateUrl = new URL(targetProcessURL+"/api/v1/"+type+"?"+tokenParam+"&include=[ID]&format=json");
		System.out.println(changeStateUrl);
		
		return getUriContentsAsString(changeStateUrl.toString());
	}

	public String changeStates(TargetProcessItem item, Integer state) throws IOException, JSONException {
		String changeStateUrl = targetProcessURL+"/api/v1/Bugs?"+tokenParam+"&resultFormat=json&resultInclude=[Id]";
		System.out.println(changeStateUrl);
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(changeStateUrl);

		JSONObject jsonItem = new JSONObject();
		
		//ID of Item
		jsonItem.put("Id",item.getId());

		//EBRURT STATE 55 = Resolved 
		JSONObject jsonEntityState = new JSONObject();
		jsonEntityState.put("Id", state);
		jsonItem.put("EntityState",jsonEntityState);

		StringEntity params = new StringEntity(jsonItem.toString());
		post.addHeader("content-type", "application/json;charset=UTF-8");
		post.setEntity(params);
	    
        HttpResponse response = client.execute(post);
		return EntityUtils.toString(response.getEntity());
	}
	
	static String getUriContentsAsString(String uri) throws IOException {
		  HttpClient client = new DefaultHttpClient();
		  HttpResponse response = client.execute(new HttpGet(uri));
		  return EntityUtils.toString(response.getEntity());
	}
	
	public enum Status {
	    OPEN(53, "Open"),
	    CANT_NOT_REPRODUCE(220, "Can Not Reproduce"),
	    NEED_DISCUSSION(223, "Need Discussion"),
	    FIXING_IN_PROGRESS(98, "Fixing In Progress"),
	    RESOLVED(55, "Resolved"),
	    TESTING_IN_PROGRESS(99, "Testing In Progress"),
	    RE_OPEN(82, "Re-Open"),
	    TRACKED_BY_QA(134, "Tracked by QA"),
	    READY_FOR_DEPLOYMENT(127, "Ready for Deployment"),
	    OBSOLETE(221, "Obsolete"),
	    DONE(56, "Done");
		
		private final Integer id;
		private final String value;
		
		private Status(Integer id, String value){
			this.id = id;
			this.value = value;
		}
		
		public String getValue(){
			return this.value;
		}
		
		public static Integer getID(String value){
			for(Status status : Status.values()){
				if(status.getValue().equals(value)){
					return status.id;
				}
			}
			return null;
		}
	    
	}
}
