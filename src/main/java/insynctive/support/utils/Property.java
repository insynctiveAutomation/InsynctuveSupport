package insynctive.support.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Property {

	@Value("${environment}")
	private Integer environmentNumberInFile;
	private String environmentNumberHeroku = System.getenv("ENVIRONMENT_NUMBER");

	public Integer getEnvironmentNumber(){
		return environmentNumberHeroku != null ? Integer.valueOf(environmentNumberHeroku) :  environmentNumberInFile;
	}
	
	public InsynctiveVSEnvironment findEnvironment(){
		 return InsynctiveVSEnvironment.findByProperyNumber(getEnvironmentNumber());
	}
	
	public String getVSAccount(){
		return InsynctiveVSEnvironment.findByProperyNumber(getEnvironmentNumber()).getVsAccount();
	}

	public String getVSProject(){
		return InsynctiveVSEnvironment.findByProperyNumber(getEnvironmentNumber()).getVsProject();
	}
	
	public Boolean isProduction(){
		return InsynctiveVSEnvironment.findByProperyNumber(getEnvironmentNumber()).equals(InsynctiveVSEnvironment.PRODUCTION);
	}
	
}
