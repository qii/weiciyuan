package org.qii.weiciyuan.ui.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: qii
 * Date: 12-10-4
 */
public class AppearanceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference listAvatarMode = null;
    private Preference listPicMode = null;
    private Preference theme = null;
    private Preference listFontSize = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.appearance_pref);


        listAvatarMode = findPreference(SettingActivity.LIST_AVATAR_MODE);
        listPicMode = findPreference(SettingActivity.LIST_PIC_MODE);
        listFontSize = findPreference(SettingActivity.FONT_SIZE);
        theme = findPreference(SettingActivity.THEME);

        buildSummary();


        setRetainInstance(true);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);

    }

    private void buildSummary() {
        String value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingActivity.LIST_AVATAR_MODE, "1");
        listAvatarMode.setSummary(getActivity().getResources().getStringArray(R.array.list_avatar_mode)[Integer.valueOf(value) - 1]);

        value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingActivity.LIST_PIC_MODE, "1");
        listPicMode.setSummary(getActivity().getResources().getStringArray(R.array.list_pic_mode)[Integer.valueOf(value) - 1]);

        value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingActivity.THEME, "2");
        theme.setSummary(getActivity().getResources().getStringArray(R.array.theme)[Integer.valueOf(value) - 1]);



    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(SettingActivity.LIST_AVATAR_MODE)) {
            String value = sharedPreferences.getString(key, "1");
            if (value.equals("1"))
                GlobalContext.getInstance().setEnableBigAvatar(false);
            if (value.equals("2"))
                GlobalContext.getInstance().setEnableBigAvatar(true);
            if (value.equals("3")) {
                ConnectivityManager cm = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        GlobalContext.getInstance().setEnableBigAvatar(true);
                    } else {
                        GlobalContext.getInstance().setEnableBigAvatar(false);
                    }
                }
            }
            buildSummary();
        }

        if (key.equals(SettingActivity.LIST_PIC_MODE)) {
            String value = sharedPreferences.getString(key, "1");
            if (value.equals("1"))
                GlobalContext.getInstance().setEnableBigPic(false);
            if (value.equals("2"))
                GlobalContext.getInstance().setEnableBigPic(true);
            if (value.equals("3")) {
                ConnectivityManager cm = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        GlobalContext.getInstance().setEnableBigPic(true);
                    } else {
                        GlobalContext.getInstance().setEnableBigPic(false);
                    }
                }
            }
            buildSummary();
        }

    }


}
