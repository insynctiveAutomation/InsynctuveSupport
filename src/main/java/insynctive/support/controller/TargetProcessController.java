package insynctive.support.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import insynctive.support.dao.RunIDDao;
import insynctive.support.form.tp.ListOfItems;
import insynctive.support.utils.tp.TPStatus;
import insynctive.support.utils.tp.TargetProcessItem;
import insynctive.support.utils.tp.InsynctiveTargetProcessUtil;

@Controller
@RequestMapping(value = "/tp")
public class TargetProcessController {
		
	private final RunIDDao runIDDao;
	
	@Inject
	public TargetProcessController(RunIDDao runIDDao) {
		this.runIDDao = runIDDao;
	}

	@RequestMapping(value = "/to/{state}" ,method = RequestMethod.POST)
	@ResponseBody
	public String restToState(@PathVariable("state") String state, @RequestBody ListOfItems items) throws JSONException, IOException{
		for(TargetProcessItem item : items.getItems()){
			InsynctiveTargetProcessUtil.changeStates(item, TPStatus.getID(state));
		}
		return "{\"status\" : 200}";
	}
	
	@RequestMapping(value = "/getAll/{type}" ,method = RequestMethod.GET)
	@ResponseBody
	public String restToState(@PathVariable("type") String type) throws JSONException, IOException, URISyntaxException{
		return InsynctiveTargetProcessUtil.getAll(type);
	}
}
