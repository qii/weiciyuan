package org.qii.weiciyuan.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import org.qii.weiciyuan.R;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 下午1:02
 */
public class HomeActivity extends Activity {

    String token;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.timeline);


        Intent intent = getIntent();

        token = intent.getStringExtra("token");

        String username = intent.getStringExtra("username");

        if (TextUtils.isEmpty(username))
            setTitle(username);

        ((TextView)findViewById(R.id.tvResult)).setText(token);
    }
}
