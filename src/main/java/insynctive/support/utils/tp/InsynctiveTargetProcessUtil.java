package insynctive.support.utils.tp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class InsynctiveTargetProcessUtil {
	
	public static final String targetProcessURL = "https://insynctive.tpondemand.com";
	public static final String tokenParamTargetProcess = "token=MTU6NTg0ODUxOTk3RDIzMzEyRDExRDEzREFGQTk5RTc4MzU=";
	
	public static String getAll(String type) throws IOException, URISyntaxException {
		URL url = new URL(targetProcessURL+"/api/v1/"+type+"?"+tokenParamTargetProcess+"&include=[ID]&format=json");
		System.out.println(url);
		
		return getUriContentsAsString(url.toString());
	}
	
	//TargetProcess
	public static String changeStates(TargetProcessItem item, Integer state) throws IOException, JSONException {
		String changeStateUrl = targetProcessURL+"/api/v1/Bugs?"+tokenParamTargetProcess+"&resultFormat=json&resultInclude=[Id]";
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

	public static String getUriContentsAsString(String uri) throws IOException {
		  HttpClient client = new DefaultHttpClient();
		  HttpResponse response = client.execute(new HttpGet(uri));
		  return EntityUtils.toString(response.getEntity());
	}
}
