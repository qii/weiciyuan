package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.location.LocationInfoDao;
import org.qii.weiciyuan.dao.show.ShowStatusDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyLinkify;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Jiang Qi
 * Date: 12-8-1
 */
public class BrowserWeiboMsgActivity extends AbstractAppActivity {

    private MessageBean msg;
    private MessageBean retweetMsg;
    private String token;

    private TextView username;
    private TextView content;
    private TextView recontent;
    private TextView time;
    private TextView location;

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
        actionBar.setTitle(getString(R.string.weibo));

        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        msg = (MessageBean) intent.getSerializableExtra("msg");
        retweetMsg = msg.getRetweeted_status();

        buildView();
        buildViewData();
        UpdateMsgTask task = new UpdateMsgTask();
        task.execute();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void buildView() {
        username = (TextView) findViewById(R.id.username);
        content = (TextView) findViewById(R.id.content);
        recontent = (TextView) findViewById(R.id.repost_content);
        time = (TextView) findViewById(R.id.time);
        location = (TextView) findViewById(R.id.location);

        avatar = (ImageView) findViewById(R.id.avatar);
        content_pic = (ImageView) findViewById(R.id.content_pic);
        repost_pic = (ImageView) findViewById(R.id.repost_content_pic);

        findViewById(R.id.first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BrowserWeiboMsgActivity.this, UserInfoActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("user", msg.getUser());
                startActivity(intent);
            }
        });

        content_pic.setOnClickListener(picOnClickListener);
        repost_pic.setOnClickListener(picOnClickListener);

//        LinearLayout repost_layout = (LinearLayout) findViewById(R.id.repost_layout);
        recontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (recontent.getSelectionStart() == -1 && recontent.getSelectionEnd() == -1) {
                    //This condition will satisfy only when it is not an autolinked text
                    //onClick action

                    Intent intent = new Intent(BrowserWeiboMsgActivity.this, BrowserWeiboMsgActivity.class);
                    intent.putExtra("token", token);
                    intent.putExtra("msg", retweetMsg);
                    startActivity(intent);
                }

            }
        });
    }

    private View.OnClickListener picOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(BrowserWeiboMsgActivity.this, "ing", Toast.LENGTH_SHORT).show();
        }
    };


    private void buildViewData() {
        if (msg.getUser() != null) {
            username.setText(msg.getUser().getScreen_name());
            SimpleBitmapWorkerTask avatarTask = new SimpleBitmapWorkerTask(avatar);
            avatarTask.execute(msg.getUser().getProfile_image_url());
        }
        content.setText(msg.getText());
        setTextViewLink(content);

        time.setText(msg.getCreated_at());

        if (msg.getGeo() != null) {
            location.setVisibility(View.VISIBLE);
            new GetGoogleLocationInfo(msg.getGeo()).execute();
        }

        comment_sum = msg.getComments_count();
        retweet_sum = msg.getReposts_count();

        invalidateOptionsMenu();

        if (retweetMsg != null) {
            recontent.setVisibility(View.VISIBLE);
            if (retweetMsg.getUser() != null) {
                recontent.setText("@" + retweetMsg.getUser().getScreen_name() + "：" + retweetMsg.getText());
                setTextViewLink(recontent);

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

    private void setTextViewLink(TextView view) {
        MyLinkify.TransformFilter mentionFilter = new MyLinkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group(1);
            }
        };

        // Match @mentions and capture just the username portion of the text.
        Pattern pattern = Pattern.compile("@([a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+)");
        String scheme = "org.qii.weiciyuan://";
        MyLinkify.addLinks(view, pattern, scheme, null, mentionFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browserweibomsgactivity_menu, menu);

        menu.getItem(0).setTitle(menu.getItem(0).getTitle() + "(" + retweet_sum + ")");
        menu.getItem(1).setTitle(menu.getItem(1).getTitle() + "(" + comment_sum + ")");

        boolean fav = msg.isFavorited();
        if (fav) {
            menu.findItem(R.id.menu_fav).setIcon(R.drawable.fav_un_black);
        } else {
            menu.findItem(R.id.menu_fav).setIcon(R.drawable.fav_en_black);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.menu_repost:
                intent = new Intent(this, BrowserRepostAndCommentListActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("id", msg.getId());
                intent.putExtra("msg", msg);
                intent.putExtra("tabindex", 0);
                startActivity(intent);
                return true;
            case R.id.menu_comment:
                intent = new Intent(this, BrowserRepostAndCommentListActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("id", msg.getId());
                intent.putExtra("msg", msg);
                intent.putExtra("tabindex", 1);
                startActivity(intent);
                return true;
            case R.id.menu_share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg.getText());
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_to)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    class UpdateMsgTask extends AsyncTask<Void, Void, MessageBean> {
        WeiboException e;

        @Override
        protected MessageBean doInBackground(Void... params) {
            try {
                return new ShowStatusDao(token, msg.getId()).getMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled(MessageBean weiboMsgBean) {
            dealWithException(e);
            setTextViewDeleted();
            super.onCancelled(weiboMsgBean);
        }

        @Override
        protected void onPostExecute(MessageBean newValue) {
            if (newValue != null && e == null) {
                msg = newValue;
                retweetMsg = msg.getRetweeted_status();
                buildViewData();
                invalidateOptionsMenu();
            }

            super.onPostExecute(newValue);
        }
    }

    private void setTextViewDeleted() {
        SpannableString ss = new SpannableString(content.getText().toString());
        ss.setSpan(new StrikethroughSpan(), 0, ss.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        content.setText(ss);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class GetGoogleLocationInfo extends AsyncTask<Void, String, String> {

        GeoBean geoBean;

        public GetGoogleLocationInfo(GeoBean geoBean) {
            this.geoBean = geoBean;
        }

        @Override
        protected String doInBackground(Void... params) {
            return new LocationInfoDao(geoBean).getInfo();
        }

        @Override
        protected void onPostExecute(String s) {
            location.setText(s);
            super.onPostExecute(s);
        }
    }
}
