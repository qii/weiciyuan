package org.qii.weiciyuan.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.dao.HomeLineMsg;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 * Time: 下午1:02
 */
public class HomeActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.timeline);


        Intent intent = getIntent();

        String token = intent.getStringExtra("token");
        String expires = intent.getStringExtra("expires");

        String username = intent.getStringExtra("username");

        if (TextUtils.isEmpty(username))
            setTitle(username);

        GlobalContext.getInstance().setToken(token);
        GlobalContext.getInstance().setExpires(expires);

        ((TextView) findViewById(R.id.tvResult)).setText(token);


        new AsyncTask<Void, String, String>() {


            @Override
            protected String doInBackground(Void... params) {

                return HomeLineMsg.getMsgstr();

            }

            @Override
            protected void onPostExecute(String o) {
                Log.e("dddd", "1" + o);
                ((TextView) findViewById(R.id.tvResult)).setText(o);
                super.onPostExecute(o);
            }
        }.execute();
    }
}
