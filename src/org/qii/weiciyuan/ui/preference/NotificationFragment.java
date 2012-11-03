package org.qii.weiciyuan.ui.preference;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.othercomponent.AppNewMsgAlarm;

/**
 * User: qii
 * Date: 12-10-24
 */
public class NotificationFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference frequency;
    private Preference ringtone;
    private Uri uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.notification_pref);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        frequency = findPreference(SettingActivity.FREQUENCY);
        ringtone = findPreference(SettingActivity.ENABLE_RINGTONE);
        ringtone.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.ringtone));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, uri);
                startActivityForResult(intent, 1);
                return true;
            }
        });
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String path = sharedPref.getString(SettingActivity.ENABLE_RINGTONE, "");
        if (!TextUtils.isEmpty(path)) {
            uri = Uri.parse(path);
        }

        buildSummary();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String ringTonePath = "";
            uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                ringTonePath = uri.toString();
            }

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPref.edit().putString(SettingActivity.ENABLE_RINGTONE, ringTonePath).commit();
            buildSummary();
        }
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

        if (uri != null) {
            Ringtone r = RingtoneManager.getRingtone(getActivity(), uri);
            ringtone.setSummary(r.getTitle(getActivity()));
        }
    }

}
