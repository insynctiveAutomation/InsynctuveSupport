package insynctive.support.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.json.JSONException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.support.dao.RunIDDao;

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
