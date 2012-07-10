package au.gov.ansto.bragg.nbi.service.notification.support;

import org.gumtree.service.notification.support.AbstractNotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterNotificationProvider extends AbstractNotificationProvider<TwitterNotification>{

	private static final Logger logger = LoggerFactory.getLogger(TwitterNotificationProvider.class);
	
	@Override
	public void handleNotification(TwitterNotification notification) {
		Twitter twitter = new TwitterFactory().getInstance(
				notification.getScreenName(), notification.getPassword());
		try {
			twitter.updateStatus(notification.getMessage());
		} catch (TwitterException e) {
			logger.error("Failed to send twitter message.", e);
		}
	}

	@Override
	public boolean isLongOperation(TwitterNotification notification) {
		return true;
	}

}
