package insynctive.support.controller;

import java.io.IOException;

import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.support.form.slack.SlackForm;
import insynctive.support.utils.UserDetails;
import insynctive.support.utils.slack.SlackMessageObject;
import insynctive.support.utils.slack.SlackUtilInsynctive;

@Controller
@RequestMapping("/slack")
public class SlackController {
	
	@RequestMapping(value = "/send" ,method = RequestMethod.POST)
	@ResponseBody
	public String sendMessge(@RequestBody SlackMessageObject message) throws IOException {
		
		String channel = message.getChannel();
		if(!channel.substring(0).equals("@") && !channel.substring(0).equals("#")){
			UserDetails findByContainStringInName = UserDetails.findByContainStringInName(channel);
			if(findByContainStringInName != null){
				message.setChannel(findByContainStringInName.slackMention);
			} else {
				return "{\"status\" : 200, \"message\" : \"channel not found\"}";
			}
		}
		
		SlackUtilInsynctive.sendMessage(message);
		
		return "{\"status\" : 200}";
	}

	@RequestMapping(value = "/createChannel" ,method = RequestMethod.POST)
	@ResponseBody
	public String createChannel(@RequestBody SlackForm form) throws JSONException, IOException, IllegalArgumentException, IllegalAccessException{
		
		SlackUtilInsynctive.createNewChannel(form.getChannel());
		
		return "{\"status\" : 200}";
	}
	
	@RequestMapping(value = "/archiveChannel" ,method = RequestMethod.POST)
	@ResponseBody
	public String archiveChannel(@RequestBody SlackForm form) throws JSONException, IOException, IllegalArgumentException, IllegalAccessException{
		
		SlackUtilInsynctive.createNewChannel(form.getChannel());
		
		return "{\"status\" : 200}";
	}
	
}
