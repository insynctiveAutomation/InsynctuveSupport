package insynctive.support.schedule;

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

	@Autowired
	private Property property;

	@Scheduled(fixedDelay = 200000)
	public void checkWorkInProgressAndCallIfHaveMoreThanOne() throws Exception {
		List<UserDetails> values = UserDetails.values(property.findEnvironment());
		for (UserDetails user : values.stream().filter((us) -> us.isQa() || us.isDev()).toArray(UserDetails[]::new)) {
			Integer countWorkInProgressCurrentIteration = VisualStudioUtil.countWorkInProgressCurrentIteration(user.name, property.getVSProject(), property.getVSAccount());
			if (countWorkInProgressCurrentIteration > 1) {
				SlackMessageObject message = new SlackMessageBuilder()
						.setIconEmoji(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.img)
						.setUsername(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.senderName)
						.setText(String.format(SlackMessage.HAVE_MORE_THAN_ONE_WORK_IN_PROGRESS.message, countWorkInProgressCurrentIteration))
						.setChannel(user.slackMention).build();
				SlackUtil.sendMessage(message);
			} else if (countWorkInProgressCurrentIteration == 0) {
				// SlackMessageObject message = new SlackMessageBuilder()
				// .setUsername(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.senderName)
				// .setText(SlackMessage.NO_HAVE_WORK_IN_PROGRESS_CURRENT_SPRINT.message)
				// .setChannel(user.slackMention)
				// .build();
				// SlackUtil.sendMessage(message);
			}
		}
	}
}
