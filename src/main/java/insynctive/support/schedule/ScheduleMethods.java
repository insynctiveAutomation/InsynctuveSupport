package insynctive.support.schedule;

import java.io.IOException;

public interface ScheduleMethods {

//	@Scheduled(fixedDelay=3600000)
	public void checkWorkInProgressAndCallIfHaveMoreThanOne() throws IOException;
	
}
