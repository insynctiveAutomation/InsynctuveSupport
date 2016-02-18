package insynctive.support.schedule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import insynctive.support.utils.UserDetails;
import insynctive.support.utils.slack.SlackMessageObject;
import insynctive.support.utils.slack.SlackUtil;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;

@Component
public class ScheduleMethodsImplementation {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			
		@Scheduled(fixedDelay=3600000)
		public void checkWorkInProgressAndCallIfHaveMoreThanOne() throws IOException {
		SlackMessageObject message = new SlackMessageBuilder()
				.setUsername("Schedule Message")
				.setText("The time is now " + dateFormat.format(new Date()))
				.setChannel(UserDetails.EUGENIO_VALEIRAS.slackMention)
				.build();
		
		SlackUtil.sendMessage(message);
	}

}
