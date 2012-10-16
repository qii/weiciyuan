package org.qii.weiciyuan.ui.preference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.othercomponent.AppNewMsgAlarm;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

/**
 * User: qii
 * Date: 12-8-30
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference clear_cache;
    private Preference frequency;

    private CalcCacheSize calcTask;
    private RemoveCache removeCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        clear_cache = findPreference(SettingActivity.CLEAR_CACHE);
        frequency = findPreference(SettingActivity.FREQUENCY);

        clear_cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                RemoveCacheDialog dialog = new RemoveCacheDialog();
                dialog.setTargetFragment(SettingsFragment.this, 0);
                dialog.show(getFragmentManager(), "");
                return true;
            }
        });

        calcTask = new CalcCacheSize();
        calcTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);



//        findPreference(SettingActivity.DOWNLOAD_EMOTIONS).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                Intent service = new Intent(getActivity(), DownloadEmotionsService.class);
//                service.putExtra("token", GlobalContext.getInstance().getSpecialToken());
//                getActivity().startService(service);
//                return true;
//            }
//        });

        buildSummary();
    }


    public void removeCache() {
        if (calcTask != null && calcTask.getStatus() != MyAsyncTask.Status.FINISHED) {
            calcTask.cancel(true);
        }

        if (removeCache == null || removeCache.getStatus() == MyAsyncTask.Status.FINISHED) {
            removeCache = new RemoveCache();
            removeCache.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        if (calcTask != null)
            calcTask.cancel(true);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        if (key.equals(SettingActivity.ENABLE_FETCH_MSG)) {
            boolean value = sharedPreferences.getBoolean(key, false);
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


        if (key.equals(SettingActivity.FONT_SIZE)) {
            String value = sharedPreferences.getString(key, "15");
            GlobalContext.getInstance().setFontSize(Integer.valueOf(value));
        }


        if (key.equals(SettingActivity.SOUND)) {
            boolean value = sharedPreferences.getBoolean(key, true);
            GlobalContext.getInstance().setEnableSound(value);
        }

        if (key.equals(SettingActivity.AUTO_REFRESH)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            GlobalContext.getInstance().setEnableAutoRefresh(value);
        }
    }

    private void buildSummary() {
        String value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(SettingActivity.FREQUENCY, "1");
        frequency.setSummary(getActivity().getResources().getStringArray(R.array.frequency)[Integer.valueOf(value) - 1]);
    }


    private void reload() {

        Intent intent = getActivity().getIntent();
        getActivity().overridePendingTransition(0, 0);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();

        getActivity().overridePendingTransition(0, 0);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.stay, R.anim.alphaout);
    }


    private void cancelAlarm() {

        AppNewMsgAlarm.stopAlarm(getActivity(), true);
    }

    private class CalcCacheSize extends MyAsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return FileManager.getPictureCacheSize();
        }

        @Override
        protected void onPostExecute(String s) {
            clear_cache.setSummary(getString(R.string.clear_avatar_and_pic) + "(" + s + ")");
        }


    }


    private class RemoveCache extends MyAsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), getString(R.string.remove_ing), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return FileManager.deletePictureCache();
        }

        @Override
        protected void onPostExecute(Boolean s) {

            if (getActivity() == null)
                return;

            if (calcTask == null || calcTask.getStatus() == Status.FINISHED) {
                calcTask = new CalcCacheSize();
                calcTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }


    }
}