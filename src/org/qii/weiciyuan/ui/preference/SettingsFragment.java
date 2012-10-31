package org.qii.weiciyuan.ui.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-8-30
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(SettingActivity.SOUND)) {
            boolean value = sharedPreferences.getBoolean(key, true);
            GlobalContext.getInstance().setEnableSound(value);
        }

        if (key.equals(SettingActivity.AUTO_REFRESH)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            GlobalContext.getInstance().setEnableAutoRefresh(value);
        }
    }

}