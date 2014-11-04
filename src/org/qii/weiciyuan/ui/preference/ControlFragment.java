package org.qii.weiciyuan.ui.preference;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.Utility;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * User: qii
 * Date: 12-10-19
 */
public class ControlFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
        addPreferencesFromResource(R.xml.control_pref);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(
                        this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(
                        this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingActivity.DISABLE_DOWNLOAD_AVATAR_PIC)) {

        }

        if (key.equals(SettingActivity.COMMENT_REPOST_AVATAR)) {
            switch (SettingUtility.getCommentRepostAvatar()) {
                case 1:
                    SettingUtility.setEnableCommentRepostAvatar(true);
                    break;
                case 2:
                    SettingUtility.setEnableCommentRepostAvatar(false);
                    break;
                case 3:
                    SettingUtility.setEnableCommentRepostAvatar(Utility.isWifi(getActivity()));
                    break;
            }
        }
    }
}
