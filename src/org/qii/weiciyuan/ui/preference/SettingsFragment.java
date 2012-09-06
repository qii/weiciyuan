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
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.othercomponent.FetchNewMsgService;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

/**
 * User: qii
 * Date: 12-8-30
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference clear_cache;
    private CalcCacheSize task;

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

        task = new CalcCacheSize();
        // task.execute();

        findPreference(SettingActivity.OFFICIAL_WEIBO).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                UserBean bean = new UserBean();
                bean.setScreen_name(getString(R.string.app_name));
                String token = GlobalContext.getInstance().getSpecialToken();
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("user", bean);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        task.cancel(true);
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
                GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Black);
            if (value.equals("2"))
                GlobalContext.getInstance().setAppTheme(R.style.AppTheme_White);
            if (value.equals("3"))
                GlobalContext.getInstance().setAppTheme(R.style.AppTheme_Black_White);

            reload();
        }

        if (key.equals(SettingActivity.FONT_SIZE)) {
            String value = sharedPreferences.getString(key, "13");
            GlobalContext.getInstance().setFontSize(Integer.valueOf(value));
        }

        if (key.equals(SettingActivity.SHOW_BIG_PIC)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            GlobalContext.getInstance().setEnableBigPic(value);
        }
    }

    private void reload() {

        Intent intent = getActivity().getIntent();
        getActivity().overridePendingTransition(0, 0);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();

        getActivity().overridePendingTransition(0, 0);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.stay,R.anim.alphaout);
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

    private class CalcCacheSize extends AsyncTask<Void, Void, String> {

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