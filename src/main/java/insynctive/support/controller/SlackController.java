package insynctive.support.controller;

import java.io.IOException;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.support.form.intercom.IntercomForm;
import insynctive.support.form.slack.SlackForm;
import insynctive.support.utils.VictorOpsUtil;
import insynctive.support.utils.slack.SlackUtil;
import insynctive.support.utils.victorops.VictorOpsIncident;
import insynctive.support.utils.victorops.builder.VictorOpsIncidentBuilder;

@Controller
@RequestMapping("/slack")
public class SlackController {

	@RequestMapping(value = "/createChannel" ,method = RequestMethod.POST)
	@ResponseBody
	public String createChannel(@RequestBody SlackForm form) throws JSONException, IOException, IllegalArgumentException, IllegalAccessException{
		
		SlackUtil.createNewChannel(form.getChannel());
		
		return "{\"status\" : 200}";
	}
	
	@RequestMapping(value = "/archiveChannel" ,method = RequestMethod.POST)
	@ResponseBody
	public String archiveChannel(@RequestBody SlackForm form) throws JSONException, IOException, IllegalArgumentException, IllegalAccessException{
		
		SlackUtil.createNewChannel(form.getChannel());
		
		return "{\"status\" : 200}";
	}
	
}
