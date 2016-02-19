package insynctive.support.schedule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import insynctive.support.utils.UserDetails;
import insynctive.support.utils.VisualStudioUtil;
import insynctive.support.utils.slack.SlackMessage;
import insynctive.support.utils.slack.SlackMessageObject;
import insynctive.support.utils.slack.SlackUtil;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;

@Component
public class ScheduleMethods {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		
		@Scheduled(fixedDelay=3600000)
		public void checkWorkInProgressAndCallIfHaveMoreThanOne() throws Exception {	
			for(UserDetails user : UserDetails.values()){
				Integer countWorkInProgressCurrentIteration = VisualStudioUtil.countWorkInProgressCurrentIteration(user.name, "Insynctive", "insynctive");
				if(countWorkInProgressCurrentIteration > 1){
					SlackMessageObject message = new SlackMessageBuilder()
							.setIconEmoji(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.img)
							.setUsername(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.senderName)
							.setText(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.message)
							.setChannel(user.slackMention)
							.build();
					SlackUtil.sendMessage(message);
				} else if(countWorkInProgressCurrentIteration == 0){
//					SlackMessageObject message = new SlackMessageBuilder()
//							.setUsername(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.senderName)
//							.setText(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.message)
//							.setChannel(user.slackMention)
//							.build();
//					SlackUtil.sendMessage(message);
				}
			}
		}
}
