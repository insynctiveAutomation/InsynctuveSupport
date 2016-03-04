package insynctive.support.model;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "run_id")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RunID {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private BigInteger id;
	
	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}
	
}
