package org.qii.weiciyuan.ui.preference;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.othercomponent.FetchNewMsgService;

/**
 * User: Jiang Qi
 * Date: 12-8-6
 */
public class SettingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.setting));

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        findPreference("clear_cache").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return false;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Toast.makeText(getActivity(), "clgggggear", Toast.LENGTH_SHORT).show();

        if (key.equals("enable_fetch_msg")) {
            boolean value = sharedPreferences.getBoolean("enable_fetch_msg", false);
            if (!value)
                cancelAlarm();
        }

        if (key.equals("frequency")) {
            Toast.makeText(getActivity(), "frequency", Toast.LENGTH_SHORT).show();

            String value = sharedPreferences.getString(key, "1");

            if (value.endsWith("1"))
                startAlarm(AlarmManager.INTERVAL_FIFTEEN_MINUTES);
            if (value.endsWith("2"))
                startAlarm(AlarmManager.INTERVAL_HALF_HOUR);
            if (value.endsWith("3"))
                startAlarm(AlarmManager.INTERVAL_HOUR);

        }
    }

    private void startAlarm(long time) {
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(
                Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), FetchNewMsgService.class);
        PendingIntent sender = PendingIntent.getService(getActivity(), 195, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis(), time, sender);
    }

    private void cancelAlarm() {
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(
                Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), FetchNewMsgService.class);
        PendingIntent sender = PendingIntent.getService(getActivity(), 195, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarm.cancel(sender);
    }
}