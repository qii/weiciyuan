package org.qii.weiciyuan.ui.userinfo;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.show.ShowUserDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
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
    private TextView verified_reason;
    private TextView isVerified;
    private TextView info;
    private TextView blog_url;
    private TextView location;
    private TextView relationship;
    private TextView sex;
    private TextView following_number;
    private TextView fans_number;


    private View verified_layout;
    private View intro_layout;
    private View location_layout;
    private View blog_url_layout;


    protected ICommander commander;

    private SimpleTask task;


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
        this.bean = bean;
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
            verified_reason.setText(bean.getVerified_reason());
        } else {
            verified_layout.setVisibility(View.GONE);
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

        boolean he = bean.isFollow_me();
        boolean me = bean.isFollowing();

        if (he && me) {
            relationship.setText(getString(R.string.following_together));
        } else if (he && !me) {
            relationship.setText(getString(R.string.he_is_following_you));
        } else if (!he && me) {
            relationship.setText(getString(R.string.you_is_following_he));
        } else {
            relationship.setText(getString(R.string.stranger_together));
        }


        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.weibo) + "(" + bean.getStatuses_count() + ")");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info_layout, container, false);
        avatar = (ImageView) view.findViewById(R.id.avatar);
        username = (TextView) view.findViewById(R.id.username);
        isVerified = (TextView) view.findViewById(R.id.isVerified);
        verified_reason = (TextView) view.findViewById(R.id.verified_info);
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
        verified_layout = view.findViewById(R.id.verified_layout);

        View fan_layout = view.findViewById(R.id.fan_layout);
        View following_layout = view.findViewById(R.id.following_layout);

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
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (bean.isFollowing()) {
            menu.findItem(R.id.menu_follow).setVisible(false);
            menu.findItem(R.id.menu_unfollow).setVisible(true);
        } else {
            menu.findItem(R.id.menu_follow).setVisible(true);
            menu.findItem(R.id.menu_unfollow).setVisible(false);
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
            task = new SimpleTask();
            task.execute();
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
