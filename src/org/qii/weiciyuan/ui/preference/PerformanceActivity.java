package org.qii.weiciyuan.ui.preference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;

/**
 * User: qii
 * Date: 13-2-14
 */
public class PerformanceActivity extends AbstractAppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.pref_performance_title));

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new PerformanceFragment())
                    .commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return false;
    }

    public static class PerformanceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(false);

            addPreferencesFromResource(R.xml.performance_pref);
        }


    }
}
