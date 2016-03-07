package insynctive.support.schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import insynctive.support.utils.Property;
import insynctive.support.utils.UserDetails;
import insynctive.support.utils.VisualStudioUtil;
import insynctive.support.utils.slack.SlackMessage;
import insynctive.support.utils.slack.SlackMessageObject;
import insynctive.support.utils.slack.SlackUtil;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;

@Component
public class ScheduleMethods {

	private List<String> notSendMessages = new ArrayList<String>() {
		
		@Override
		public boolean add(String e) {
			if(!contains(e)){
				return super.add(e); 
			}
			return false;
		};
		
	};
	
	@Autowired
	private Property property;

	//Repeat 1 per hour
	@Scheduled(cron = "0 0 * * * ?")
//	@Scheduled(fixedDelay = 60000)
	public void checkWorkInProgressAndCallIfHaveMoreThanOne() throws Exception {
		List<UserDetails> values = UserDetails.values(property.findEnvironment());
		CheckIfSendMessageAndSend(Arrays.asList(values.stream().filter((us) -> us.isQa() || us.isDev()).toArray(UserDetails[]::new)));
	}

	@Scheduled(cron = "0 5/5 * * * ?")
//	@Scheduled(fixedDelay = 10000)
	public void sendNoSendMessages() throws Exception{
		List<UserDetails> values = new ArrayList<>();
		for(String email : notSendMessages){
			values.add(UserDetails.findByEmail(email));
		}
		CheckIfSendMessageAndSend(values);
	}
	
	private void CheckIfSendMessageAndSend(List<UserDetails> listOfUsers) throws Exception {
		for(UserDetails user : listOfUsers){
			Integer countWorkInProgressCurrentIteration = VisualStudioUtil.countWorkInProgressCurrentIteration(user.name, property.getVSProject(), property.getVSAccount());
			if (countWorkInProgressCurrentIteration > 1) {
				SlackMessageObject message = new SlackMessageBuilder()
					.setIconEmoji(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.img)
					.setUsername(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.senderName)
					.setText(String.format(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.message, countWorkInProgressCurrentIteration))
					.setChannel(user.slackID)
					.build();
				
				if(!SlackUtil.sendMessageIfOnline(message)){
					notSendMessages.add(user.email);
				}
				
			} else if (countWorkInProgressCurrentIteration == 0) {
				SlackMessageObject message = new SlackMessageBuilder()
					.setIconEmoji(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.img)
					.setUsername(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.senderName)
					.setText(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.message)
					.setChannel(user.slackID)
					.build();
				
				if(!SlackUtil.sendMessageIfOnline(message)){
					notSendMessages.add(user.email);
				}
				
			}
		}	
	}
}
