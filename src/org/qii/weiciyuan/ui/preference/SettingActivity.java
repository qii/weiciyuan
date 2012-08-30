package org.qii.weiciyuan.ui.preference;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.othercomponent.FetchNewMsgService;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-6
 */
public class SettingActivity extends AbstractAppActivity {


    public static final String ENABLE_PIC = "show_picture";
    public static final String THEME = "theme";
    public static final String FREQUENCY = "frequency";
    public static final String ENABLE_FETCH_MSG = "enable_fetch_msg";
    public static final String CLEAR_CACHE = "clear_cache";
    public static final String FONT_SIZE = "font_size";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.setting));

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return false;
    }


}


