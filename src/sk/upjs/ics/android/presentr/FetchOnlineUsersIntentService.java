package sk.upjs.ics.android.presentr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class FetchOnlineUsersIntentService extends IntentService {
	public static final String EXTRA_USER_LIST = "userList";

	public static final String INTENT_ACTION_USER_LIST = User.class.getName();

	public static final String TAG = FetchOnlineUsersIntentService.class.getSimpleName(); 
	
	public static final String ENDPOINT_URL = "http://192.168.1.102/presentr/available-users";
	
	public static final String EXTRA_SUPPRESS_TRIGGER_NOTIFICATION = "suppressTriggerNotification";

	private static final int NOTIFICATION_CLICK_REQUEST_CODE = 0;

	private static final int NO_FLAGS = 0;

	private static final int NOTIFICATION_ID = 0;

	private static final int SERVICE_REQUEST_CODE = 0;
	
	public FetchOnlineUsersIntentService() {
		super("FetchOnlineUsersIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		BufferedReader reader = null;
		try {
			URL url = new URL(ENDPOINT_URL);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			List<User> users = new LinkedList<User>();
			while((line = reader.readLine()) != null) {
				User user = parseUser(line);
				if(user != null) {
					users.add(user);
				}
			}
			Log.i(TAG, "Successfully downloaded user list");
			if(! intent.hasExtra(EXTRA_SUPPRESS_TRIGGER_NOTIFICATION)) {
				notify(users);
			}
			broadcast(users);
		} catch (IOException e) {
			Log.e(TAG, "Cannot download available users", e);
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					Log.e(TAG, "Cannot close URL reader", e);
				}
			}
		}
	}

	private void broadcast(List<User> users) {
		LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
		Intent intent = new Intent(INTENT_ACTION_USER_LIST);
		intent.putExtra(EXTRA_USER_LIST, (Serializable) users);
		broadcastManager.sendBroadcast(intent);
	}

	private void notify(List<User> users) {
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_CLICK_REQUEST_CODE, intent, NO_FLAGS);
		
		Notification notification = new NotificationCompat.Builder(this)
			.setContentTitle("Poèet prihlásených: " + users.size())
			.setAutoCancel(true)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(pendingIntent)
			.build();
		
		NotificationManager notificationManager = 
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);	
	}

	private User parseUser(String line) {
		String[] components = line.split(":");
		if(components.length >= 1) {
			String userName = components[0];
			return new User(userName);
		}
		return null;
	}
	
	public static void schedule(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		PendingIntent pendingIntent = getPendingIntent(context);
		
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 
				SystemClock.elapsedRealtime(), 7 * 1000, pendingIntent);
	}

	private static PendingIntent getPendingIntent(Context context) {
		Intent intent = new Intent(context, FetchOnlineUsersIntentService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context, SERVICE_REQUEST_CODE, intent, NO_FLAGS);
		return pendingIntent;
	}

	public static void unschedule(Context context) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
		alarmManager.cancel(getPendingIntent(context));
	}
}
