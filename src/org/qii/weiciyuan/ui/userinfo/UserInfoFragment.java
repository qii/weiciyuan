package org.qii.weiciyuan.ui.userinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.*;
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
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.*;
import org.qii.weiciyuan.ui.browser.SimpleBitmapWorkerTask;
import org.qii.weiciyuan.ui.send.StatusNewActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
public class UserInfoFragment extends Fragment {

    private UserBean bean;

    private ImageView avatar;
    private TextView username;
    private TextView info;
    private TextView blog_url;
    private TextView location;
    private TextView relationship;
    private Button following_number;
    private Button fans_number;

    private Button follow_it;
    private Button unfollow_it;

    protected ICommander commander;

    private SimpleTask task;

    private volatile boolean isBusying = false;


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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
        if (task == null || task.getStatus() == MyAsyncTask.Status.FINISHED) {
            task = new SimpleTask();
            task.execute();
        }
    }


    private void setValue() {
        getActivity().getActionBar().setTitle(bean.getScreen_name());
        username.setText(bean.getScreen_name());
        info.setText(bean.getDescription());

        String avatarUrl = bean.getAvatar_large();
        if (!TextUtils.isEmpty(avatarUrl)) {
            new SimpleBitmapWorkerTask(avatar).execute(avatarUrl);
        }
        if (!TextUtils.isEmpty(bean.getUrl())) {

            blog_url.setText(bean.getUrl());
        } else {
            blog_url.setVisibility(View.GONE);
        }
        location.setText(bean.getLocation());
        setTextViewNum(fans_number, bean.getFollowers_count());
        setTextViewNum(following_number, bean.getFriends_count());
        if (bean.isFollow_me()) {
            relationship.setText(getString(R.string.he_is_following_you));
        } else {
            relationship.setText(getString(R.string.he_is_not_following_you));
        }

        if (bean.isFollowing()) {
            follow_it.setVisibility(View.GONE);
            unfollow_it.setVisibility(View.VISIBLE);
        } else {
            unfollow_it.setVisibility(View.GONE);
            follow_it.setVisibility(View.VISIBLE);
        }

        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.weibo) + "(" + bean.getStatuses_count() + ")");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info_layout, container, false);
        avatar = (ImageView) view.findViewById(R.id.avatar);
        username = (TextView) view.findViewById(R.id.username);
        info = (TextView) view.findViewById(R.id.textView_info);
        blog_url = (TextView) view.findViewById(R.id.blog_url);
        location = (TextView) view.findViewById(R.id.location);
        relationship = (TextView) view.findViewById(R.id.relationship);
        following_number = (Button) view.findViewById(R.id.following_number);
        fans_number = (Button) view.findViewById(R.id.fans_number);

        follow_it = (Button) view.findViewById(R.id.follow);
        unfollow_it = (Button) view.findViewById(R.id.unfollow);

        follow_it.setOnClickListener(onClickListener);
        unfollow_it.setOnClickListener(onClickListener);


        following_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendListActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("user", bean);
                startActivity(intent);
            }
        });
        fans_number.setOnClickListener(new View.OnClickListener() {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.infofragment_menu, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if (task == null || task.getStatus() == MyAsyncTask.Status.FINISHED) {
                    task = new SimpleTask();
                    task.execute();
                }
                break;
            case R.id.menu_at:
                Intent intent = new Intent(getActivity(), StatusNewActivity.class);
                intent.putExtra("token", ((IToken) getActivity()).getToken());
                intent.putExtra("content", "@" + bean.getScreen_name());
                intent.putExtra("accountName", GlobalContext.getInstance().getCurrentAccountName());
                startActivity(intent);
                break;
        }
        return true;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.follow:

                    if (!isBusying) {
                        new FollowTask().execute();
                    }
                    break;
                case R.id.unfollow:

                    if (!isBusying) {
                        new UnFollowTask().execute();
                    }
                    break;
            }
        }
    };


    private class FollowTask extends AsyncTask<Void, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected UserBean doInBackground(Void... params) {
            if (isBusying)
                return null;
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
            isBusying = false;
            if (e != null) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                switch (e.getError_code()) {
                    case ErrorCode.ALREADY_FOLLOWED:
                        follow_it.setVisibility(View.GONE);
                        unfollow_it.setVisibility(View.VISIBLE);
                        break;
                }

            }
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            bean = o;
            isBusying = false;
            setValue();
        }
    }

    private class UnFollowTask extends AsyncTask<Void, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected UserBean doInBackground(Void... params) {
            if (isBusying)
                return null;
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
            isBusying = false;
        }

        @Override
        protected void onPostExecute(UserBean o) {
            super.onPostExecute(o);
            bean = o;
            isBusying = false;
            setValue();
        }
    }


    private class SimpleTask extends MyAsyncTask<Object, UserBean, UserBean> {
        WeiboException e;

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
            if (e != null) {
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

        String name = tv.getText().toString();

        String value = "(" + num + ")";
        if (!name.endsWith(")")) {
            tv.setText(name + value);
        } else {
            int index = name.indexOf("(");
            String newName = name.substring(0, index);
            tv.setText(newName + value);
        }

    }
}
