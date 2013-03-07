package assassins.ui.prototypes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Notifier {

	public static void sendStatusNotification(Context c, CharSequence contentTitle, CharSequence contentText, int iconResource) 
	{
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) (c.getSystemService(ns));

		Intent notificationIntent = new Intent(c, ProjectAssassins.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(c, 0, notificationIntent, 0);

	    Notification notif = new Notification(iconResource, contentText, System.currentTimeMillis());
	    notif.setLatestEventInfo(c, contentTitle, contentText, contentIntent/*need tp handle task management*/);

	    notif.defaults = Notification.DEFAULT_ALL;

	    mNotificationManager.notify(iconResource, notif);
	}

}
