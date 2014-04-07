package sk.upjs.ics.android.presentr;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

public class PreferencesActivity extends android.preference.PreferenceActivity implements OnSharedPreferenceChangeListener {
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		initializeSummaries();
	}

	private void initializeSummaries() {
		SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
		for(String key : sharedPreferences.getAll().keySet()) {
			onSharedPreferenceChanged(sharedPreferences, key);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = findPreference(key);
		if(preference != null && preference instanceof EditTextPreference) {
			EditTextPreference editTextPreference = (EditTextPreference) preference;
			preference.setSummary(editTextPreference.getText());
		}
		if("isPeriodicallyUpdating".equals(key)) {
			boolean isPeriodicallyUpdating = sharedPreferences.getBoolean(key, false);
			if(isPeriodicallyUpdating) {
				FetchOnlineUsersIntentService.schedule(this);
			} else {
				FetchOnlineUsersIntentService.unschedule(this);
			}
		}
	}
}
