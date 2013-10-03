//package org.qii.weiciyuan.ui.userinfo;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.*;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//import org.qii.weiciyuan.R;
//import org.qii.weiciyuan.bean.AccountBean;
//import org.qii.weiciyuan.bean.UserBean;
//import org.qii.weiciyuan.dao.show.ShowUserDao;
//import org.qii.weiciyuan.dao.topic.UserTopicListDao;
//import org.qii.weiciyuan.support.asyncdrawable.ProfileAvatarReadWorker;
//import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
//import org.qii.weiciyuan.support.database.AccountDBTask;
//import org.qii.weiciyuan.support.error.WeiboException;
//import org.qii.weiciyuan.support.file.FileLocationMethod;
//import org.qii.weiciyuan.support.file.FileManager;
//import org.qii.weiciyuan.support.lib.MyAsyncTask;
//import org.qii.weiciyuan.support.utils.GlobalContext;
//import org.qii.weiciyuan.support.utils.TimeLineUtility;
//import org.qii.weiciyuan.support.utils.Utility;
//import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
//import org.qii.weiciyuan.ui.interfaces.AbstractAppFragment;
//import org.qii.weiciyuan.ui.topic.UserTopicListActivity;
//
//import java.io.File;
//import java.util.ArrayList;
//
///**
// * User: qii
// * Date: 12-7-30
// */
//public class MyInfoFragment extends AbstractAppFragment {
//
//    private UserBean bean;
//
//    private Layout layout;
//
//    protected TimeLineBitmapDownloader commander;
//
//    private MyAsyncTask<Object, UserBean, UserBean> refreshTask;
//    private ProfileAvatarReadWorker avatarTask;
//    private TopicListTask topicListTask;
//
//    private ArrayList<String> topicList;
//
//
//    public MyInfoFragment() {
//        super();
//    }
//
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putStringArrayList("topicList", topicList);
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
//        setRetainInstance(true);
//    }
//
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        AccountBean accountBean;
//        switch (getCurrentState(savedInstanceState)) {
//            case FIRST_TIME_START:
//                accountBean = GlobalContext.getInstance().getAccountBean();
//                bean = accountBean.getInfo();
//                refresh();
//                break;
//            case SCREEN_ROTATE:
//                //nothing
//
//                break;
//            case ACTIVITY_DESTROY_AND_CREATE:
//                topicList = savedInstanceState.getStringArrayList("topicList");
//                accountBean = GlobalContext.getInstance().getAccountBean();
//                bean = accountBean.getInfo();
//                break;
//        }
//
//        commander = ((AbstractAppActivity) getActivity()).getBitmapDownloader();
//        setValue();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        GlobalContext.getInstance().registerForAccountChangeListener(listener);
//    }
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Utility.cancelTasks(refreshTask, avatarTask, topicListTask);
//        GlobalContext.getInstance().unRegisterForAccountChangeListener(listener);
//
//    }
//
//
//    private GlobalContext.MyProfileInfoChangeListener listener = new GlobalContext.MyProfileInfoChangeListener() {
//        @Override
//        public void onChange(UserBean newUserBean) {
//            bean = newUserBean;
//            setValue();
//        }
//    };
//
//    @Override
//    public void onStart() {
//        super.onStart();
//    }
//
//    private void setValue() {
//        layout.username.setText(bean.getScreen_name());
//
//        if (bean.isVerified()) {
//            layout.isVerified.setVisibility(View.VISIBLE);
//            layout.isVerified.setText(getString(R.string.verified_user));
//            layout.verified_reason.setText(bean.getVerified_reason());
//            layout.verified_layout.setVisibility(View.VISIBLE);
//        } else {
//            layout.verified_layout.setVisibility(View.GONE);
//        }
//
//        if (!TextUtils.isEmpty(bean.getDescription())) {
//            layout.intro_layout.setVisibility(View.VISIBLE);
//            layout.info.setText(bean.getDescription());
//        } else {
//            layout.intro_layout.setVisibility(View.GONE);
//        }
//
//        //sina weibo have a bug, after modify your profile, the return UserBean object don't have large avatar url
//        String avatarUrl = bean.getAvatar_large();
//        if (!TextUtils.isEmpty(avatarUrl)) {
//            avatarTask = new ProfileAvatarReadWorker(layout.avatar, avatarUrl);
//            avatarTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
//        }
//        if (!TextUtils.isEmpty(bean.getUrl())) {
//            layout.blog_url_layout.setVisibility(View.VISIBLE);
//            layout.blog_url.setVisibility(View.VISIBLE);
//            layout.blog_url.setText(bean.getUrl());
//            TimeLineUtility.addLinks(layout.blog_url);
//        } else {
//            layout.blog_url_layout.setVisibility(View.GONE);
//            layout.blog_url.setVisibility(View.GONE);
//        }
//
//        if (!TextUtils.isEmpty(bean.getLocation())) {
//            layout.location_layout.setVisibility(View.VISIBLE);
//            layout.location.setText(bean.getLocation());
//        } else {
//            layout.location_layout.setVisibility(View.GONE);
//        }
//        String s = bean.getGender();
//        if (!TextUtils.isEmpty(s)) {
//            if (s.equals("m"))
//                layout.sex.setText(getString(R.string.m));
//            else if (s.equals("f"))
//                layout.sex.setText(getString(R.string.f));
//            else
//                layout.sex.setVisibility(View.GONE);
//        }
//
//        setTextViewNum(layout.fans_number, bean.getFollowers_count());
//        setTextViewNum(layout.following_number, bean.getFriends_count());
//        setTextViewNum(layout.fav_number, bean.getFavourites_count());
//        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.weibo) + "(" + bean.getStatuses_count() + ")");
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.myinfofragment_layout, container, false);
//        layout = new Layout();
//        layout.avatar = (ImageView) view.findViewById(R.id.avatar);
//
//        layout.avatar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String path = FileManager.getFilePathFromUrl(bean.getAvatar_large(), FileLocationMethod.avatar_large);
//                if (new File(path).exists()) {
//                    UserAvatarDialog dialog = new UserAvatarDialog(path);
//                    dialog.show(getFragmentManager(), "");
//                }
//            }
//        });
//
//        layout.username = (TextView) view.findViewById(R.id.username);
//        layout.isVerified = (TextView) view.findViewById(R.id.isVerified);
//        layout.verified_reason = (TextView) view.findViewById(R.id.verified_info);
//        layout.info = (TextView) view.findViewById(R.id.textView_info);
//        layout.blog_url = (TextView) view.findViewById(R.id.blog_url);
//        layout.location = (TextView) view.findViewById(R.id.location);
//        layout.sex = (TextView) view.findViewById(R.id.sex);
//        layout.following_number = (TextView) view.findViewById(R.id.following_number);
//        layout.fans_number = (TextView) view.findViewById(R.id.fans_number);
//        layout.fav_number = (TextView) view.findViewById(R.id.fav_number);
//        layout.topic_number = (TextView) view.findViewById(R.id.topic_number);
//
//        layout.blog_url_layout = (ViewGroup) view.findViewById(R.id.blog_url_layout);
//        layout.intro_layout = (ViewGroup) view.findViewById(R.id.intro_layout);
//        layout.location_layout = (ViewGroup) view.findViewById(R.id.location_layout);
//        layout.verified_layout = (ViewGroup) view.findViewById(R.id.verified_layout);
//
//        View fan_layout = view.findViewById(R.id.fan_layout);
//        View following_layout = view.findViewById(R.id.following_layout);
//        View fav_layout = view.findViewById(R.id.fav_layout);
//        View topic_layout = view.findViewById(R.id.topic_layout);
//
//        following_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), FriendListActivity.class);
//                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
//                intent.putExtra("user", bean);
//                startActivity(intent);
//            }
//        });
//        fan_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), FanListActivity.class);
//                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
//                intent.putExtra("user", bean);
//                startActivity(intent);
//            }
//        });
//        fav_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), MyFavActivity.class);
//                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
//                startActivity(intent);
//            }
//        });
//        topic_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), UserTopicListActivity.class);
//                intent.putExtra("userBean", bean);
//                intent.putStringArrayListExtra("topicList", topicList);
//                startActivity(intent);
//
//            }
//        });
//        return view;
//    }
//
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.actionbar_menu_myinfofragment, menu);
//
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_refresh:
//                refresh();
//                break;
//
//
//        }
//        return true;
//    }
//
//    private void refresh() {
//        if (refreshTask == null || refreshTask.getStatus() == MyAsyncTask.Status.FINISHED) {
//            refreshTask = new RefreshTask();
//            refreshTask.execute();
//        }
//    }
//
//    private class RefreshTask extends MyAsyncTask<Object, UserBean, UserBean> {
//        WeiboException e;
//
//        @Override
//        protected UserBean doInBackground(Object... params) {
//            UserBean user = null;
//            try {
//                ShowUserDao dao = new ShowUserDao(GlobalContext.getInstance().getSpecialToken());
//                if (!TextUtils.isEmpty(bean.getId()))
//                    dao.setUid(bean.getId());
//                else
//                    dao.setScreen_name(bean.getScreen_name());
//
//                user = dao.getUserInfo();
//            } catch (WeiboException e) {
//                this.e = e;
//                cancel(true);
//            }
//            if (user != null) {
//                bean = user;
//                AccountDBTask.updateMyProfile(GlobalContext.getInstance().getAccountBean(), bean);
//            } else {
//                cancel(true);
//            }
//            return user;
//        }
//
//        @Override
//        protected void onCancelled(UserBean userBean) {
//            super.onCancelled(userBean);
//            if (Utility.isAllNotNull(getActivity(), this.e)) {
//                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        @Override
//        protected void onPostExecute(UserBean userBean) {
//            super.onPostExecute(userBean);
//            setValue();
//            GlobalContext.getInstance().updateUserInfo(userBean);
//            topicListTask = new TopicListTask();
//            topicListTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
//        }
//    }
//
//
//    private class TopicListTask extends MyAsyncTask<Void, ArrayList<String>, ArrayList<String>> {
//        WeiboException e;
//
//        @Override
//        protected ArrayList<String> doInBackground(Void... params) {
//            UserTopicListDao dao = new UserTopicListDao(GlobalContext.getInstance().getSpecialToken(), bean.getId());
//            try {
//                return dao.getGSONMsgList();
//            } catch (WeiboException e) {
//                this.e = e;
//                cancel(true);
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<String> result) {
//            super.onPostExecute(result);
//            if (isCancelled())
//                return;
//            if (result == null || result.size() == 0) {
//                return;
//            }
//            topicList = result;
//            setTextViewNum(layout.topic_number, String.valueOf(result.size()));
//
//
//        }
//    }
//
//    private void setTextViewNum(TextView tv, String num) {
//
//        if (TextUtils.isEmpty(num)) {
//            return;
//        }
//        int number = Integer.valueOf(num);
//        String value = num;
//        if (number > 10000) {
//            value = String.valueOf((number / 10000) + getString(R.string.ten_thousand));
//        }
//        tv.setText(value);
//
//    }
//
//    private class Layout {
//        ImageView avatar;
//
//        TextView username;
//        TextView verified_reason;
//        TextView isVerified;
//        TextView info;
//        TextView blog_url;
//        TextView location;
//        TextView sex;
//
//        TextView following_number;
//        TextView fans_number;
//        TextView fav_number;
//        TextView topic_number;
//
//        ViewGroup verified_layout;
//        ViewGroup intro_layout;
//        ViewGroup location_layout;
//        ViewGroup blog_url_layout;
//    }
//}
