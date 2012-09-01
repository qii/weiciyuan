package org.qii.weiciyuan.ui.login;

import android.content.Intent;
import android.os.Bundle;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;

/**
 * User: qii
 * Date: 12-9-1
 */
public class DummyActivity extends AbstractAppActivity {

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!GlobalContext.getInstance().startedApp) {
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
    }

    finish();
} }