package org.qii.weiciyuan.ui.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.Utility;

/**
 * User: qii
 * Date: 12-10-19
 */
public class ControlFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference msgCount = null;
    private Preference commentRepostListAvatar = null;
    private Preference uploadPicQuality = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);

        addPreferencesFromResource(R.xml.control_pref);

        msgCount = findPreference(SettingActivity.MSG_COUNT);
        commentRepostListAvatar = findPreference(SettingActivity.COMMENT_REPOST_AVATAR);
        uploadPicQuality = findPreference(SettingActivity.UPLOAD_PIC_QUALITY);

        buildSummary();

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

    }


    private void buildSummary() {
        String value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingActivity.MSG_COUNT, "3");
        msgCount.setSummary(getActivity().getResources().getStringArray(R.array.msg_count_title)[Integer.valueOf(value) - 1]);

        value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingActivity.COMMENT_REPOST_AVATAR, "3");
        commentRepostListAvatar.setSummary(getActivity().getResources().getStringArray(R.array.comment_repost_list_avatar_mode)[Integer.valueOf(value) - 1]);


        value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingActivity.UPLOAD_PIC_QUALITY, "1");
        uploadPicQuality.setSummary(getActivity().getResources().getStringArray(R.array.upload_pic_quality_hack_bug)[Integer.valueOf(value) - 1]);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);

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

        buildSummary();
    }
}
