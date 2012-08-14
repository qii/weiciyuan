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
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.dao.send.CommentNewMsgDao;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 * Time: 下午4:00
 */
public class CommentNewActivity extends AbstractAppActivity {

    private String id;
    private String token;
    private EditText et;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statusnewactivity_layout);

        token = getIntent().getStringExtra("token");
        id = getIntent().getStringExtra("id");
        getActionBar().setTitle(getString(R.string.comments));
        et = (EditText) findViewById(R.id.status_new_content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.commentnewactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_send:

                final String content = et.getText().toString();

                if (!TextUtils.isEmpty(content)) {
                    new SimpleTask().execute();
                } else {
                    Toast.makeText(this, "comment can't be empty", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }


    class SimpleTask extends AsyncTask<Void, Void, CommentBean> {

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
        protected CommentBean doInBackground(Void... params) {
            CommentNewMsgDao dao = new CommentNewMsgDao(token, id, ((EditText) findViewById(R.id.status_new_content)).getText().toString());
            return dao.sendNewMsg();
        }

        @Override
        protected void onPostExecute(CommentBean s) {
            progressFragment.dismissAllowingStateLoss();
            if (s != null) {
                finish();
                Toast.makeText(CommentNewActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CommentNewActivity.this, "failed", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);

        }
    }
}
