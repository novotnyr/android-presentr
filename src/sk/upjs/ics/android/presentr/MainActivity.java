package sk.upjs.ics.android.presentr;

import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

public class MainActivity extends ListActivity {

	private static final int REQUEST_CODE_SHOW_PREFERENCES = 0;
	private BroadcastReceiver userListBroadcastReceiver;
	private ArrayAdapter<User> listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1);
		setListAdapter(listAdapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter userListIntentFilter = new IntentFilter(FetchOnlineUsersIntentService.INTENT_ACTION_USER_LIST);
		userListBroadcastReceiver = new UserListBroadcastReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(userListBroadcastReceiver, userListIntentFilter);
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(userListBroadcastReceiver);
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			onRefreshOptionItemClick();
			return true;
		case R.id.action_settings:
			onSettingsOptionItemClick();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void onSettingsOptionItemClick() {
		Intent showPreferencesIntent = new Intent(this, PreferencesActivity.class);
		startActivityForResult(showPreferencesIntent, REQUEST_CODE_SHOW_PREFERENCES);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_SHOW_PREFERENCES:
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void onRefreshOptionItemClick() {
		Intent service = new Intent(this, FetchOnlineUsersIntentService.class);
		service.putExtra(FetchOnlineUsersIntentService.EXTRA_SUPPRESS_TRIGGER_NOTIFICATION, true);
		startService(service);
	}

	public void onEmptyListViewClick(View view) {
		onRefreshOptionItemClick();
	}
	
	private class UserListBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			List<User> users = (List<User>) intent.getSerializableExtra(FetchOnlineUsersIntentService.EXTRA_USER_LIST);
			listAdapter.clear();
			for (User user : users) {
				listAdapter.add(user);				
			}
		}
		
	}
}
