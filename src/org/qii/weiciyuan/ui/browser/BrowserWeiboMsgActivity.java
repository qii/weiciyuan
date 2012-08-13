package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.WeiboMsgBean;
import org.qii.weiciyuan.dao.StatusesShowMsgDao;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.send.CommentNewActivity;
import org.qii.weiciyuan.ui.send.RepostNewActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-1
 * Time: 上午10:48
 */
public class BrowserWeiboMsgActivity extends AbstractAppActivity {

    private WeiboMsgBean msg;
    private WeiboMsgBean retweetMsg;
    private String token;

    private TextView username;
    private TextView content;
    private TextView recontent;
    private TextView time;

    private ImageView avatar;
    private ImageView content_pic;
    private ImageView repost_pic;


    private String comment_sum = "";
    private String retweet_sum = "";

    private ViewPager mViewPager = null;


    boolean a = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browserweibomsgactivity_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.detail));

        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        msg = (WeiboMsgBean) intent.getSerializableExtra("msg");
        retweetMsg = msg.getRetweeted_status();

        buildView();
        buildViewData();
        buildViewPager();
        new UpdateMsgTask().execute();

    }

    private void buildView() {
        username = (TextView) findViewById(R.id.username);
        content = (TextView) findViewById(R.id.content);
        recontent = (TextView) findViewById(R.id.repost_content);
        time = (TextView) findViewById(R.id.time);


        avatar = (ImageView) findViewById(R.id.avatar);
        content_pic = (ImageView) findViewById(R.id.content_pic);
        repost_pic = (ImageView) findViewById(R.id.repost_content_pic);

        Button switchBtn = (Button) findViewById(R.id.switchbtn);

        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (a) {
                    mViewPager.setCurrentItem(0);
                    a = false;
                } else {
                    mViewPager.setCurrentItem(1);
                    a = true;
                }
            }
        });
    }

    private void buildViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(adapter);
    }

    class TimeLinePagerAdapter extends
            FragmentPagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);

            list.add(new RepostsByIdTimeLineFragment(token, msg.getId()));
            list.add(new CommentsByIdTimeLineFragment(token, msg.getId()));

        }

        @Override
        public Fragment getItem(int i) {
            return list.get(i);
        }

        @Override
        public int getCount() {
            return list.size();
        }
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
            if (retweetMsg.getUser() != null) {
                recontent.setText(retweetMsg.getUser().getScreen_name() + "：" + retweetMsg.getText());
            } else {
                recontent.setText(retweetMsg.getText());

            }
            if (!TextUtils.isEmpty(retweetMsg.getBmiddle_pic())) {
                repost_pic.setVisibility(View.VISIBLE);
                SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(repost_pic);
                task.execute(retweetMsg.getBmiddle_pic());
            } else if (!TextUtils.isEmpty(retweetMsg.getThumbnail_pic())) {
                repost_pic.setVisibility(View.VISIBLE);
                SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(repost_pic);
                task.execute(retweetMsg.getThumbnail_pic());

            }
        }


        if (!TextUtils.isEmpty(msg.getBmiddle_pic())) {
            content_pic.setVisibility(View.VISIBLE);
            SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(content_pic);
            task.execute(msg.getBmiddle_pic());
        } else if (!TextUtils.isEmpty(msg.getThumbnail_pic())) {
            content_pic.setVisibility(View.VISIBLE);
            SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(content_pic);
            task.execute(msg.getThumbnail_pic());

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
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_repost:
                intent = new Intent(this, RepostNewActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("id", msg.getId());
                intent.putExtra("repost_content", msg.getText());
                startActivity(intent);
                return true;
            case R.id.menu_comment:
                intent = new Intent(this, CommentNewActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("id", msg.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    class UpdateMsgTask extends AsyncTask<Void, Void, WeiboMsgBean> {

        @Override
        protected WeiboMsgBean doInBackground(Void... params) {
            return new StatusesShowMsgDao(token, msg.getId()).getMsg();
        }

        @Override
        protected void onPostExecute(WeiboMsgBean newValue) {
            if (newValue != null) {
                msg = newValue;
                retweetMsg = msg.getRetweeted_status();
                buildViewData();
            }

            super.onPostExecute(newValue);
        }
    }
}
