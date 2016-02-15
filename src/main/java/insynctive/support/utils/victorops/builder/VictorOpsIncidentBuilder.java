package insynctive.support.utils.victorops.builder;

import java.util.Date;

import insynctive.support.utils.victorops.VIctorOpsMessageType;
import insynctive.support.utils.victorops.VictorOpsIncident;

public class VictorOpsIncidentBuilder {

	private VictorOpsIncident item;
	
	public VictorOpsIncidentBuilder() {
		this.item = new VictorOpsIncident();
	}
	
	
	public VictorOpsIncidentBuilder setMessageType(VIctorOpsMessageType type){
		item.setMessageType(type.getValue());
		return this;
	}
	
	public VictorOpsIncidentBuilder setMessageTypeInfo(){
		item.setMessageType(VIctorOpsMessageType.INFO);
		return this;
	}
	
	public VictorOpsIncidentBuilder setMessageTypeWarning(){
		item.setMessageType(VIctorOpsMessageType.WARNING);
		return this;
	}
	
	public VictorOpsIncidentBuilder setMessageTypeAcknowledgement(){
		item.setMessageType(VIctorOpsMessageType.ACKNOWLEDGEMENT);
		return this;
	}
	
	public VictorOpsIncidentBuilder setMessageTypeCritical(){
		item.setMessageType(VIctorOpsMessageType.CRITICAL);
		return this;
	}
	
	public VictorOpsIncidentBuilder setMessageTypeRecovery(){
		item.setMessageType(VIctorOpsMessageType.RECOVERY);
		return this;
	}
	
	public VictorOpsIncidentBuilder setEntityID(String entityID){
		item.setEntityID(entityID);
		return this;
	}
	
	public VictorOpsIncidentBuilder setTimestmp(Date date){
		item.setTimestamp(date.toString());
		return this;
	}
	
	public VictorOpsIncidentBuilder setTimestmp(){
		item.setTimestamp(new Date().toString());
		return this;
	}
	
	public VictorOpsIncidentBuilder setMessage(String message){
		item.setStateMessage(message);
		return this; 
	}
	
	public VictorOpsIncidentBuilder setMonitoringTool(String tool){
		item.setMonitoringTool(tool);
		return this;
	}
	
	public VictorOpsIncidentBuilder setMonitoringToolIntercom(){
		item.setMonitoringTool("Intercom");
		return this;
	}
	
	public VictorOpsIncidentBuilder setEntityDisplayName(String name){
		item.setEntityDisplayName(name);
		return this;
	}
	
	public VictorOpsIncident build(){
		return item;
	}
	
}
