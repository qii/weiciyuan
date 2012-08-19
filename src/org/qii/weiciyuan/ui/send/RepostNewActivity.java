package org.qii.weiciyuan.ui.send;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.widgets.SendProgressFragment;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 */
public class RepostNewActivity extends AbstractAppActivity {

    private String id;

    private String token;

    private EditText et = null;

    private WeiboMsgBean msg;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statusnewactivity_layout);
        getActionBar().setTitle(R.string.repost);

        token = getIntent().getStringExtra("token");
        id = getIntent().getStringExtra("id");
        msg = (WeiboMsgBean) getIntent().getSerializableExtra("msg");
        getActionBar().setTitle(getString(R.string.repost));

        et = ((EditText) findViewById(R.id.status_new_content));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.repostnewactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send:
                new SimpleTask().execute();
                break;
        }
        return true;
    }


    class SimpleTask extends AsyncTask<Void, Void, WeiboMsgBean> {

        SendProgressFragment progressFragment = new SendProgressFragment();

        @Override
        protected void onPreExecute() {
            progressFragment.onCancel(new DialogInterface() {

                @Override
                public void cancel() {
                    SimpleTask.this.cancel(true);
                }

                @Override
                public void dismiss() {
                    SimpleTask.this.cancel(true);
                }
            });

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected WeiboMsgBean doInBackground(Void... params) {

            String content = et.getText().toString();
            if (TextUtils.isEmpty(content)) {
                content = getString(R.string.repost);
            }

            RepostNewMsgDao dao = new RepostNewMsgDao(token, id);
            dao.setStatus(content + "//@"+msg.getUser().getScreen_name()+":"+msg.getText());
            return dao.sendNewMsg();
        }

        @Override
        protected void onPostExecute(WeiboMsgBean s) {
            progressFragment.dismissAllowingStateLoss();
            if (s != null) {
                finish();
                Toast.makeText(RepostNewActivity.this, getString(R.string.send_successfully), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RepostNewActivity.this, getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);

        }
    }
}
