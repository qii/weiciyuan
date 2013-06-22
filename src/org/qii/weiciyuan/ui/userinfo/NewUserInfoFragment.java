package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import org.qii.weiciyuan.dao.topic.UserTopicListDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.VelocityListView;
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

    private ImageView avatar;
    private TextView nickname;
    private TextView bio;
    private TextView location;
    private TextView url;
    private TextView verifiedReason;

    private ImageView leftPoint;
    private ImageView centerPoint;
    private ImageView rightPoint;

    private View progressFooter;

    private ArrayList<String> topicList;

    private TopicListTask topicListTask;


    public NewUserInfoFragment() {

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utility.cancelTasks(topicListTask);
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

        progressFooter = inflater.inflate(R.layout.newuserinfofragment_progress_footer, getListView(), false);
        progressFooter.setVisibility(View.GONE);
        getListView().addFooterView(progressFooter);

        viewPager = (ViewPager) header.findViewById(R.id.viewpager);
        friendsCount = (TextView) header.findViewById(R.id.friends_count);
        fansCount = (TextView) header.findViewById(R.id.fans_count);
        topicsCount = (TextView) header.findViewById(R.id.topics_count);
        weiboCount = (TextView) header.findViewById(R.id.weibo_count);

        headerLeft = inflater.inflate(R.layout.newuserinfofragment_header_viewpager_left_layout, null, false);
        headerRight = inflater.inflate(R.layout.newuserinfofragment_header_viewpager_right_layout, null, false);

        avatar = (ImageView) headerLeft.findViewById(R.id.avatar);
        nickname = (TextView) headerLeft.findViewById(R.id.nickname);
        location = (TextView) headerLeft.findViewById(R.id.location);


        bio = (TextView) headerRight.findViewById(R.id.bio);
        url = (TextView) headerRight.findViewById(R.id.url);

        leftPoint = (ImageView) header.findViewById(R.id.left_point);
        centerPoint = (ImageView) header.findViewById(R.id.center_point);
        rightPoint = (ImageView) header.findViewById(R.id.right_point);
        leftPoint.getDrawable().setLevel(1);

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

        friendsCount.setText(String.valueOf(userBean.getFriends_count()));
        fansCount.setText(userBean.getFollowers_count());
//        topicsCount.setText(userBean.());
        weiboCount.setText(userBean.getStatuses_count());

        nickname.setText(userBean.getScreen_name());


        ((ICommander) getActivity()).getBitmapDownloader().downloadAvatar(avatar, userBean, (AbstractTimeLineFragment) this);

//        userBean.getProfile_image_url()

        if (!TextUtils.isEmpty(userBean.getDescription())) {
            bio.setText(userBean.getDescription());
            bio.setVisibility(View.VISIBLE);
        } else {
            bio.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userBean.getLocation())) {
            location.setText(userBean.getLocation());
        }

        if (!TextUtils.isEmpty(userBean.getUrl())) {
            url.setText(userBean.getUrl());
            ListViewTool.addLinks(url);
            url.setVisibility(View.VISIBLE);
        }


        HeaderPagerAdapter adapter = new HeaderPagerAdapter();
        viewPager.setAdapter(adapter);

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
        progressFooter.setVisibility(View.GONE);
        if (getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();

        }


    }

    @Override
    protected void oldMsgOnPostExecute(MessageListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {
            getList().addOldData(newValue);
            getActivity().invalidateOptionsMenu();

        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public void loadMiddleMsg(String beginId, String endId, int position) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        dismissFooterView();

        Bundle bundle = new Bundle();
        bundle.putString("beginId", beginId);
        bundle.putString("endId", endId);
        bundle.putInt("position", position);
        VelocityListView velocityListView = (VelocityListView) getListView();
        bundle.putBoolean("towardsBottom", velocityListView.getTowardsOrientation() == VelocityListView.TOWARDS_BOTTOM);
        getLoaderManager().restartLoader(MIDDLE_MSG_LOADER_ID, bundle, msgCallback);

    }

    @Override
    public void loadNewMsg() {
        progressFooter.setVisibility(View.VISIBLE);
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_MSG_LOADER_ID, null, msgCallback);
    }


    @Override
    protected void loadOldMsg(View view) {
        getLoaderManager().destroyLoader(NEW_MSG_LOADER_ID);
        getPullToRefreshListView().onRefreshComplete();
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().restartLoader(OLD_MSG_LOADER_ID, null, msgCallback);
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

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateMiddleMsgLoader(int id, Bundle args, String middleBeginId, String middleEndId, String middleEndTag, int middlePosition) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        return new StatusesByIdLoader(getActivity(), uid, screenName, token, middleBeginId, middleEndId);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateOldMsgLoader(int id, Bundle args) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        String maxId = null;

        if (getList().getSize() > 0) {
            maxId = getList().getItemList().get(getList().getSize() - 1).getId();
        }

        return new StatusesByIdLoader(getActivity(), uid, screenName, token, null, maxId);
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
            return 2;
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
            topicsCount.setText(String.valueOf(result.size()));
        }
    }

}


