package insynctive.support.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.json.JSONException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.support.dao.RunIDDao;
import insynctive.support.utils.Property;

@Controller
@Scope("session")
public class AppController {
	
	private final RunIDDao runIDDao;
	private final Property property;
	
	@Inject
	public AppController(RunIDDao runIDDao, Property property) {
		this.property = property;
		this.runIDDao = runIDDao;
	}
	
	@RequestMapping(value = "/runID" ,method = RequestMethod.GET)
	@ResponseBody
	public String getRunID() throws JSONException, IOException, URISyntaxException{
		BigInteger newRunID = runIDDao.getNextRunID();
		return "{\"status\" : 200, \"runID\" : "+newRunID+"}";
	}
	
	@RequestMapping(value = "/property" ,method = RequestMethod.GET)
	@ResponseBody
	public String getPropertyNumber() throws Exception{
		return "{\"status\" : 200, \"Environment\" : "+property.findEnvironment()+", \"Number\" : "+property.getEnvironmentNumber()+"}";
	}
	
}
