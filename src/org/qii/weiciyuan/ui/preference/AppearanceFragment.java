package org.qii.weiciyuan.ui.preference;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * User: qii
 * Date: 12-10-4
 */
public class AppearanceFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.appearance_pref);
        setRetainInstance(false);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingActivity.LIST_AVATAR_MODE)) {
            String value = sharedPreferences.getString(key, "1");
            if (value.equals("1")) {
                SettingUtility.setEnableBigAvatar(false);
            }
            if (value.equals("2")) {
                SettingUtility.setEnableBigAvatar(true);
            }
            if (value.equals("3")) {
                SettingUtility.setEnableBigAvatar(Utility.isWifi(getActivity()));
            }
        }

        if (key.equals(SettingActivity.LIST_PIC_MODE)) {
            String value = sharedPreferences.getString(key, "1");
            if (value.equals("1")) {
                SettingUtility.setEnableBigPic(false);
            }
            if (value.equals("2")) {
                SettingUtility.setEnableBigPic(true);
            }
            if (value.equals("3")) {
                SettingUtility.setEnableBigPic(Utility.isWifi(getActivity()));
            }
        }
        if (key.equals(SettingActivity.LIST_HIGH_PIC_MODE)) {
            GlobalContext.getInstance().getBitmapCache().evictAll();
        }
    }
}
