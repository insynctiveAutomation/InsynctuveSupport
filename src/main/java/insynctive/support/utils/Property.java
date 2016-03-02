package insynctive.support.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Property {

	@Value("${environment}")
	private Integer environmentNumberInFile;
	private String environmentNumberHeroku = System.getenv("ENVIRONMENT_NUMBER");

	private Integer getEnvironmentNumber(){
		return environmentNumberHeroku != null ? Integer.valueOf(environmentNumberHeroku) :  environmentNumberInFile;
	}
	
	public InsynctiveVSEnvironment findEnvironment() throws Exception{
		 return InsynctiveVSEnvironment.findByProperyNumber(getEnvironmentNumber());
	}
	
	public String getVSAccount() throws Exception{
		return InsynctiveVSEnvironment.findByProperyNumber(getEnvironmentNumber()).getVsAccount();
	}

	public String getVSProject() throws Exception{
		return InsynctiveVSEnvironment.findByProperyNumber(getEnvironmentNumber()).getVsProject();
	}
	
	public Boolean isProduction() throws Exception{
		return InsynctiveVSEnvironment.findByProperyNumber(getEnvironmentNumber()).equals(InsynctiveVSEnvironment.PRODUCTION);
	}
	
}
