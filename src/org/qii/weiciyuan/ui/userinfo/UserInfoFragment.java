package org.qii.weiciyuan.ui.userinfo;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.relationship.FriendshipsDao;
import org.qii.weiciyuan.dao.show.ShowUserDao;
import org.qii.weiciyuan.support.error.ErrorCode;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.ICommander;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.browser.SimpleBitmapWorkerTask;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
public class UserInfoFragment extends Fragment {

    private UserBean bean;

    private ImageView avatar;
    private TextView username;
    private TextView isVerified;
    private TextView info;
    private TextView blog_url;
    private TextView location;
    private TextView relationship;
    private TextView sex;
    private TextView following_number;
    private TextView fans_number;


    private View intro_layout;
    private View location_layout;
    private View blog_url_layout;

    private Button unfollow_it;

    protected ICommander commander;

    private SimpleTask task;

    private MyAsyncTask<Void, UserBean, UserBean> followOrUnfollowTask;


    public UserInfoFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (task != null)
            task.cancel(true);

        if (followOrUnfollowTask != null)
            followOrUnfollowTask.cancel(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bean = ((IUserInfo) getActivity()).getUser();
        commander = ((AbstractAppActivity) getActivity()).getCommander();
        setValue();
        refresh();
    }

    //sina api has bug,so must refresh to get actual data
    public void forceReloadData(UserBean bean) {
//        this.bean=bean;
        refresh();
    }

    private void setValue() {
        getActivity().getActionBar().setTitle(bean.getScreen_name());
        if (TextUtils.isEmpty(bean.getRemark())) {
            username.setText(bean.getScreen_name());
        } else {
            username.setText(bean.getScreen_name() + "(" + bean.getRemark() + ")");
        }

        if (bean.isVerified()) {
            isVerified.setVisibility(View.VISIBLE);
            isVerified.setText(getString(R.string.verified_user));
        }

        if (!TextUtils.isEmpty(bean.getDescription())) {
            info.setText(bean.getDescription());
        } else {
            intro_layout.setVisibility(View.GONE);
        }

        String avatarUrl = bean.getAvatar_large();
        if (!TextUtils.isEmpty(avatarUrl)) {
            new SimpleBitmapWorkerTask(avatar, FileLocationMethod.avatar_large).execute(avatarUrl);
        }
        if (!TextUtils.isEmpty(bean.getUrl())) {

            blog_url.setText(bean.getUrl());
            ListViewTool.addLinks(blog_url);
        } else {
            blog_url_layout.setVisibility(View.GONE);
            blog_url.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bean.getLocation())) {
            location.setText(bean.getLocation());
        } else {
            location_layout.setVisibility(View.GONE);
        }
        String s = bean.getGender();
        if (!TextUtils.isEmpty(s)) {
            if (s.equals("m"))
                sex.setText(getString(R.string.m));
            else if (s.equals("f"))
                sex.setText(getString(R.string.f));
            else
                sex.setVisibility(View.GONE);
        }
        setTextViewNum(fans_number, bean.getFollowers_count());
        setTextViewNum(following_number, bean.getFriends_count());
        if (bean.isFollow_me()) {
            relationship.setText(getString(R.string.he_is_following_you));
        } else {
            relationship.setText(getString(R.string.he_is_not_following_you));
        }


        if (bean.isFollowing()) {
            unfollow_it.setText(getString(R.string.unfollow_he));
        } else {
            unfollow_it.setText(getString(R.string.follow_he));

        }
        unfollow_it.setVisibility(View.VISIBLE);

        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.weibo) + "(" + bean.getStatuses_count() + ")");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info_layout, container, false);
        avatar = (ImageView) view.findViewById(R.id.avatar);
        username = (TextView) view.findViewById(R.id.username);
        isVerified = (TextView) view.findViewById(R.id.isVerified);
        info = (TextView) view.findViewById(R.id.textView_info);
        blog_url = (TextView) view.findViewById(R.id.blog_url);
        location = (TextView) view.findViewById(R.id.location);
        sex = (TextView) view.findViewById(R.id.sex);
        relationship = (TextView) view.findViewById(R.id.relationship);
        following_number = (TextView) view.findViewById(R.id.following_number);
        fans_number = (TextView) view.findViewById(R.id.fans_number);
        blog_url_layout = view.findViewById(R.id.blog_url_layout);
        intro_layout = view.findViewById(R.id.intro_layout);
        location_layout = view.findViewById(R.id.location_layout);

        View fan_layout = view.findViewById(R.id.fan_layout);
        View following_layout = view.findViewById(R.id.following_layout);

        unfollow_it = (Button) view.findViewById(R.id.unfollow);

        unfollow_it.setOnClickListener(onClickListener);


        following_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendListActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });
        fan_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FanListActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });
        return view;
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
            task = new SimpleTask();
            task.execute();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            boolean a = followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED;
            boolean b = followOrUnfollowTask == null || followOrUnfollowTask.getStatus() == MyAsyncTask.Status.FINISHED;

            if (a && b) {
                if (bean.isFollowing()) {
                    followOrUnfollowTask = new UnFollowTask();
                    followOrUnfollowTask.execute();

                } else {
                    followOrUnfollowTask = new FollowTask();
                    followOrUnfollowTask.execute();

                }

            }
        }
    };


    private class FollowTask extends MyAsyncTask<Void, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FriendshipsDao dao = new FriendshipsDao(((IToken) getActivity()).getToken());
            if (!TextUtils.isEmpty(bean.getId())) {
                dao.setUid(bean.getId());
            } else {
                dao.setScreen_name(bean.getScreen_name());
            }
            try {
                return dao.followIt();
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (getActivity() != null)
                if (e != null && (getActivity() != null)) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    switch (e.getError_code()) {
                        case ErrorCode.ALREADY_FOLLOWED:
                            unfollow_it.setVisibility(View.VISIBLE);
                            break;
                    }

                }
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            bean = o;
            setValue();
            refresh();
        }
    }

    private class UnFollowTask extends MyAsyncTask<Void, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Void... params) {

            FriendshipsDao dao = new FriendshipsDao(((IToken) getActivity()).getToken());
            if (!TextUtils.isEmpty(bean.getId())) {
                dao.setUid(bean.getId());
            } else {
                dao.setScreen_name(bean.getScreen_name());
            }

            try {
                return dao.unFollowIt();
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            bean = o;
            setValue();
        }
    }


    private class SimpleTask extends MyAsyncTask<Object, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UserBean doInBackground(Object... params) {
            if (!isCancelled()) {
                ShowUserDao dao = new ShowUserDao(((IToken) getActivity()).getToken());
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
            if (e != null && getActivity() != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(UserBean o) {
            setValue();
            super.onPostExecute(o);
        }

    }

    private void setTextViewNum(TextView tv, String num) {
        if (TextUtils.isEmpty(num)) {
            return;
        }

//        String name = tv.getText().toString();
//
//        String value = "(" + num + ")";
//        if (!name.endsWith(")")) {
//            tv.setText(name + value);
//        } else {
//            int index = name.indexOf("(");
//            String newName = name.substring(0, index);
//            tv.setText(newName + value);
//        }
        tv.setText(num);

    }
}
