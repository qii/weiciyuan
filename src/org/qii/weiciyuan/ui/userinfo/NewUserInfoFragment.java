package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.dao.show.ShowUserDao;
import org.qii.weiciyuan.dao.topic.UserTopicListDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.ListViewTool;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.interfaces.ICommander;
import org.qii.weiciyuan.ui.loader.StatusesByIdLoader;
import org.qii.weiciyuan.ui.topic.UserTopicListActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * User: qii
 * Date: 13-6-20
 */
public class NewUserInfoFragment extends AbstractMessageTimeLineFragment<MessageListBean> {


    protected UserBean userBean;
    protected String token;
    private MessageListBean bean = new MessageListBean();

    private ViewPager viewPager;
    private TextView friendsCount;
    private TextView fansCount;
    private TextView topicsCount;
    private TextView weiboCount;

    private View headerLeft;
    private View headerRight;
    private View headerThird;


    private ImageView avatar;
    private TextView nickname;
    private TextView bio;
    private TextView location;
    private TextView url;
    private TextView verifiedReason;
    private TextView followsYou;

    private ImageView leftPoint;
    private ImageView centerPoint;
    private ImageView rightPoint;

    private View progressFooter;
    private View moreFooter;

    private ArrayList<String> topicList;

    private TopicListTask topicListTask;
    private RefreshTask refreshTask;


    public NewUserInfoFragment() {

    }


    @Override
    public MessageListBean getList() {
        return bean;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        MessageBean msg = (MessageBean) data.getParcelableExtra("msg");
        if (msg != null) {
            for (int i = 0; i < getList().getSize(); i++) {
                if (msg.equals(getList().getItem(i))) {
                    getList().getItem(i).setReposts_count(msg.getReposts_count());
                    getList().getItem(i).setComments_count(msg.getComments_count());
                    break;
                }
            }
            getAdapter().notifyDataSetChanged();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        View header = inflater.inflate(R.layout.newuserinfofragment_header_layout, getListView(), false);
        getListView().addHeaderView(header);

        footerView.setVisibility(View.GONE);

        progressFooter = inflater.inflate(R.layout.newuserinfofragment_progress_footer, getListView(), false);
        progressFooter.setVisibility(View.GONE);
        getListView().addFooterView(progressFooter);

        moreFooter = inflater.inflate(R.layout.newuserinfofragment_more_footer, getListView(), false);
        moreFooter.setVisibility(View.GONE);
        getListView().addFooterView(moreFooter);

        viewPager = (ViewPager) header.findViewById(R.id.viewpager);
        friendsCount = (TextView) header.findViewById(R.id.friends_count);
        fansCount = (TextView) header.findViewById(R.id.fans_count);
        topicsCount = (TextView) header.findViewById(R.id.topics_count);
        weiboCount = (TextView) header.findViewById(R.id.weibo_count);

        headerLeft = inflater.inflate(R.layout.newuserinfofragment_header_viewpager_left_layout, null, false);
        headerRight = inflater.inflate(R.layout.newuserinfofragment_header_viewpager_right_layout, null, false);
        headerThird = inflater.inflate(R.layout.newuserinfofragment_header_viewpager_third_layout, null, false);

        avatar = (ImageView) headerLeft.findViewById(R.id.avatar);
        nickname = (TextView) headerLeft.findViewById(R.id.nickname);
        location = (TextView) headerLeft.findViewById(R.id.location);
        followsYou = (TextView) headerLeft.findViewById(R.id.follows_you);

        bio = (TextView) headerRight.findViewById(R.id.bio);
        url = (TextView) headerRight.findViewById(R.id.url);
        verifiedReason = (TextView) headerThird.findViewById(R.id.verified_reason);

        leftPoint = (ImageView) header.findViewById(R.id.left_point);
        centerPoint = (ImageView) header.findViewById(R.id.center_point);
        rightPoint = (ImageView) header.findViewById(R.id.right_point);
        leftPoint.getDrawable().setLevel(1);
        if (!userBean.isVerified()) {
            rightPoint.setVisibility(View.GONE);
        } else {
            rightPoint.setVisibility(View.VISIBLE);
        }

        View weiboCountLayout = header.findViewById(R.id.weibo_count_layout);
        View friendsCountLayout = header.findViewById(R.id.friends_count_layout);
        View fansCountLayout = header.findViewById(R.id.fans_count_layout);
        View topicCountLayout = header.findViewById(R.id.topics_count_layout);

        weiboCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserTimeLineActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", userBean);
                startActivity(intent);
            }
        });

        friendsCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendListActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", userBean);
                startActivity(intent);
            }
        });

        fansCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FanListActivity.class);
                intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
                intent.putExtra("user", userBean);
                startActivity(intent);
            }
        });

        topicCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserTopicListActivity.class);
                intent.putExtra("userBean", userBean);
                intent.putStringArrayListExtra("topicList", topicList);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getPullToRefreshListView().setMode(PullToRefreshBase.Mode.DISABLED);
        getPullToRefreshListView().setOnLastItemVisibleListener(null);
        getPullToRefreshListView().getRefreshableView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        viewPager.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        viewPager.getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                }


                return false;
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        leftPoint.getDrawable().setLevel(1);
                        centerPoint.getDrawable().setLevel(0);
                        rightPoint.getDrawable().setLevel(0);
                        break;
                    case 1:
                        leftPoint.getDrawable().setLevel(0);
                        centerPoint.getDrawable().setLevel(1);
                        rightPoint.getDrawable().setLevel(0);
                        break;
                    case 2:
                        leftPoint.getDrawable().setLevel(0);
                        centerPoint.getDrawable().setLevel(0);
                        rightPoint.getDrawable().setLevel(1);
                        break;
                }
            }
        });


        HeaderPagerAdapter adapter = new HeaderPagerAdapter();
        viewPager.setAdapter(adapter);

        setValue();


    }

    private void setValue() {
        friendsCount.setText(Utility.convertStateNumberToString(getActivity(), userBean.getFriends_count()));
        fansCount.setText(Utility.convertStateNumberToString(getActivity(), userBean.getFollowers_count()));
        weiboCount.setText(Utility.convertStateNumberToString(getActivity(), userBean.getStatuses_count()));

        TextPaint tp = nickname.getPaint();
        tp.setFakeBoldText(true);
        if (TextUtils.isEmpty(userBean.getRemark()))
            nickname.setText(userBean.getScreen_name());
        else
            nickname.setText(userBean.getScreen_name() + "(" + userBean.getRemark() + ")");


        ((ICommander) getActivity()).getBitmapDownloader().downloadAvatar(avatar, userBean, (AbstractTimeLineFragment) this);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = FileManager.getFilePathFromUrl(userBean.getAvatar_large(), FileLocationMethod.avatar_large);
                if (new File(path).exists()) {
                    UserAvatarDialog dialog = new UserAvatarDialog(path);
                    dialog.show(getFragmentManager(), "");
                }
            }
        });

        if (!TextUtils.isEmpty(userBean.getDescription())) {
            bio.setText(userBean.getDescription());
            bio.setVisibility(View.VISIBLE);
        } else {
            bio.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userBean.getLocation())) {
            location.setText(userBean.getLocation());
            location.setVisibility(View.VISIBLE);
        } else {
            location.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userBean.getUrl())) {
            url.setText(userBean.getUrl());
            ListViewTool.addLinks(url);
            url.setVisibility(View.VISIBLE);
        } else {
            url.setVisibility(View.GONE);
        }

        if (userBean.isVerified()) {
            verifiedReason.setVisibility(View.VISIBLE);
            verifiedReason.setText(userBean.getVerified_reason());
        } else {
            verifiedReason.setVisibility(View.GONE);
        }

        if (userBean.isFollow_me()) {
            followsYou.setVisibility(View.VISIBLE);
            followsYou.setText(getString(R.string.is_following_me) + "@" + GlobalContext.getInstance().getCurrentAccountName());
        } else {
            followsYou.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userBean != null
                && userBean.getId() != null
                && userBean.getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
            GlobalContext.getInstance().registerForAccountChangeListener(myProfileInfoChangeListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(refreshTask, topicListTask);
        GlobalContext.getInstance().unRegisterForAccountChangeListener(myProfileInfoChangeListener);
    }

    private GlobalContext.MyProfileInfoChangeListener myProfileInfoChangeListener = new GlobalContext.MyProfileInfoChangeListener() {
        @Override
        public void onChange(UserBean newUserBean) {
            for (MessageBean msg : getList().getItemList()) {
                msg.setUser(newUserBean);
            }
            getAdapter().notifyDataSetChanged();
        }
    };

    public NewUserInfoFragment(UserBean userBean, String token) {
        this.userBean = userBean;
        this.token = token;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("bean", getList());
        outState.putParcelable("userBean", userBean);
        outState.putString("token", token);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        switch (getCurrentState(savedInstanceState)) {
            case FIRST_TIME_START:
                loadNewMsg();
                topicListTask = new TopicListTask();
                topicListTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                refresh();
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                getList().replaceData((MessageListBean) savedInstanceState.getParcelable("bean"));
                userBean = (UserBean) savedInstanceState.getParcelable("userBean");
                token = savedInstanceState.getString("token");
                getAdapter().notifyDataSetChanged();
                refreshLayout(getList());
                break;
        }

        super.onActivityCreated(savedInstanceState);
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("msg", getList().getItem(position));
        startActivityForResult(intent, 0);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_refresh:
                getPullToRefreshListView().setRefreshing();
                loadNewMsg();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue, Bundle loaderArgs) {
        getListView().removeFooterView(progressFooter);
        if (getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();
            moreFooter.setVisibility(View.VISIBLE);

        }


    }

    @Override
    protected void oldMsgOnPostExecute(MessageListBean newValue) {

    }


    @Override
    public void loadNewMsg() {
        progressFooter.setVisibility(View.VISIBLE);
        moreFooter.setVisibility(View.GONE);
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_MSG_LOADER_ID, null, msgCallback);
    }


    @Override
    protected void loadOldMsg(View view) {
        Intent intent = new Intent(getActivity(), UserTimeLineActivity.class);
        intent.putExtra("token", GlobalContext.getInstance().getSpecialToken());
        intent.putExtra("user", userBean);
        startActivity(intent);
    }


    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateNewMsgLoader(int id, Bundle args) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        String sinceId = null;
        if (getList().getItemList().size() > 0) {
            sinceId = getList().getItemList().get(0).getId();
        }
        return new StatusesByIdLoader(getActivity(), uid, screenName, token, sinceId, null);
    }


    class HeaderPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = null;
            switch (position) {
                case 0:
                    view = headerLeft;

                    break;
                case 1:
                    view = headerRight;
                    break;
                case 2:
                    view = headerThird;
                    break;

            }
            container.addView(view, 0);
            return view;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public int getCount() {
            return userBean.isVerified() ? 3 : 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View) object;
        }
    }


    private class TopicListTask extends MyAsyncTask<Void, ArrayList<String>, ArrayList<String>> {
        WeiboException e;

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            UserTopicListDao dao = new UserTopicListDao(GlobalContext.getInstance().getSpecialToken(), userBean.getId());
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
            topicsCount.setText(Utility.convertStateNumberToString(getActivity(), String.valueOf(result.size())));
        }
    }

    //sina api has bug,so must refresh to get actual data
    public void forceReloadData(UserBean bean) {
        this.userBean = bean;
        refresh();
    }

    private void refresh() {
        if (Utility.isTaskStopped(refreshTask)) {
            refreshTask = new RefreshTask();
            refreshTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
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
                boolean haveId = !TextUtils.isEmpty(userBean.getId());
                boolean haveName = !TextUtils.isEmpty(userBean.getScreen_name());
                if (haveId) {
                    dao.setUid(userBean.getId());
                } else if (haveName) {
                    dao.setScreen_name(userBean.getScreen_name());
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
                    userBean = user;
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
            ((UserInfoActivity) getActivity()).setUser(o);
            getActivity().invalidateOptionsMenu();
            topicListTask = new TopicListTask();
            topicListTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            for (MessageBean msg : bean.getItemList()) {
                msg.setUser(o);
            }
            getAdapter().notifyDataSetChanged();
            super.onPostExecute(o);
        }

    }
}


