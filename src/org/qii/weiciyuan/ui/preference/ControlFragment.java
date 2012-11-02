package org.qii.weiciyuan.ui.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-10-19
 */
public class ControlFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.control_pref);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingActivity.DISABLE_DOWNLOAD_AVATAR_PIC)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            if (value) {
                GlobalContext.getInstance().setEnablePic(false);
            } else {
                GlobalContext.getInstance().setEnablePic(true);
            }
        }

        if (key.equals(SettingActivity.CLOSE_COMMENT_AND_REPOST_AVATAR)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            if (value) {
                GlobalContext.getInstance().setEnableCommentRepostListAvatar(false);
            } else {
                GlobalContext.getInstance().setEnableCommentRepostListAvatar(true);
            }
        }
    }
}
