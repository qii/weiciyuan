package org.qii.weiciyuan.ui.preference;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.send.WriteWeiboActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

/**
 * User: qii
 * Date: 12-9-29
 */
public class AboutFragment extends PreferenceFragment {

    private MediaPlayer mp;

    @Override
    public void onPause() {
        super.onPause();
        if (mp != null)
            mp.stop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.about_pref);
        findPreference(SettingActivity.SUGGEST).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), WriteWeiboActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("account", GlobalContext.getInstance().getAccountBean());
                intent.putExtra("content", buildContent());

                startActivity(intent);
                return true;
            }
        });

        findPreference(SettingActivity.OFFICIAL_WEIBO).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                UserBean bean = new UserBean();
                bean.setScreen_name(getString(R.string.official_weibo_link));
                String token = GlobalContext.getInstance().getSpecialToken();
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("user", bean);
                startActivity(intent);
                return true;
            }
        });

        findPreference(SettingActivity.AUTHOR).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "《星之所在》", Toast.LENGTH_SHORT).show();
                if (mp != null) {
                    mp.stop();
                }
                mp = MediaPlayer.create(getActivity(), R.raw.star);
                mp.start();
                return true;
            }
        }

        );

        String version = "";
        PackageManager packageManager = getActivity().getPackageManager();
        PackageInfo packInfo = null;
        try

        {
            packInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
        } catch (
                PackageManager.NameNotFoundException e
                )

        {
            AppLogger.e(e.getMessage());
        }

        if (packInfo != null)

        {
            version = packInfo.versionName;
        }

        if (!TextUtils.isEmpty(version))

            findPreference(SettingActivity.VERSION)

                    .

                            setSummary(version);

    }

    private String buildContent() {

        String network = "";

        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                network = "Wifi";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

                int subType = networkInfo.getSubtype();

                if (subType == TelephonyManager.NETWORK_TYPE_GPRS) {
                    network = "GPRS";
                }
            }
        }

        String version = "";
        PackageManager packageManager = getActivity().getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            AppLogger.e(e.getMessage());
        }
        if (packInfo != null) {
            version = packInfo.versionName;
        }

        return "@四次元App #四次元App反馈# " + android.os.Build.MANUFACTURER
                + " " + android.os.Build.MODEL + ",Android "
                + android.os.Build.VERSION.RELEASE + "," + network + " version:" + version;
    }
}
