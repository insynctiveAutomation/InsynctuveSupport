package insynctive.controller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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

import insynctive.model.TargetProcessItem;

@Controller
@Scope("session")
public class AppController {
	
	private final String targetProcessURL = "https://insynctive.tpondemand.com";
	
	@RequestMapping(value = "/view/to/{state}" ,method = RequestMethod.GET)
	public ModelAndView toState(@PathVariable("state") String state, @RequestBody List<TargetProcessItem> items) throws JSONException, IOException{
		String response = changeStates(items);
		ModelAndView model = new ModelAndView();
		model.setViewName("to_state");
		model.addObject("response",response);
		
		return model;
	}

	@RequestMapping(value = "/to/{state}" ,method = RequestMethod.GET)
	@ResponseBody
	public String restToState(@PathVariable("state") String state, @RequestBody List<TargetProcessItem> items) throws JSONException, IOException{
		String response = changeStates(items);
		ModelAndView model = new ModelAndView();
		model.setViewName("to_state");
		model.addObject(response);
		
		return "{\"status\" : 200}";
	}
	
	@SuppressWarnings("unchecked")
	public String changeStates(List<TargetProcessItem> items) throws IOException, JSONException {
		URL u = new URL(targetProcessURL+"/Targetprocess/api/v1/Projects?resultFormat=json&resultInclude=[Id,CustomFields]");
		
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		
		JSONArray jsonItems = new JSONArray();
		for(TargetProcessItem item : items){
			JSONObject jsonItem = new JSONObject();

			//ID of Item
			jsonItem.put("Id",item.getId());
			
			//Custom Fields to change.
			JSONObject customField = new JSONObject();
			customField.put("Name", "State");
			customField.put("Value", "Resolved");
			
			//Add Custom Field to Item
			jsonItem.put("CustomFields",customField);
			
			//Add Iem to Array
			jsonItems.add(jsonItem);
		}
		
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(5000);
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		
		
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(jsonItems.toString());
		wr.flush();
        wr.close();
        
		InputStream is = conn.getInputStream();
		System.out.println(is);
		return is.toString();
	}
}
