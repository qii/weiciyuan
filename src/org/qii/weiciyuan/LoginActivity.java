package org.qii.weiciyuan;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import org.qii.weiciyuan.ui.HomeActivity;

public class LoginActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String account = settings.getString("username", "");
        if (!TextUtils.isEmpty(account)) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }



    }
}
