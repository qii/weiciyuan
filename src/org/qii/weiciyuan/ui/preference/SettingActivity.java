package org.qii.weiciyuan.ui.preference;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.othercomponent.FetchNewMsgService;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-6
 */
public class SettingActivity extends AbstractAppActivity {


    public static final String ENABLE_PIC = "show_picture";
    public static final String THEME = "theme";
    public static final String FREQUENCY = "frequency";
    public static final String ENABLE_FETCH_MSG = "enable_fetch_msg";
    public static final String CLEAR_CACHE = "clear_cache";


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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return false;
    }


}


class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference clear_cache;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        clear_cache = findPreference(SettingActivity.CLEAR_CACHE);

        clear_cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FileManager.deleteCache();
                return true;
            }
        });

        new calcCacheSize().execute();
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(SettingActivity.ENABLE_PIC)) {
            boolean value = sharedPreferences.getBoolean(key, true);
            if (value) {
                GlobalContext.getInstance().setEnablePic(true);
            } else {
                GlobalContext.getInstance().setEnablePic(false);
            }
        }

        if (key.equals(SettingActivity.ENABLE_FETCH_MSG)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            if (!value)
                cancelAlarm();
        }

        if (key.equals(SettingActivity.FREQUENCY)) {
            String value = sharedPreferences.getString(key, "1");

            if (value.equals("1"))
                startAlarm(3 * 60 * 1000);
            if (value.equals("2"))
                startAlarm(AlarmManager.INTERVAL_FIFTEEN_MINUTES);
            if (value.equals("3"))
                startAlarm(AlarmManager.INTERVAL_HALF_HOUR);

        }

        if (key.equals(SettingActivity.THEME)) {
            String value = sharedPreferences.getString(key, "1");
            if (value.equals("1"))
                GlobalContext.getInstance().setAppTheme(android.R.style.Theme_Holo);
            if (value.equals("2"))
                GlobalContext.getInstance().setAppTheme(android.R.style.Theme_Holo_Light);
            if (value.equals("3"))
                GlobalContext.getInstance().setAppTheme(android.R.style.Theme_Holo_Light_DarkActionBar);

            reload();
        }
    }

    private void reload() {

        Intent intent = getActivity().getIntent();
        getActivity().overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();

        getActivity().overridePendingTransition(0, 0);
        startActivity(intent);
    }


    private void startAlarm(long time) {
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(
                Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), FetchNewMsgService.class);
        PendingIntent sender = PendingIntent.getService(getActivity(), 195, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, time, time, sender);
        Toast.makeText(getActivity(), "start fetch new message", Toast.LENGTH_SHORT).show();

    }

    private void cancelAlarm() {
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(
                Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), FetchNewMsgService.class);
        PendingIntent sender = PendingIntent.getService(getActivity(), 195, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarm.cancel(sender);
    }

    private class calcCacheSize extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return FileManager.getCacheSize();
        }

        @Override
        protected void onPostExecute(String s) {
            clear_cache.setSummary(getString(R.string.clear_avatar_and_pic) + " " + s);
        }
    }
}