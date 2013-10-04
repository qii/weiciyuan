package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.show.ShowUserDao;
import org.qii.weiciyuan.dao.topic.UserTopicListDao;
import org.qii.weiciyuan.support.asyncdrawable.ProfileAvatarReadWorker;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.TimeLineUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
import org.qii.weiciyuan.ui.interfaces.IUserInfo;
import org.qii.weiciyuan.ui.topic.UserTopicListActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
@Deprecated
public class UserInfoFragment extends AbstractAppFragment {

    private UserBean bean;


    private Layout layout;

    private RefreshTask task;
    private ProfileAvatarReadWorker avatarTask;
    private TopicListTask topicListTask;


    private ArrayList<String> topicList;

    private Handler handler = new Handler();

    public UserInfoFragment() {
        super();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("topicList", topicList);
        outState.putParcelable("bean", bean);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(task, avatarTask, topicListTask);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                bean = ((IUserInfo) getActivity()).getUser();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                }, 1000);
                break;
            case SCREEN_ROTATE:
                //nothing

                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                topicList = savedInstanceState.getStringArrayList("topicList");
                bean = (UserBean) savedInstanceState.getParcelable("bean");
                break;
        }

        setValue();

    }

    //sina api has bug,so must refresh to get actual data
    public void forceReloadData(UserBean bean) {
        this.bean = bean;
        refresh();
    }

    private void setValue() {
        getActivity().getActionBar().setTitle(bean.getScreen_name());
        if (TextUtils.isEmpty(bean.getRemark())) {
            layout.username.setText(bean.getScreen_name());
        } else {
            layout.username.setText(bean.getScreen_name() + "(" + bean.getRemark() + ")");
        }

        if (bean.isVerified()) {
            layout.isVerified.setVisibility(View.VISIBLE);
            layout.isVerified.setText(getString(R.string.verified_user));
            layout.verified_reason.setText(bean.getVerified_reason());
            layout.verified_layout.setVisibility(View.VISIBLE);
        } else {
            layout.verified_layout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bean.getDescription())) {
            layout.info.setText(bean.getDescription());
            layout.intro_layout.setVisibility(View.VISIBLE);
        } else {
            layout.intro_layout.setVisibility(View.GONE);
        }

        String avatarUrl = bean.getAvatar_large();
        if (!TextUtils.isEmpty(avatarUrl)) {
            avatarTask = new ProfileAvatarReadWorker(layout.avatar, avatarUrl);
            avatarTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (!TextUtils.isEmpty(bean.getUrl())) {

            layout.blog_url.setText(bean.getUrl());
            TimeLineUtility.addLinks(layout.blog_url);
            layout.blog_url_layout.setVisibility(View.VISIBLE);
            layout.blog_url.setVisibility(View.VISIBLE);
        } else {
            layout.blog_url_layout.setVisibility(View.GONE);
            layout.blog_url.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bean.getLocation())) {
            layout.location.setText(bean.getLocation());
            layout.location_layout.setVisibility(View.VISIBLE);
        } else {
            layout.location_layout.setVisibility(View.GONE);
        }
        String s = bean.getGender();
        if (!TextUtils.isEmpty(s)) {
            if (s.equals("m"))
                layout.sex.setText(getString(R.string.m));
            else if (s.equals("f"))
                layout.sex.setText(getString(R.string.f));
            else
                layout.sex.setVisibility(View.GONE);
        }
        setTextViewNum(layout.fans_number, bean.getFollowers_count());
        setTextViewNum(layout.following_number, bean.getFriends_count());

        boolean he = bean.isFollow_me();
        boolean me = bean.isFollowing();

        if (he && me) {
            layout.relationship.setText(getString(R.string.following_each_other));
        } else if (he && !me) {
            layout.relationship.setText(getString(R.string.he_is_following_you));
        } else if (!he && me) {
            layout.relationship.setText(getString(R.string.you_is_following_he));
        } else {
            layout.relationship.setText(getString(R.string.stranger_each_other));
        }


        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.weibo) + "(" + bean.getStatuses_count() + ")");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.userinfofragment_layout, container, false);
        layout = new Layout();
        layout.avatar = (ImageView) view.findViewById(R.id.avatar);

        layout.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = FileManager.getFilePathFromUrl(bean.getAvatar_large(), FileLocationMethod.avatar_large);
                if (new File(path).exists()) {
                    UserAvatarDialog dialog = new UserAvatarDialog(path);
                    dialog.show(getFragmentManager(), "");
                }
            }
        });

        layout.username = (TextView) view.findViewById(R.id.username);
        layout.isVerified = (TextView) view.findViewById(R.id.isVerified);
        layout.verified_reason = (TextView) view.findViewById(R.id.verified_info);
        layout.info = (TextView) view.findViewById(R.id.textView_info);
        layout.blog_url = (TextView) view.findViewById(R.id.blog_url);
        layout.location = (TextView) view.findViewById(R.id.location);
        layout.sex = (TextView) view.findViewById(R.id.sex);
        layout.relationship = (TextView) view.findViewById(R.id.relationship);
        layout.following_number = (TextView) view.findViewById(R.id.following_number);
        layout.fans_number = (TextView) view.findViewById(R.id.fans_number);
        layout.topic_number = (TextView) view.findViewById(R.id.topic_number);

        layout.blog_url_layout = (ViewGroup) view.findViewById(R.id.blog_url_layout);
        layout.intro_layout = (ViewGroup) view.findViewById(R.id.intro_layout);
        layout.location_layout = (ViewGroup) view.findViewById(R.id.location_layout);
        layout.verified_layout = (ViewGroup) view.findViewById(R.id.verified_layout);

        View fan_layout = view.findViewById(R.id.fan_layout);
        View following_layout = view.findViewById(R.id.following_layout);
        View topic_layout = view.findViewById(R.id.topic_layout);

        following_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendListActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });
        fan_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FanListActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });
        topic_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserTopicListActivity.class);
                intent.putExtra("userBean", bean);
                intent.putStringArrayListExtra("topicList", topicList);
                startActivity(intent);

            }
        });
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (bean.isFollowing()) {
            menu.findItem(R.id.menu_follow).setVisible(false);
            menu.findItem(R.id.menu_unfollow).setVisible(true);
            menu.findItem(R.id.menu_manage_group).setVisible(true);
        } else {
            menu.findItem(R.id.menu_follow).setVisible(true);
            menu.findItem(R.id.menu_unfollow).setVisible(false);
            menu.findItem(R.id.menu_manage_group).setVisible(false);
        }

        if (!bean.isFollowing() && bean.isFollow_me()) {
            menu.findItem(R.id.menu_remove_fan).setVisible(true);
        } else {
            menu.findItem(R.id.menu_remove_fan).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
        }
        return true;
    }

    private void refresh() {
        if (task == null || task.getStatus() == MyAsyncTask.Status.FINISHED) {
            task = new RefreshTask();
            task.execute();
        }
    }

    private class RefreshTask extends MyAsyncTask<Object, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Object... params) {
            if (!isCancelled()) {
                ShowUserDao dao = new ShowUserDao(GlobalContext.getInstance().getSpecialToken());
                boolean haveId = !TextUtils.isEmpty(bean.getId());
                boolean haveName = !TextUtils.isEmpty(bean.getScreen_name());
                if (haveId) {
                    dao.setUid(bean.getId());
                } else if (haveName) {
                    dao.setScreen_name(bean.getScreen_name());
                } else {
                    cancel(true);
                    return null;
                }

                UserBean user = null;
                try {
                    user = dao.getUserInfo();
                } catch (WeiboException e) {
                    this.e = e;
                    cancel(true);
                }
                if (user != null) {
                    bean = user;
                } else {
                    cancel(true);
                }
                return user;
            } else {
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (Utility.isAllNotNull(getActivity(), this.e)) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(UserBean o) {
            setValue();
            topicListTask = new TopicListTask();
            topicListTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            super.onPostExecute(o);
        }

    }

    private class TopicListTask extends MyAsyncTask<Void, ArrayList<String>, ArrayList<String>> {
        WeiboException e;

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            UserTopicListDao dao = new UserTopicListDao(GlobalContext.getInstance().getSpecialToken(), bean.getId());
            try {
                return dao.getGSONMsgList();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            if (isCancelled())
                return;
            if (result == null || result.size() == 0) {
                return;
            }
            topicList = result;
            setTextViewNum(layout.topic_number, String.valueOf(result.size()));


        }
    }


    private void setTextViewNum(TextView tv, String num) {
        if (TextUtils.isEmpty(num)) {
            return;
        }
        int number = Integer.valueOf(num);
        String value = num;
        if (number > 10000) {
            value = String.valueOf((number / 10000) + getString(R.string.ten_thousand));
        }
        tv.setText(value);

    }

    private class Layout {
        ImageView avatar;
        TextView username;
        TextView verified_reason;
        TextView isVerified;
        TextView info;
        TextView blog_url;
        TextView location;
        TextView relationship;
        TextView sex;
        TextView following_number;
        TextView fans_number;
        TextView topic_number;


        ViewGroup verified_layout;
        ViewGroup intro_layout;
        ViewGroup location_layout;
        ViewGroup blog_url_layout;
    }
}
