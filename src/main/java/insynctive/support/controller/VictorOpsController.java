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
import insynctive.support.utils.VictorOpsUtil;
import insynctive.support.utils.victorops.VictorOpsIncident;
import insynctive.support.utils.victorops.builder.VictorOpsIncidentBuilder;

@Controller
@RequestMapping("/victorOps")
public class VictorOpsController {

	@RequestMapping(value = "/createFromIntercom" ,method = RequestMethod.POST)
	@ResponseBody
	public String newRequest(@RequestBody IntercomForm form) throws JSONException, IOException, IllegalArgumentException, IllegalAccessException{
		
		Document html = Jsoup.parse(form.getConversation().getConversationMessage().getSubject());
		
		VictorOpsIncident incident = new VictorOpsIncidentBuilder()
				.setEntityDisplayName(String.format("New Incident reported in Intercom - %s", "<"+form.getConversationUrl()+"|"+form.getItemID()+">"))
				.setMessage(html.body().text())
				.setMessageTypeCritical()
				.setMonitoringToolIntercom()
				.setTimestmp()
				.build();
		
		VictorOpsUtil.createIncident(incident);
		
		return "{\"status\" : 200}";
	}
}
