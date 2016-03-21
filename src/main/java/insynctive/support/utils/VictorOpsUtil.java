package insynctive.support.utils;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;

import support.utils.victorops.VictorOpsIncident;

public class VictorOpsUtil {

	private static String urlString = "https://alert.victorops.com/integrations/generic/20131114/alert/cfb06721-241f-4df5-8f98-65762681b85d/InsynctiveVO";
	
	public static Boolean createIncident(VictorOpsIncident incident) throws IllegalArgumentException, IllegalAccessException, ClientProtocolException, IOException{
		//Create Data JSON
		String incidentAsJsonString = JSONObject.toJSONString(incident.asMap());
		StringEntity entity = new StringEntity(incidentAsJsonString, "UTF-8");
		
		//Create Patch
		HttpPost httpPost = new HttpPost(urlString);
		httpPost.setEntity(entity);
		
		//Run Patch
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpResponse response = httpClient.execute(httpPost);
		
		System.out.println("URL: \n" + urlString);
		System.out.println("Data: \n" + incidentAsJsonString);
		
		System.out.println("Status: \n" + response.getStatusLine().getStatusCode());
		System.out.println("Response: \n" + response);
		
		return response.getStatusLine().getStatusCode() == 200;
		
	}
		
}
