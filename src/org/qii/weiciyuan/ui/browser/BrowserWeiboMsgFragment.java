package org.qii.weiciyuan.ui.browser;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.StrikethroughSpan;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.location.LocationInfoDao;
import org.qii.weiciyuan.dao.show.ShowStatusDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.util.List;

/**
 * User: qii
 * Date: 12-9-1
 */
public class BrowserWeiboMsgFragment extends Fragment {

    private MessageBean msg;

    private TextView username;
    private TextView content;
    private TextView recontent;
    private TextView time;
    private TextView location;
    private TextView source;

    private ImageView avatar;
    private ImageView content_pic;
    private ImageView repost_pic;

    private UpdateMsgTask task = null;

    public BrowserWeiboMsgFragment() {
    }


    public BrowserWeiboMsgFragment(MessageBean msg) {
        this.msg = msg;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        buildViewData();
        setHasOptionsMenu(true);
        setRetainInstance(true);
        task = new UpdateMsgTask();
        task.execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (task != null) {
            task.cancel(true);
        }
    }

    class UpdateMsgTask extends AsyncTask<Void, Void, MessageBean> {
        WeiboException e;

        @Override
        protected MessageBean doInBackground(Void... params) {
            try {
                return new ShowStatusDao(((IToken) getActivity()).getToken(), msg.getId()).getMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled(MessageBean weiboMsgBean) {
//            Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            setTextViewDeleted();
            super.onCancelled(weiboMsgBean);
        }

        @Override
        protected void onPostExecute(MessageBean newValue) {
            if (newValue != null && e == null) {
                msg = newValue;
                buildViewData();
                getActivity().invalidateOptionsMenu();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browserweibomsgactivity_layout, container, false);

        username = (TextView) view.findViewById(R.id.username);
        content = (TextView) view.findViewById(R.id.content);
        recontent = (TextView) view.findViewById(R.id.repost_content);
        time = (TextView) view.findViewById(R.id.time);
        location = (TextView) view.findViewById(R.id.location);
        source = (TextView) view.findViewById(R.id.source);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoBean bean = msg.getGeo();
                String geoUriString = "geo:" + bean.getLat() + "," + bean.getLon() + "?q=" + location.getText();
                Uri geoUri = Uri.parse(geoUriString);
                Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
                PackageManager packageManager = getActivity().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(mapCall, 0);
                boolean isIntentSafe = activities.size() > 0;
                if (isIntentSafe) {
                    startActivity(mapCall);
                }


            }
        });

        avatar = (ImageView) view.findViewById(R.id.avatar);
        content_pic = (ImageView) view.findViewById(R.id.content_pic);
        repost_pic = (ImageView) view.findViewById(R.id.repost_content_pic);

        view.findViewById(R.id.first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", msg.getUser());
                startActivity(intent);
            }
        });

//        content_pic.setOnClickListener(picOnClickListener);
//        repost_pic.setOnClickListener(picOnClickListener);

        //        LinearLayout repost_layout = (LinearLayout) findViewById(R.id.repost_layout);
        recontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (recontent.getSelectionStart() == -1 && recontent.getSelectionEnd() == -1) {
                    //This condition will satisfy only when it is not an autolinked text
                    //onClick action

                    Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
                    intent.putExtra("token", ((IToken) getActivity()).getToken());
                    intent.putExtra("msg", msg.getRetweeted_status());
                    startActivity(intent);
                }

            }
        });
        return view;
    }

    private void buildViewData() {
        if (msg.getUser() != null) {
            username.setText(msg.getUser().getScreen_name());
            SimpleBitmapWorkerTask avatarTask = new SimpleBitmapWorkerTask(avatar);
            avatarTask.execute(msg.getUser().getProfile_image_url());
        }
        content.setText(msg.getText());
        ListViewTool.addLinks(content);

        time.setText(msg.getCreated_at());

        if (msg.getGeo() != null) {
            new GetGoogleLocationInfo(msg.getGeo()).execute();
        }

        source.setText(Html.fromHtml(msg.getSource()));
        source.setMovementMethod(LinkMovementMethod.getInstance());


        if (msg.getRetweeted_status() != null) {
            recontent.setVisibility(View.VISIBLE);
            if (msg.getRetweeted_status().getUser() != null) {
                recontent.setText("@" + msg.getRetweeted_status().getUser().getScreen_name() + "：" + msg.getRetweeted_status().getText());
                ListViewTool.addLinks(recontent);

            } else {
                recontent.setText(msg.getRetweeted_status().getText());

            }
            if (!TextUtils.isEmpty(msg.getRetweeted_status().getBmiddle_pic())) {
                repost_pic.setVisibility(View.VISIBLE);
                SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(repost_pic);
                task.execute(msg.getRetweeted_status().getBmiddle_pic());
            } else if (!TextUtils.isEmpty(msg.getRetweeted_status().getThumbnail_pic())) {
                repost_pic.setVisibility(View.VISIBLE);
                SimpleBitmapWorkerTask task = new SimpleBitmapWorkerTask(repost_pic);
                task.execute(msg.getRetweeted_status().getThumbnail_pic());

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
            location.setVisibility(View.VISIBLE);
            location.setText(s);
            super.onPostExecute(s);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.browserweibomsgactivity_menu, menu);


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(getActivity(), MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;

            case R.id.menu_refresh:
                if (task == null | task.getStatus() == AsyncTask.Status.FINISHED) {
                    task = new UpdateMsgTask();
                    task.execute();
                }

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
}
