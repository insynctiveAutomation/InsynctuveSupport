package insynctive.support.model;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "performance")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformanceConfiguration {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;
	
	private Boolean scheduleEnabled;
	
	public PerformanceConfiguration() {
		this.scheduleEnabled = true;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getScheduleEnabled() {
		return scheduleEnabled;
	}

	public void setScheduleEnabled(Boolean scheduleEnabled) {
		this.scheduleEnabled = scheduleEnabled;
	}
}
