package insynctive.support.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Property {

	@Value("${environment}")
	private Integer environmentNumberInFile;
	private String environmentNumberHeroku = System.getenv("ENVIRONMENT_NUMBER");

	private Integer getEnvironment(){
		return environmentNumberHeroku != null ? Integer.valueOf(environmentNumberHeroku) :  environmentNumberInFile;
	}
	
	public InsynctiveVSEnvironment findEnvironment() throws Exception{
		 return InsynctiveVSEnvironment.findByProperyNumber(getEnvironment());
	}
	
	public String getVSAccount() throws Exception{
		return InsynctiveVSEnvironment.findByProperyNumber(getEnvironment()).getVsAccount();
	}

	public String getVSProject() throws Exception{
		return InsynctiveVSEnvironment.findByProperyNumber(getEnvironment()).getVsProject();
	}
	
	public Boolean isProduction() throws Exception{
		return InsynctiveVSEnvironment.findByProperyNumber(getEnvironment()).equals(InsynctiveVSEnvironment.PRODUCTION);
	}
	
}
