package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.ui.AbstractMainActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-1
 * Time: 上午10:48
 */
public class BrowserWeiboMsgActivity extends AbstractMainActivity {

    private WeiboMsgBean msg;
    private WeiboMsgBean retweetMsg;

    private TextView username;
    private TextView content;
    private TextView recontent;
    private TextView time;

    private ImageView avatar;
    private ImageView content_pic;
    private ImageView repost_pic;


    private String comment_sum = "";
    private String retweet_sum = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browserweibomsgactivity_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("detail");

        Intent intent = getIntent();
        msg = (WeiboMsgBean) intent.getSerializableExtra("msg");
        retweetMsg = msg.getRetweeted_status();

        buildView();
        buildViewData();


    }

    private void buildView() {
        username = (TextView) findViewById(R.id.username);
        content = (TextView) findViewById(R.id.content);
        recontent = (TextView) findViewById(R.id.repost_content);
        time = (TextView) findViewById(R.id.time);


        avatar = (ImageView) findViewById(R.id.avatar);
        content_pic = (ImageView) findViewById(R.id.content_pic);
        repost_pic = (ImageView) findViewById(R.id.repost_content_pic);
    }

    private void buildViewData() {

        username.setText(msg.getUser().getScreen_name());
        content.setText(msg.getText());
        time.setText(msg.getCreated_at());

        comment_sum = msg.getComments_count();
        retweet_sum = msg.getReposts_count();


        invalidateOptionsMenu();

        SimpleBitmapWorkerTask avatarTask = new SimpleBitmapWorkerTask(avatar);
        avatarTask.execute(msg.getUser().getProfile_image_url());

        if (retweetMsg != null) {
            recontent.setVisibility(View.VISIBLE);
            recontent.setText(retweetMsg.getUser().getScreen_name() + "：" + retweetMsg.getText());
            if (!TextUtils.isEmpty(retweetMsg.getThumbnail_pic())) {
                repost_pic.setVisibility(View.VISIBLE);
                SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(repost_pic);
                task.execute(retweetMsg.getBmiddle_pic());
            }
        } else if (!TextUtils.isEmpty(msg.getThumbnail_pic())) {
            content_pic.setVisibility(View.VISIBLE);
            SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(content_pic);
            task.execute(msg.getBmiddle_pic());
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browserweibomsgactivity_menu, menu);

        menu.getItem(0).setTitle(menu.getItem(0).getTitle() + "(" + retweet_sum + ")");
        menu.getItem(1).setTitle(menu.getItem(1).getTitle() + "(" + comment_sum + ")");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    class UpdateMsgTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            buildViewData();
            super.onPostExecute(aVoid);
        }
    }
}
