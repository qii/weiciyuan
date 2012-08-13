package org.qii.weiciyuan.ui.send;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.dao.RepostNewMsgDao;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 * Time: 下午4:00
 */
public class RepostNewActivity extends AbstractAppActivity {

    private String rePostContent;

    private String id;

    private String token;

    private EditText content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statusnewactivity_layout);

        Intent intent = getIntent();
        rePostContent = intent.getStringExtra("repost_content");

        content = ((EditText) findViewById(R.id.status_new_content));
        content.setText(rePostContent);

        token = getIntent().getStringExtra("token");
        id = getIntent().getStringExtra("id");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.statusnewactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.menu_send:

                final String content = ((EditText) findViewById(R.id.status_new_content)).getText().toString();

                if (!TextUtils.isEmpty(content)) {


                    new SimpleTask().execute();

                }
                break;
        }
        return true;
    }


    class SimpleTask extends AsyncTask<Void, Void, WeiboMsgBean> {

        @Override
        protected WeiboMsgBean doInBackground(Void... params) {
            RepostNewMsgDao dao = new RepostNewMsgDao(token, id);
            dao.setStatus(((EditText) findViewById(R.id.status_new_content)).getText().toString());
            return dao.sendNewMsg();
        }
    }
}
