package insynctive.support.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import insynctive.support.utils.Property;
import insynctive.support.utils.UserDetails;
import insynctive.support.utils.slack.SlackMessage;
import insynctive.support.utils.slack.SlackMessageObject;
import insynctive.support.utils.slack.SlackUtilInsynctive;
import insynctive.support.utils.slack.builder.SlackMessageBuilder;
import support.utils.vs.builder.VisualStudioBuildBuilder;
import support.utils.vs.master.VisualStudioBuildDefinition;
import support.utils.vs.master.VisualStudioUtil;

@Component
public class ScheduleMethods {

	private List<String> notSendMessages = newUniqueList();
	private VisualStudioUtil vsUtil = new VisualStudioUtil("evaleiras@insynctive.com", "d5bb3o6xbmecnqgsa3vgequknbu3qyv7zf3shdbtijrwrpmhauwq");
	
	@Autowired
	private Property property;
	
	private List<String> newUniqueList() {
		return new ArrayList<String>() {
			@Override
			public boolean add(String e) {
				if (!contains(e)) {
					return super.add(e);
				}
				return false;
			}
		};
	}

	//Repeat 1 per hour
	@Scheduled(cron = "0 0 * * * ?")
//	@Scheduled(fixedDelay = 60000)
	public void checkWorkInProgressAndCallIfHaveMoreThanOne() throws Exception {
		List<UserDetails> usersInEnvironment = UserDetails.values(property.findEnvironment());
		List<UserDetails> values = Arrays.asList(usersInEnvironment.stream().filter((us) -> us.isQa() || us.isDev()).toArray(UserDetails[]::new));
		CheckIfSendMessageAndSend(values);
	}

	@Scheduled(cron = "0 5/5 * * * ?")
//	@Scheduled(fixedDelay = 10000)
	public void sendNoSendMessages() throws Exception{
		List<UserDetails> values = new ArrayList<>();
		for(String email : notSendMessages){ values.add(UserDetails.findByEmail(email)); }
		CheckIfSendMessageAndSend(values);
	}

	@Scheduled(cron = "0 0/30 * * * ?")
	public void runPerformanceTests() throws Exception{
		vsUtil.queueABuild(new VisualStudioBuildBuilder().addDefinition(new VisualStudioBuildDefinition(22)).setBranch("master").build(), "insynctive", "insynctive");
		vsUtil.queueABuild(new VisualStudioBuildBuilder().addDefinition(new VisualStudioBuildDefinition(25)).setBranch("master").build(), "insynctive", "insynctive");
		vsUtil.queueABuild(new VisualStudioBuildBuilder().addDefinition(new VisualStudioBuildDefinition(26)).setBranch("master").build(), "insynctive", "insynctive");
		vsUtil.queueABuild(new VisualStudioBuildBuilder().addDefinition(new VisualStudioBuildDefinition(27)).setBranch("master").build(), "insynctive", "insynctive");
		vsUtil.queueABuild(new VisualStudioBuildBuilder().addDefinition(new VisualStudioBuildDefinition(28)).setBranch("master").build(), "insynctive", "insynctive");
	}
	
	private void CheckIfSendMessageAndSend(List<UserDetails> listOfUsers) throws Exception {
		notSendMessages = newUniqueList();
		for(UserDetails user : listOfUsers){
			SlackMessageObject message = null;
			Integer countWorkInProgressCurrentIteration = vsUtil.countWorkInProgressCurrentIteration(user.name, property.getVSProject(), property.getVSAccount());
			if (countWorkInProgressCurrentIteration > 1) {
				message = new SlackMessageBuilder()
					.setIconEmoji(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.img)
					.setUsername(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.senderName)
					.setText(String.format(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.message, countWorkInProgressCurrentIteration))
					.setChannel(user.slackID)
					.build();
				
			} else if (countWorkInProgressCurrentIteration == 0) {
				message = new SlackMessageBuilder()
					.setIconEmoji(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.img)
					.setUsername(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.senderName)
					.setText(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.message)
					.setChannel(user.slackID)
					.build();
				}

			if(message != null && !SlackUtilInsynctive.sendMessageIfOnline(message)){
				notSendMessages.add(user.email);
			}
		}	
	}
}
