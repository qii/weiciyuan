package org.qii.weiciyuan.ui.browser;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
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
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.dao.show.ShowStatusDao;
import org.qii.weiciyuan.support.error.ErrorCode;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.IToken;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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

    private LinearLayout repost_layout;
    private FrameLayout pic_layout;
    private FrameLayout repost_pic_layout;

    private ProgressBar content_pic_pb;
    private ProgressBar repost_pic_pb;

    private UpdateMsgTask updateMsgTask = null;
    private GetGoogleLocationInfo geoTask = null;
    private SimpleBitmapWorkerTask picTask = null;
    private SimpleBitmapWorkerTask avatarTask = null;

    public BrowserWeiboMsgFragment() {
    }


    public BrowserWeiboMsgFragment(MessageBean msg) {
        this.msg = msg;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("msg", msg);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        if (savedInstanceState != null) {
            msg = (MessageBean) savedInstanceState.getSerializable("msg");
        } else {
            updateMsgTask = new UpdateMsgTask();
            updateMsgTask.execute();

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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Utility.cancelTasks(updateMsgTask, geoTask, avatarTask, picTask);
        avatar.setImageDrawable(null);
        content_pic.setImageDrawable(null);
        repost_pic.setImageDrawable(null);
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
                return new ShowStatusDao(((IToken) getActivity()).getToken(), msg.getId()).getMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled(MessageBean weiboMsgBean) {
            super.onCancelled(weiboMsgBean);
            if (getActivity() != null) {
                if (this.e != null) {
                    Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
                    if (e.getError_code() == ErrorCode.DELETED) {
                        setTextViewDeleted();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(MessageBean newValue) {
            if (newValue != null && e == null) {
                if (isStatusDeleted(newValue)) {
                    setTextViewDeleted(content);
                    if (recontent.getVisibility() == View.VISIBLE) {
                        setTextViewDeleted(recontent);
                    }
                } else if (isRepostDeleted(newValue)) {
                    setTextViewDeleted(recontent);
                } else {
                    msg = newValue;
                    buildViewData();
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
        SpannableString ss = SpannableString.valueOf(content.getText());
        ss.setSpan(new StrikethroughSpan(), 0, ss.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        content.setText(ss);
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

        content_pic_pb = (ProgressBar) view.findViewById(R.id.content_pic_pb);
        repost_pic_pb = (ProgressBar) view.findViewById(R.id.repost_content_pic_pb);

        view.findViewById(R.id.first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", msg.getUser());
                startActivity(intent);
            }
        });

        content_pic.setOnClickListener(picOnClickListener);
        repost_pic.setOnClickListener(picOnClickListener);

        repost_layout = (LinearLayout) view.findViewById(R.id.repost_layout);
        pic_layout = (FrameLayout) view.findViewById(R.id.pic_layout);
        recontent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //This condition will satisfy only when it is not an autolinked text
                //onClick action
                boolean isNotLink = recontent.getSelectionStart() == -1 && recontent.getSelectionEnd() == -1;
                boolean isDeleted = msg.getRetweeted_status() == null || msg.getRetweeted_status().getUser() == null;

                if (isNotLink && !isDeleted) {

                    Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
                    intent.putExtra("token", ((IToken) getActivity()).getToken());
                    intent.putExtra("msg", msg.getRetweeted_status());
                    startActivity(intent);
                } else if (isNotLink && isDeleted) {
                    Toast.makeText(getActivity(), getString(R.string.cant_open_deleted_weibo), Toast.LENGTH_SHORT).show();
                }

            }
        });
        repost_pic_layout = (FrameLayout) view.findViewById(R.id.repost_pic_layout);
        return view;
    }

    private void buildViewData() {
        if (msg.getUser() != null) {
            username.setText(msg.getUser().getScreen_name());
            //50px avatar or 180px avatar
            String url;
            FileLocationMethod method;
            if (GlobalContext.getInstance().getEnableBigAvatar()) {
                url = msg.getUser().getAvatar_large();
                method = FileLocationMethod.avatar_large;
            } else {
                url = msg.getUser().getProfile_image_url();
                method = FileLocationMethod.avatar_small;
            }
            Bitmap bitmap = GlobalContext.getInstance().getAvatarCache().get(url);
            if (bitmap != null) {
                avatar.setImageBitmap(bitmap);
            } else {
                if (avatarTask == null || avatarTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    avatarTask = new SimpleBitmapWorkerTask(avatar, method);
                    avatarTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, msg.getUser().getProfile_image_url());
                }
            }
        }
        content.setText(msg.getText());
        ListViewTool.addLinks(content);

        time.setText(msg.getTimeInFormat());

        if (msg.getGeo() != null) {
            if (geoTask == null || geoTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                geoTask = new GetGoogleLocationInfo(msg.getGeo());
                geoTask.execute();
            }
        }
        if (!TextUtils.isEmpty(msg.getSource())) {
            source.setText(Html.fromHtml(msg.getSource()).toString());
        }


        if (!TextUtils.isEmpty(msg.getBmiddle_pic())) {
            if (picTask == null || picTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                picTask = new SimpleBitmapWorkerTask(content_pic, FileLocationMethod.picture_bmiddle, content_pic_pb);
                picTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, msg.getBmiddle_pic());
            }
        } else if (!TextUtils.isEmpty(msg.getThumbnail_pic())) {
            if (picTask == null || picTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                picTask = new SimpleBitmapWorkerTask(content_pic, FileLocationMethod.picture_thumbnail);
                picTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, msg.getThumbnail_pic());
            }
        } else {
            content_pic.setVisibility(View.GONE);
            content_pic_pb.setVisibility(View.GONE);
            pic_layout.setVisibility(View.GONE);
        }


        if (msg.getRetweeted_status() != null) {
            repost_layout.setVisibility(View.VISIBLE);
            recontent.setVisibility(View.VISIBLE);
            if (msg.getRetweeted_status().getUser() != null) {
                recontent.setText("@" + msg.getRetweeted_status().getUser().getScreen_name() + "：" + msg.getRetweeted_status().getText());
                ListViewTool.addLinks(recontent);

            } else {
                recontent.setText(msg.getRetweeted_status().getText());
                ListViewTool.addLinks(recontent);

            }
            if (!TextUtils.isEmpty(msg.getRetweeted_status().getBmiddle_pic())) {
                repost_pic_layout.setVisibility(View.VISIBLE);
                repost_pic.setVisibility(View.VISIBLE);
                if (picTask == null || picTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    picTask = new SimpleBitmapWorkerTask(repost_pic, FileLocationMethod.picture_bmiddle, repost_pic_pb);
                    picTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, msg.getRetweeted_status().getBmiddle_pic());
                }
            } else if (!TextUtils.isEmpty(msg.getRetweeted_status().getThumbnail_pic())) {
                repost_pic_layout.setVisibility(View.VISIBLE);
                repost_pic.setVisibility(View.VISIBLE);
                if (picTask == null || picTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    picTask = new SimpleBitmapWorkerTask(repost_pic, FileLocationMethod.picture_thumbnail);
                    picTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR, msg.getRetweeted_status().getThumbnail_pic());
                }


            }
        } else {
            repost_layout.setVisibility(View.GONE);
        }


        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.comments) + "(" + msg.getComments_count() + ")");
        getActivity().getActionBar().getTabAt(2).setText(getString(R.string.repost) + "(" + msg.getReposts_count() + ")");

    }

    private class GetGoogleLocationInfo extends MyAsyncTask<Void, String, String> {

        GeoBean geoBean;

        public GetGoogleLocationInfo(GeoBean geoBean) {
            this.geoBean = geoBean;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            location.setVisibility(View.VISIBLE);
            location.setText(String.valueOf(geoBean.getLat() + "," + geoBean.getLon()));
        }

        @Override
        protected String doInBackground(Void... params) {

            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(geoBean.getLat(), geoBean.getLon(), 1);
            } catch (IOException e) {
                cancel(true);
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                StringBuilder builder = new StringBuilder();
                int size = address.getMaxAddressLineIndex();
                for (int i = 0; i < size; i++) {
                    builder.append(address.getAddressLine(i));
                }
                return builder.toString();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            location.setVisibility(View.VISIBLE);
            location.setText(s);
            super.onPostExecute(s);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                if (updateMsgTask == null || updateMsgTask.getStatus() == MyAsyncTask.Status.FINISHED) {
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
