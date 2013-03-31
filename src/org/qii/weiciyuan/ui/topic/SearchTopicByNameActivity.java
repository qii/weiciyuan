package org.qii.weiciyuan.ui.topic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-9-8
 */
public class SearchTopicByNameActivity extends AbstractAppActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String q = getIntent().getStringExtra("q");
        if (TextUtils.isEmpty(q)) {
            Uri data = getIntent().getData();
            String d = data.toString();
            int index = d.indexOf("#");
            q = d.substring(index + 1, d.length() - 1);
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("#" + q + "#");
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SearchTopicByNameFragment(q))
                    .commit();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                Intent intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;

        }
        return false;
    }
}
