package insynctive.support.utils.slack;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackUserPresence {

	private String presence;
	private Boolean online;
	private Boolean ok;

	@JsonProperty("auto_away")
	private Boolean autoAway;
	
	@JsonProperty("manual_away")
	private Boolean manualAway;
	
	@JsonProperty("connection_count")
	private Integer connectionCount;
	
	@JsonProperty("last_activity")
	private BigInteger lastActivity;
	
	
	public SlackUserPresence() { }
	
	public SlackUserPresence(String presence, Boolean ok) {
		super();
		this.presence = presence;
		this.ok = ok;
	}
	
	public String getPresence() {
		return presence;
	}
	public void setPresence(String presence) {
		this.presence = presence;
	}
	
	public Boolean isOk() {
		return ok;
	}
	public void setOk(Boolean ok) {
		this.ok = ok;
	}

	public Boolean isOnline() {
		return online;
	}

	public void setOnline(Boolean online) {
		this.online = online;
	}

	public Boolean isAutoAway() {
		return autoAway;
	}

	public void setAutoAway(Boolean autoAway) {
		this.autoAway = autoAway;
	}

	public Boolean isManualAway() {
		return manualAway;
	}

	public void setManualAway(Boolean manualAway) {
		this.manualAway = manualAway;
	}

	public Integer getConnectionCount() {
		return connectionCount;
	}

	public void setConnectionCount(Integer connectionCount) {
		this.connectionCount = connectionCount;
	}

	public BigInteger getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(BigInteger lastActivity) {
		this.lastActivity = lastActivity;
	}
	
}
