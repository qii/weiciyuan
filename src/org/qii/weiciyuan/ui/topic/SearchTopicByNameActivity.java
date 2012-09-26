package org.qii.weiciyuan.ui.topic;

import android.net.Uri;
import android.os.Bundle;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;

/**
 * User: qii
 * Date: 12-9-8
 */
public class SearchTopicByNameActivity extends AbstractAppActivity implements IToken {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri data = getIntent().getData();
        String d = data.toString();
        int index = d.lastIndexOf("/");
        String newValue = d.substring(index + 1);
        getActionBar().setTitle(getString(R.string.search_topic) + " #" + newValue + "#");
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SearchTopicByNameFragment(newValue))
                .commit();
    }

    @Override
    public String getToken() {
        return GlobalContext.getInstance().getSpecialToken();
    }
}
