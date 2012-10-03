package org.qii.weiciyuan.ui.preference;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-9-21
 */
public class FilterActivity extends AbstractAppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.filter));

        View title = getLayoutInflater().inflate(R.layout.filteractivity_title_layout, null);
        Switch switchBtn = (Switch) title.findViewById(R.id.switchBtn);
        getActionBar().setCustomView(title, new ActionBar.LayoutParams(Gravity.RIGHT));
        getActionBar().setDisplayShowCustomEnabled(true);

        switchBtn.setChecked(GlobalContext.getInstance().isEnableFilter());
        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                GlobalContext.getInstance().setEnableFilter(isChecked);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(FilterActivity.this).edit();

                if (isChecked) {
                    editor.putBoolean(SettingActivity.FILTER, true).commit();
                } else {
                    editor.putBoolean(SettingActivity.FILTER, false).commit();
                }
            }
        });

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new FilterFragment())
                .commit();

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

}
