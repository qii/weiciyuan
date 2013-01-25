package org.qii.weiciyuan.ui.browser;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.show.ShowStatusDao;
import org.qii.weiciyuan.support.asyncdrawable.ProfileAvatarAndDetailMsgPicTask;
import org.qii.weiciyuan.support.error.ErrorCode;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

/**
 * User: qii
 * Date: 12-9-1
 */
public class BrowserWeiboMsgFragment extends AbstractAppFragment {

    private MessageBean msg;

    private BrowserWeiboMsgLayout layout;

    private UpdateMsgTask updateMsgTask;
    private GetGoogleLocationInfoTask geoTask;
    private ProfileAvatarAndDetailMsgPicTask picTask;

    private static class BrowserWeiboMsgLayout {
        TextView username;
        TextView content;
        TextView recontent;
        TextView time;
        TextView location;
        TextView source;

        MapView mapView;

        ImageView avatar;
        ImageView content_pic;
        ImageView repost_pic;

        LinearLayout repost_layout;
        LinearLayout count_layout;
        FrameLayout pic_layout;
        FrameLayout repost_pic_layout;

        TextView comment_count;
        TextView repost_count;

        ProgressBar content_pic_pb;
        ProgressBar repost_pic_pb;
    }

    public BrowserWeiboMsgFragment() {
    }


    public BrowserWeiboMsgFragment(MessageBean msg) {
        this.msg = msg;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        layout.mapView.onSaveInstanceState(outState);
        outState.putSerializable("msg", msg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            MapsInitializer.initialize(getActivity());
        } catch (GooglePlayServicesNotAvailableException impossible) {
                      /* Impossible */
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                if (Utility.isTaskStopped(updateMsgTask)) {
                    updateMsgTask = new UpdateMsgTask();
                    updateMsgTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
                break;
            case SCREEN_ROTATE:
                //nothing

                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                msg = (MessageBean) savedInstanceState.getSerializable("msg");
                break;
        }


    }

    //android has a bug,I am tired. I use another color and disable underline for link,but when I open "dont save activity" in
    //developer option,click the link to open another activity, then press back,this fragment is restored,
    //but the link color is restored to android own blue color,not my custom color,the underline appears
    //the workaround is set textview value in onresume() method
    @Override
    public void onResume() {
        super.onResume();
        buildViewData();
        layout.mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(updateMsgTask, geoTask, picTask);
        layout.avatar.setImageDrawable(null);
        layout.content_pic.setImageDrawable(null);
        layout.repost_pic.setImageDrawable(null);
    }

    class UpdateMsgTask extends MyAsyncTask<Void, Void, MessageBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

            Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh);
            iv.startAnimation(rotation);

        }

        @Override
        protected MessageBean doInBackground(Void... params) {
            try {
                return new ShowStatusDao(GlobalContext.getInstance().getSpecialToken(), msg.getId()).getMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled(MessageBean weiboMsgBean) {
            super.onCancelled(weiboMsgBean);
            if (Utility.isAllNotNull(getActivity(), this.e)) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
                if (e.getError_code() == ErrorCode.DELETED) {
                    setTextViewDeleted();
                }
            }
        }

        @Override
        protected void onPostExecute(MessageBean newValue) {
            if (newValue != null && e == null) {
                if (isStatusDeleted(newValue)) {
                    setTextViewDeleted(layout.content);
                    if (layout.recontent.getVisibility() == View.VISIBLE) {
                        setTextViewDeleted(layout.recontent);
                    }
                } else if (isRepostDeleted(newValue)) {
                    setTextViewDeleted(layout.recontent);
                } else {
                    msg = newValue;
                    buildViewData();
                    Intent intent = new Intent();
                    intent.putExtra("msg", msg);
                    getActivity().setResult(0, intent);
                }
            }
            super.onPostExecute(newValue);
        }
    }

    //sometime status is deleted
    private boolean isStatusDeleted(MessageBean newValue) {

        //status is deleted
        if ((msg != null))
            if ((msg.getUser() != null) && (newValue.getUser() == null)) {
                return true;
            }

        return false;

    }


    //sometime the ori status is deleted
    private boolean isRepostDeleted(MessageBean newValue) {

        if (msg.getRetweeted_status() != null && msg.getRetweeted_status().getUser() != null) {

            //ori status is deleted
            if (newValue.getRetweeted_status() != null && newValue.getRetweeted_status().getUser() == null) {
                return true;
            }
        }

        return false;

    }

    private void setTextViewDeleted() {
        SpannableString ss = SpannableString.valueOf(layout.content.getText());
        ss.setSpan(new StrikethroughSpan(), 0, ss.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        layout.content.setText(ss);
    }

    private void setTextViewDeleted(TextView tv) {
        SpannableString ss = SpannableString.valueOf(tv.getText());
        ss.setSpan(new StrikethroughSpan(), 0, ss.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ss);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browserweibomsgactivity_layout, container, false);
        layout = new BrowserWeiboMsgLayout();
        layout.username = (TextView) view.findViewById(R.id.username);
        layout.content = (TextView) view.findViewById(R.id.content);
        layout.recontent = (TextView) view.findViewById(R.id.repost_content);
        layout.time = (TextView) view.findViewById(R.id.time);
        layout.location = (TextView) view.findViewById(R.id.location);
        layout.source = (TextView) view.findViewById(R.id.source);
        layout.mapView = (MapView) view.findViewById(R.id.location_mv);
        if (savedInstanceState != null) {
            MessageBean msg = (MessageBean) savedInstanceState.get("msg");
            savedInstanceState.remove("msg");
            layout.mapView.onCreate(savedInstanceState);
            savedInstanceState.putSerializable("msg", msg);
        } else {
            layout.mapView.onCreate(savedInstanceState);
        }
        layout.comment_count = (TextView) view.findViewById(R.id.comment_count);
        layout.repost_count = (TextView) view.findViewById(R.id.repost_count);
        layout.count_layout = (LinearLayout) view.findViewById(R.id.count_layout);

        layout.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.isGooglePlaySafe(getActivity())) {
                    GeoBean bean = msg.getGeo();
                    Intent intent = new Intent(getActivity(), AppMapActivity.class);
                    intent.putExtra("lat", bean.getLat());
                    intent.putExtra("lon", bean.getLon());
                    if (!String.valueOf(bean.getLat() + "," + bean.getLon()).equals(layout.location.getText()))
                        intent.putExtra("locationStr", layout.location.getText());
                    startActivity(intent);
                } else {
                    GeoBean bean = msg.getGeo();
                    String geoUriString = "geo:" + bean.getLat() + "," + bean.getLon() + "?q=" + layout.location.getText();
                    Uri geoUri = Uri.parse(geoUriString);
                    Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
                    if (Utility.isIntentSafe(getActivity(), mapCall)) {
                        startActivity(mapCall);
                    }

                }
            }
        });

        layout.avatar = (ImageView) view.findViewById(R.id.avatar);
        layout.content_pic = (ImageView) view.findViewById(R.id.content_pic);
        layout.repost_pic = (ImageView) view.findViewById(R.id.repost_content_pic);

        layout.content_pic_pb = (ProgressBar) view.findViewById(R.id.content_pic_pb);
        layout.repost_pic_pb = (ProgressBar) view.findViewById(R.id.repost_content_pic_pb);

        view.findViewById(R.id.first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", msg.getUser());
                startActivity(intent);
            }
        });

        layout.content_pic.setOnClickListener(picOnClickListener);
        layout.repost_pic.setOnClickListener(picOnClickListener);

        layout.repost_layout = (LinearLayout) view.findViewById(R.id.repost_layout);
        layout.pic_layout = (FrameLayout) view.findViewById(R.id.pic_layout);

        layout.recontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //This condition will satisfy only when it is not an autolinked text
                //onClick action
                boolean isNotLink = layout.recontent.getSelectionStart() == -1 && layout.recontent.getSelectionEnd() == -1;
                boolean isDeleted = msg.getRetweeted_status() == null || msg.getRetweeted_status().getUser() == null;

                if (isNotLink && !isDeleted) {

                    Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
                    intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                    intent.putExtra("msg", msg.getRetweeted_status());
                    startActivity(intent);
                } else if (isNotLink && isDeleted) {
                    Toast.makeText(getActivity(), getString(R.string.cant_open_deleted_weibo), Toast.LENGTH_SHORT).show();
                }

            }
        });
        layout.repost_pic_layout = (FrameLayout) view.findViewById(R.id.repost_pic_layout);
        return view;
    }

    private void buildViewData() {
        if (msg.getUser() != null) {
            layout.username.setText(msg.getUser().getScreen_name());
            ((AbstractAppActivity) getActivity()).getBitmapDownloader().downloadAvatar(layout.avatar, msg.getUser());
        }
        layout.content.setText(msg.getText());
        ListViewTool.addLinks(layout.content);

        layout.time.setText(msg.getTimeInFormat());

        if (msg.getGeo() != null) {
            if (Utility.isTaskStopped(geoTask)) {
                geoTask = new GetGoogleLocationInfoTask(getActivity(), msg.getGeo(), layout.mapView, layout.location);
                geoTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if (!TextUtils.isEmpty(msg.getSource())) {
            layout.source.setText(Html.fromHtml(msg.getSource()).toString());
        }

        //sina weibo official account can send repost message with picture, fuck sina weibo
        if (!TextUtils.isEmpty(msg.getBmiddle_pic()) && msg.getRetweeted_status() == null) {
            if (Utility.isTaskStopped(picTask)) {
                picTask = new ProfileAvatarAndDetailMsgPicTask(layout.content_pic, FileLocationMethod.picture_bmiddle, layout.content_pic_pb);
                picTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, msg.getBmiddle_pic());
            }
        } else {
            layout.content_pic.setVisibility(View.GONE);
            layout.content_pic_pb.setVisibility(View.GONE);
            layout.pic_layout.setVisibility(View.GONE);
        }

        MessageBean repostMsg = msg.getRetweeted_status();
        if (repostMsg != null) {
            //sina weibo official account can send repost message with picture, fuck sina weibo
            layout.content_pic.setVisibility(View.GONE);
            layout.content_pic_pb.setVisibility(View.GONE);
            layout.pic_layout.setVisibility(View.GONE);

            layout.repost_layout.setVisibility(View.VISIBLE);
            layout.recontent.setVisibility(View.VISIBLE);
            if (repostMsg.getUser() != null) {
                layout.recontent.setText("@" + repostMsg.getUser().getScreen_name() + "：" + repostMsg.getText());
                ListViewTool.addLinks(layout.recontent);
                buildRepostCount();
            } else {
                layout.recontent.setText(repostMsg.getText());
                ListViewTool.addLinks(layout.recontent);

            }
            if (!TextUtils.isEmpty(repostMsg.getBmiddle_pic())) {
                layout.repost_pic_layout.setVisibility(View.VISIBLE);
                layout.repost_pic.setVisibility(View.VISIBLE);
                if (Utility.isTaskStopped(picTask)) {
                    picTask = new ProfileAvatarAndDetailMsgPicTask(layout.repost_pic, FileLocationMethod.picture_bmiddle, layout.repost_pic_pb);
                    picTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, msg.getRetweeted_status().getBmiddle_pic());
                }
            }
        } else {
            layout.repost_layout.setVisibility(View.GONE);
        }


        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.comments) + "(" + msg.getComments_count() + ")");
        getActivity().getActionBar().getTabAt(2).setText(getString(R.string.repost) + "(" + msg.getReposts_count() + ")");

    }

    private void buildRepostCount() {
        MessageBean repostBean = msg.getRetweeted_status();

        if (repostBean.getComments_count() == 0 && repostBean.getReposts_count() == 0) {
            layout.count_layout.setVisibility(View.GONE);
            return;
        } else {
            layout.count_layout.setVisibility(View.VISIBLE);
        }

        if (repostBean.getComments_count() > 0) {
            layout.comment_count.setVisibility(View.VISIBLE);
            layout.comment_count.setText(String.valueOf(repostBean.getComments_count()));
        } else {
            layout.comment_count.setVisibility(View.GONE);
        }

        if (repostBean.getReposts_count() > 0) {
            layout.repost_count.setVisibility(View.VISIBLE);
            layout.repost_count.setText(String.valueOf(repostBean.getReposts_count()));
        } else {
            layout.repost_count.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                if (Utility.isTaskStopped(updateMsgTask)) {
                    updateMsgTask = new UpdateMsgTask();
                    updateMsgTask.execute();
                }
                break;

        }
        return true;
    }

    private View.OnClickListener picOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = "";
            String oriUrl = "";
            switch (v.getId()) {
                case R.id.content_pic:
                    url = msg.getBmiddle_pic();
                    oriUrl = msg.getOriginal_pic();
                    break;
                case R.id.repost_content_pic:
                    url = msg.getRetweeted_status().getBmiddle_pic();
                    oriUrl = msg.getRetweeted_status().getOriginal_pic();
                    break;
            }
            if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(oriUrl)) {
                Intent intent = new Intent(getActivity(), BrowserBigPicActivity.class);
                intent.putExtra("url", url);
                intent.putExtra("oriUrl", oriUrl);
                startActivity(intent);
            }
        }


    };
}
