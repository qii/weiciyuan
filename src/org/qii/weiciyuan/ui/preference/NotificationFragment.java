package org.qii.weiciyuan.ui.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.othercomponent.AppNewMsgAlarm;

/**
 * User: qii
 * Date: 12-10-24
 */
public class NotificationFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference frequency;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_pref);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        frequency = findPreference(SettingActivity.FREQUENCY);
        buildSummary();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingActivity.ENABLE_FETCH_MSG)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            buildSummary();
            if (value) {
                AppNewMsgAlarm.startAlarm(getActivity(), false);
            } else {
                AppNewMsgAlarm.stopAlarm(getActivity(), true);
            }
        }

        if (key.equals(SettingActivity.FREQUENCY)) {

            AppNewMsgAlarm.startAlarm(getActivity(), false);
            buildSummary();
        }


    }

    private void buildSummary() {
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(SettingActivity.ENABLE_FETCH_MSG, false)) {
            String value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingActivity.FREQUENCY, "1");
            frequency.setSummary(getActivity().getResources().getStringArray(R.array.frequency)[Integer.valueOf(value) - 1]);
        } else {
            frequency.setSummary(getString(R.string.stopped));

        }
    }

}
