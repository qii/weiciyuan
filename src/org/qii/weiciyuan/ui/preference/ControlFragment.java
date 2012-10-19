package org.qii.weiciyuan.ui.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-10-19
 */
public class ControlFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference upload_pic_quality;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.control);
        upload_pic_quality = findPreference(SettingActivity.UPLOAD_PIC_QUALITY);
//        upload_pic_quality.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                buildSummary();
//                return false;
//            }
//        });
//
//        buildSummary();

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);

    }

    private void buildSummary() {
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String value = sharedPref.getString(SettingActivity.UPLOAD_PIC_QUALITY, "4");
//        upload_pic_quality.setSummary(getActivity().getResources().getStringArray(R.array.upload_pic_quality)[Integer.valueOf(value) - 1]);
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
    }
}
