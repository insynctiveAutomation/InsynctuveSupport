package insynctive.support.utils.victorops;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VictorOpsIncident {

	/**
	 * message_type (required)
	 * 				[String] One of the following values: INFO, WARNING, ACKNOWLEDGEMENT, CRITICAL, RECOVERY
					CRITICAL messages raise incidents in VictorOps, you can also configure your settings to raise incidents for WARNING messages.
					INFO messages become part of your timeline but do not raise incidents.
					
	 * entity_id
	 				[String] The name of alerting entity. If not provided, a random name will be assigned.
					VictorOps uses the entity_id field to identify the monitored entity (host, service, metric, etc.) for rollup into incidents. If you don't provide an ID in your request, we'll assign one and return it as part of the response.
					By using the same entity_id in multiple notifications, the incidents created can go through the normal incident lifecycle, from Triggered, through Acknowledged, to Resolved.
					An example entity_id could be "<service>/<HostName>", i.e... "diskspace/db01.mycompany.com"
					
	 * timestamp
 					[Number] Timestamp of the alert in seconds since epoch. Defaults to the time the alert is received at VictorOps.
	 					
	 * state_start_time
 					[Number] The time this entity entered its current state (seconds since epoch). Defaults to the time alert is received.
	 					
	 * state_message
 					[String] Any additional status information from the alert item.
	 					
	 * monitoring_tool
  					[String] The name of the monitoring system software (eg. nagios, icinga, sensu, etc.)
	 
	 * entity_display_name
 					[String] Used within VictorOps to display a human-readable name for the entity.
	 					
	 * ack_msg
 					[String] A user entered comment for the acknowledgment.
	 					
	 * ack_author
 					[String] The user that acknowledged the incident.
	 
	 * */
	
	@JsonProperty("message_type")
	private String messageType;
	
	@JsonProperty("entity_id")
	private String entityID;
	
	@JsonProperty("timestamp")
	private String timestamp;
	
	@JsonProperty("state_start_time")
	private String stateStartTime;
	
	@JsonProperty("state_message")
	private String stateMessage;
	
	@JsonProperty("monitoring_tool")
	private String monitoringTool;
	
	@JsonProperty("entity_display_name")
	private String entityDisplayName;
	
	@JsonProperty("ack_msg")
	private String aclMsg;

	@JsonProperty("ack_author")
	private String ackAuthor;
	
	public VictorOpsIncident() {
		// TODO Auto-generated constructor stub
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	@JsonIgnore
	public void setMessageType(VIctorOpsMessageType messageType) {
		this.messageType = messageType.getValue();
	}

	public String getEntityID() {
		return entityID;
	}

	public void setEntityID(String entityID) {
		this.entityID = entityID;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getStateStartTime() {
		return stateStartTime;
	}

	public void setStateStartTime(String stateStartTime) {
		this.stateStartTime = stateStartTime;
	}

	public String getStateMessage() {
		return stateMessage;
	}

	public void setStateMessage(String stateMessage) {
		this.stateMessage = stateMessage;
	}

	public String getMonitoringTool() {
		return monitoringTool;
	}

	public void setMonitoringTool(String monitoringTool) {
		this.monitoringTool = monitoringTool;
	}

	public String getEntityDisplayName() {
		return entityDisplayName;
	}

	public void setEntityDisplayName(String entityDisplayName) {
		this.entityDisplayName = entityDisplayName;
	}

	public String getAclMsg() {
		return aclMsg;
	}

	public void setAclMsg(String aclMsg) {
		this.aclMsg = aclMsg;
	}

	public String getAckAuthor() {
		return ackAuthor;
	}

	public void setAckAuthor(String ackAuthor) {
		this.ackAuthor = ackAuthor;
	}

	public Map<String, String> asMap() throws IllegalArgumentException, IllegalAccessException {
		Map<String, String> map = new HashMap<>();
		
		Field[] fields = VictorOpsIncident.class.getDeclaredFields();
		for (Field field : fields){
			field.setAccessible(true);
			if(field.get(this) != null){
				map.put(field.getAnnotation(JsonProperty.class).value(), field.get(this).toString());
			}
		}
		
		return map;
	}
}
