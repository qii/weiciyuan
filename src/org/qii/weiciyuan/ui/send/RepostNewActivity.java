package org.qii.weiciyuan.ui.send;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.GlobalContext;
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

    private MessageBean msg;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statusnewactivity_layout);

        token = getIntent().getStringExtra("token");
        id = getIntent().getStringExtra("id");
        msg = (MessageBean) getIntent().getSerializableExtra("msg");
        getActionBar().setTitle(getString(R.string.repost));
        getActionBar().setSubtitle(GlobalContext.getInstance().getCurrentAccountName());


        View title = getLayoutInflater().inflate(R.layout.statusnewactivity_title_layout, null);
        TextView contentNumber = (TextView) title.findViewById(R.id.content_number);
        getActionBar().setCustomView(title, new ActionBar.LayoutParams(Gravity.RIGHT));
        getActionBar().setDisplayShowCustomEnabled(true);

        et = ((EditText) findViewById(R.id.status_new_content));
        et.addTextChangedListener(new TextNumLimitWatcher(contentNumber, et, this));

        if (msg.getRetweeted_status() != null) {
            et.setText("//@" + msg.getUser().getScreen_name() + ": " + msg.getText());
        }

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
                if (canSend())
                    new SimpleTask().execute();
                break;
        }
        return true;
    }

    private boolean canSend() {

        boolean haveContent = !TextUtils.isEmpty(et.getText().toString());
        boolean haveToken = !TextUtils.isEmpty(token);
        boolean contentNumBelow140 = (et.getText().toString().length() < 140);

        if (haveContent && haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveContent && !haveToken) {
                Toast.makeText(this, getString(R.string.content_cant_be_empty_and_dont_have_account), Toast.LENGTH_SHORT).show();
            } else if (!haveContent) {
                et.setError(getString(R.string.content_cant_be_empty));
            } else if (!haveToken) {
                Toast.makeText(this, getString(R.string.dont_have_account), Toast.LENGTH_SHORT).show();
            }

            if (!contentNumBelow140) {
                et.setError(getString(R.string.content_words_number_too_many));
            }

        }

        return false;
    }


    class SimpleTask extends AsyncTask<Void, Void, MessageBean> {

        SendProgressFragment progressFragment = new SendProgressFragment();
        WeiboException e;

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

            progressFragment.show(getSupportFragmentManager(), "");

        }

        @Override
        protected MessageBean doInBackground(Void... params) {

            String content = et.getText().toString();
            if (TextUtils.isEmpty(content)) {
                content = getString(R.string.repost);
            }

            RepostNewMsgDao dao = new RepostNewMsgDao(token, id);
//            dao.setStatus(content + "//@" + msg.getUser().getScreen_name() + ":" + msg.getText());
            dao.setStatus(content);
            try {
                return dao.sendNewMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(MessageBean s) {
            super.onCancelled(s);
            if (this.e != null) {
                Toast.makeText(RepostNewActivity.this, e.getError(), Toast.LENGTH_SHORT).show();

            }

            if (progressFragment != null)
                progressFragment.dismissAllowingStateLoss();
        }

        @Override
        protected void onPostExecute(MessageBean s) {
            if (progressFragment.isVisible())
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
