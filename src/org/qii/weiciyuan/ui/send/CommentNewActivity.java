package org.qii.weiciyuan.ui.send;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentBean;
import org.qii.weiciyuan.dao.CommentNewMsgDao;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 * Time: 下午4:00
 */
public class CommentNewActivity extends AbstractAppActivity {


    private String id;

    private String token;

    private EditText content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statusnewactivity_layout);
        content = ((EditText) findViewById(R.id.status_new_content));

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


    class SimpleTask extends AsyncTask<Void, Void, CommentBean> {

        @Override
        protected CommentBean doInBackground(Void... params) {
            CommentNewMsgDao dao = new CommentNewMsgDao(token, id, ((EditText) findViewById(R.id.status_new_content)).getText().toString());
            return dao.sendNewMsg();
        }
    }
}
