package insynctive.support.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Inject;

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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import insynctive.support.dao.RunIDDao;
import insynctive.support.model.ListOfItems;
import insynctive.support.model.Status;
import insynctive.support.model.TargetProcessItem;

@Controller
@Scope("session")
public class AppController {
	
	private final String targetProcessURL = "https://insynctive.tpondemand.com";
	private final String tokenParamTargetProcess = "token=ODE6MjcyNUFBMzZBQkRGNkE0N0FGRDAyMzI2MDUyMTY1MzA=";
	
	private final RunIDDao runIDDao;
	
	@Inject
	public AppController(RunIDDao runIDDao) {
		this.runIDDao = runIDDao;
	}
	
	@RequestMapping(value = "/runID" ,method = RequestMethod.GET)
	@ResponseBody
	public String getRunID() throws JSONException, IOException, URISyntaxException{
		Integer newRunID = runIDDao.getNextRunID();
		return "{\"status\" : 200, \"runID\" : "+newRunID+"}";
	}
	
}
