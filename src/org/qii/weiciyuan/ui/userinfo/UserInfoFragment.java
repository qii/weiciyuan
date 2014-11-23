package org.qii.weiciyuan.ui.userinfo;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.bean.android.MyStatusTimeLineData;
import org.qii.weiciyuan.bean.android.TimeLinePosition;
import org.qii.weiciyuan.dao.show.ShowUserDao;
import org.qii.weiciyuan.dao.topic.UserTopicListDao;
import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.database.MyStatusDBTask;
import org.qii.weiciyuan.support.database.TopicDBTask;
import org.qii.weiciyuan.support.debug.AppLogger;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.BlurImageView;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.lib.SwipeFrameLayout;
import org.qii.weiciyuan.support.lib.TimeLineAvatarImageView;
import org.qii.weiciyuan.support.lib.pulltorefresh.PullToRefreshBase;
import org.qii.weiciyuan.support.utils.AnimationUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.TimeLineUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.support.utils.ViewUtility;
import org.qii.weiciyuan.ui.basefragment.AbstractMessageTimeLineFragment;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.loader.StatusesByIdLoader;
import org.qii.weiciyuan.ui.main.LeftMenuFragment;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.topic.UserTopicListActivity;

import android.animation.Animator;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: qii
 * Date: 13-6-20
 */
public class UserInfoFragment extends AbstractMessageTimeLineFragment<MessageListBean>
        implements MainTimeLineActivity.ScrollableListFragment, Animator.AnimatorListener {

    private static final String LIMITED_READ_MESSAGE_COUNT = "10";

    protected UserBean userBean;
    protected String token;
    private MessageListBean bean = new MessageListBean();

    private ViewPager viewPager;
    private ImageView cover;
    private BlurImageView blur;

    private TextView friendsCount;
    private TextView fansCount;
    private TextView topicsCount;
    private TextView weiboCount;

    public View header;
    private View headerFirst;
    private View headerSecond;
    private View headerThird;

    private TimeLineAvatarImageView avatar;
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

    private MenuItem refreshItem;

    private ArrayList<String> topicList;

    private TopicListTask topicListTask;
    private RefreshTask refreshTask;
    private DBCacheTask dbTask;

    private AtomicInteger finishedWatcher;

    private TimeLinePosition position;

    public static UserInfoFragment newInstance(UserBean userBean, String token) {
        UserInfoFragment fragment = new UserInfoFragment(userBean, token);
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public UserInfoFragment() {

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
        if (data == null) {
            return;
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackground(null);
        header = inflater
                .inflate(R.layout.newuserinfofragment_header_layout, getListView(), false);
        getListView().addHeaderView(header);

        footerView.setVisibility(View.GONE);

        progressFooter = inflater
                .inflate(R.layout.newuserinfofragment_progress_footer, getListView(), false);
        progressFooter.setVisibility(View.GONE);
        getListView().addFooterView(progressFooter);

        moreFooter = inflater
                .inflate(R.layout.newuserinfofragment_more_footer, getListView(), false);
        moreFooter.setVisibility(View.GONE);
        getListView().addFooterView(moreFooter);

        viewPager = ViewUtility.findViewById(header, R.id.viewpager);
        cover = ViewUtility.findViewById(header, R.id.cover);
        blur = ViewUtility.findViewById(header, R.id.blur);
        friendsCount = ViewUtility.findViewById(header, R.id.friends_count);
        fansCount = ViewUtility.findViewById(header, R.id.fans_count);
        topicsCount = ViewUtility.findViewById(header, R.id.topics_count);
        weiboCount = ViewUtility.findViewById(header, R.id.weibo_count);

        headerFirst = inflater
                .inflate(R.layout.newuserinfofragment_header_viewpager_first_layout, null, false);
        headerSecond = inflater
                .inflate(R.layout.newuserinfofragment_header_viewpager_second_layout, null, false);
        headerThird = inflater
                .inflate(R.layout.newuserinfofragment_header_viewpager_third_layout, null, false);

        avatar = ViewUtility.findViewById(headerFirst, R.id.avatar);
        nickname = ViewUtility.findViewById(headerFirst, R.id.nickname);
        location = ViewUtility.findViewById(headerFirst, R.id.location);
        followsYou = ViewUtility.findViewById(headerFirst, R.id.follows_you);

        bio = ViewUtility.findViewById(headerSecond, R.id.bio);
        url = ViewUtility.findViewById(headerSecond, R.id.url);
        verifiedReason = ViewUtility.findViewById(headerThird, R.id.verified_reason);

        leftPoint = ViewUtility.findViewById(header, R.id.left_point);
        centerPoint = ViewUtility.findViewById(header, R.id.center_point);
        rightPoint = ViewUtility.findViewById(header, R.id.right_point);
        leftPoint.getDrawable().setLevel(1);

        View weiboCountLayout = ViewUtility.findViewById(header, R.id.weibo_count_layout);
        View friendsCountLayout = ViewUtility.findViewById(header, R.id.friends_count_layout);
        View fansCountLayout = ViewUtility.findViewById(header, R.id.fans_count_layout);
        View topicCountLayout = ViewUtility.findViewById(header, R.id.topics_count_layout);

        weiboCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UserTimeLineActivity
                        .newIntent(GlobalContext.getInstance().getSpecialToken(), userBean);
                startActivity(intent);
            }
        });

        friendsCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = FriendListActivity
                        .newIntent(GlobalContext.getInstance().getSpecialToken(),
                                userBean);
                startActivity(intent);
            }
        });

        fansCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = FanListActivity
                        .newIntent(GlobalContext.getInstance().getSpecialToken(),
                                userBean);
                startActivity(intent);
            }
        });

        topicCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = UserTopicListActivity.newIntent(userBean, topicList);
                startActivity(intent);
            }
        });

        View result = view;

        if (!isOpenedFromMainPage()) {
            SwipeFrameLayout swipeFrameLayout = new SwipeFrameLayout(getActivity());
            swipeFrameLayout.addView(result,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            result = swipeFrameLayout;
        }

        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getPullToRefreshListView().setMode(PullToRefreshBase.Mode.DISABLED);
        getPullToRefreshListView().setOnLastItemVisibleListener(null);
        getPullToRefreshListView().getRefreshableView().setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            float rawX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        rawX = event.getRawX();
                        return false;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        viewPager.getParent().requestDisallowInterceptTouchEvent(false);
                        rawX = 0f;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(rawX - event.getRawX()) > ViewConfiguration.get(getActivity())
                                .getScaledTouchSlop()) {
                            viewPager.getParent().requestDisallowInterceptTouchEvent(true);
                        }

                        break;
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

            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (position == 0) {
                    if (positionOffset > 0) {
                        blur.setAlpha(positionOffset);
                    }
                }
            }
        });
    }

    private void displayBasicInfo() {
        HeaderPagerAdapter adapter = new HeaderPagerAdapter();
        viewPager.setAdapter(adapter);

        friendsCount.setText(
                Utility.convertStateNumberToString(getActivity(), userBean.getFriends_count()));
        fansCount.setText(
                Utility.convertStateNumberToString(getActivity(), userBean.getFollowers_count()));
        weiboCount.setText(
                Utility.convertStateNumberToString(getActivity(), userBean.getStatuses_count()));

        TextPaint tp = nickname.getPaint();
        tp.setFakeBoldText(true);
        if (TextUtils.isEmpty(userBean.getRemark())) {
            nickname.setText(userBean.getScreen_name());
        } else {
            nickname.setText(userBean.getScreen_name() + "(" + userBean.getRemark() + ")");
        }

        getActivity().getActionBar().setTitle(userBean.getScreen_name());

        avatar.checkVerified(userBean);

        if (!userBean.isVerified()) {
            rightPoint.setVisibility(View.GONE);
        } else {
            rightPoint.setVisibility(View.VISIBLE);
        }

        avatar.getImageView().post(new Runnable() {
            @Override
            public void run() {

                TimeLineBitmapDownloader.getInstance()
                        .display(avatar.getImageView(), avatar.getImageView().getWidth()
                                , avatar.getImageView().getHeight(), userBean.getAvatar_large(),
                                FileLocationMethod.avatar_large);
            }
        });

//        TimeLineBitmapDownloader.getInstance().downloadAvatar(avatar.getImageView(), userBean, (AbstractTimeLineFragment) this);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = FileManager.getFilePathFromUrl(userBean.getAvatar_large(),
                        FileLocationMethod.avatar_large);
                if (!ImageUtility.isThisBitmapCanRead(path)) {

                    path = FileManager.getFilePathFromUrl(userBean.getProfile_image_url(),
                            FileLocationMethod.avatar_small);

                    if (!ImageUtility.isThisBitmapCanRead(path)) {
                        return;
                    }
                }
                Rect rect = AnimationUtility.getBitmapRectFromImageView(avatar);
                UserAvatarDialog dialog = UserAvatarDialog.newInstance(path, rect);
                dialog.show(getFragmentManager(), "");
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
            TimeLineUtility.addLinks(url);
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
            followsYou.setText(
                    getString(R.string.is_following_me) + "@" + GlobalContext.getInstance()
                            .getCurrentAccountName());
        } else {
            followsYou.setVisibility(View.GONE);
        }
    }

    private void displayCoverPicture() {
        if (cover.getDrawable() != null) {
            return;
        }

//        final int height = viewPager.getHeight();
        final int height = Utility.dip2px(200);
        final int width = Utility.getMaxLeftWidthOrHeightImageViewCanRead(height);
        final String picPath = userBean.getCover_image();
        blur.setAlpha(0f);
        blur.setOriImageUrl(picPath);
        ArrayList<ImageView> imageViewArrayList = new ArrayList<ImageView>();
        imageViewArrayList.add(cover);
        imageViewArrayList.add(blur);
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
                -100f, Animation.RELATIVE_TO_SELF, 0f);
        animation.setDuration(3000);
        animation.setInterpolator(new DecelerateInterpolator());
        ArrayList<Animation> animationArray = new ArrayList<Animation>();
        animationArray.add(animation);
        TimeLineBitmapDownloader.getInstance()
                .display(imageViewArrayList, width, height, picPath, FileLocationMethod.cover,
                        animationArray);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userBean != null
                && userBean.getId() != null
                && userBean.getId().equals(GlobalContext.getInstance().getCurrentAccountId())) {
            GlobalContext.getInstance()
                    .registerForAccountChangeListener(myProfileInfoChangeListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(refreshTask, topicListTask);
        GlobalContext.getInstance().unRegisterForAccountChangeListener(myProfileInfoChangeListener);
    }

    private GlobalContext.MyProfileInfoChangeListener myProfileInfoChangeListener
            = new GlobalContext.MyProfileInfoChangeListener() {
        @Override
        public void onChange(UserBean newUserBean) {

            if (getActivity() == null) {
                return;
            }

            userBean = newUserBean;
            displayBasicInfo();
            displayCoverPicture();
            for (MessageBean msg : getList().getItemList()) {
                msg.setUser(newUserBean);
            }
            getAdapter().notifyDataSetChanged();
        }
    };

    public UserInfoFragment(UserBean userBean, String token) {
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
                displayBasicInfo();
                break;
            case SCREEN_ROTATE:
                //nothing
                refreshLayout(getList());
                displayBasicInfo();
                displayCoverPicture();
                if (bean.getSize() > 0) {
                    moreFooter.setVisibility(View.VISIBLE);
                    getListView().removeFooterView(progressFooter);
                }
                break;
            case ACTIVITY_DESTROY_AND_CREATE:
                getList().replaceData((MessageListBean) savedInstanceState.getParcelable("bean"));
                userBean = (UserBean) savedInstanceState.getParcelable("userBean");
                token = savedInstanceState.getString("token");
                getAdapter().notifyDataSetChanged();
                refreshLayout(getList());
                displayBasicInfo();
                displayCoverPicture();
                break;
        }

        super.onActivityCreated(savedInstanceState);

        if ((getActivity() instanceof MainTimeLineActivity)
                && (((MainTimeLineActivity) getActivity()).getMenuFragment()).getCurrentIndex()
                == LeftMenuFragment.PROFILE_INDEX) {
            buildActionBarAndViewPagerTitles();
        }
    }

    private void fetchTopicInfoFromServer() {
        topicListTask = new TopicListTask();
        topicListTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if ((getActivity() instanceof MainTimeLineActivity) && !hidden) {
            buildActionBarAndViewPagerTitles();
        }
    }

    public void buildActionBarAndViewPagerTitles() {
        ((MainTimeLineActivity) getActivity()).setCurrentFragment(this);

        if (Utility.isDevicePort()) {
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.profile));
            getActivity().getActionBar().setIcon(R.drawable.ic_menu_profile);
        } else {
            ((MainTimeLineActivity) getActivity()).setTitle(getString(R.string.profile));
            getActivity().getActionBar().setIcon(R.drawable.ic_launcher);
        }

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();
    }

    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {

        startActivityForResult(
                BrowserWeiboMsgActivity.newIntent(getList().getItem(position),
                        GlobalContext.getInstance().getSpecialToken()),
                0);
    }

    private boolean isMyself() {

        if (!TextUtils.isEmpty(userBean.getId())) {
            return userBean.getId().equals(GlobalContext.getInstance().getCurrentAccountId());
        }

        if (!TextUtils.isEmpty(userBean.getScreen_name())) {
            return userBean.getScreen_name()
                    .equals(GlobalContext.getInstance().getCurrentAccountName());
        }

        return false;
    }

    private boolean isOpenedFromMainPage() {
        return getActivity() instanceof MainTimeLineActivity;
    }

    @Override
    protected boolean allowLoadOldMsgBeforeReachListBottom() {
        return false;
    }

    @Override
    protected void newMsgLoaderSuccessCallback(MessageListBean newValue, Bundle loaderArgs) {
        stopRefreshMenuAnimationIfPossible();
        getListView().removeFooterView(progressFooter);
        if (getActivity() != null && newValue.getSize() > 0) {
            getList().addNewData(newValue);
            getAdapter().notifyDataSetChanged();
            getListView().setSelectionAfterHeaderView();
            getActivity().invalidateOptionsMenu();
            moreFooter.setVisibility(View.VISIBLE);
            if (isMyself()) {
                MyStatusDBTask.asyncReplace(getList(), userBean.getId());
            }

            UserBean userInfoFromServer = newValue.getItem(0).getUser();
            if (userInfoFromServer != null && !isSinaWeiboBlockWeiciyuanFetchUserInfo(userInfoFromServer)) {
                UserInfoFragment.this.userBean = userInfoFromServer;
                displayBasicInfo();
                displayCoverPicture();
                if (getActivity() instanceof UserInfoActivity) {
                    ((UserInfoActivity) getActivity()).setUser(userInfoFromServer);
                    getActivity().invalidateOptionsMenu();
                }
            }
        }
    }

    @Override
    protected void newMsgLoaderFailedCallback(WeiboException exception) {
        super.newMsgLoaderFailedCallback(exception);
        stopRefreshMenuAnimationIfPossible();
        getListView().removeFooterView(progressFooter);
    }

    @Override
    protected void oldMsgLoaderSuccessCallback(MessageListBean newValue) {

    }

    private void readDBCache() {
        if (Utility.isTaskStopped(dbTask) && getList().getSize() == 0) {
            dbTask = new DBCacheTask();
            dbTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void loadNewMsg() {
        progressFooter.setVisibility(View.VISIBLE);
        moreFooter.setVisibility(View.GONE);
        getLoaderManager().destroyLoader(MIDDLE_MSG_LOADER_ID);
        getLoaderManager().destroyLoader(OLD_MSG_LOADER_ID);
        dismissFooterView();
        getLoaderManager().restartLoader(NEW_MSG_LOADER_ID, null, msgAsyncTaskLoaderCallback);
    }

    @Override
    protected void loadOldMsg(View view) {
        Intent intent = UserTimeLineActivity
                .newIntent(GlobalContext.getInstance().getSpecialToken(), userBean);
        startActivity(intent);
    }

    protected Loader<AsyncTaskLoaderResult<MessageListBean>> onCreateNewMsgLoader(int id,
            Bundle args) {
        String uid = userBean.getId();
        String screenName = userBean.getScreen_name();
        String sinceId = null;
        if (getList().getItemList().size() > 0) {
            sinceId = getList().getItemList().get(0).getId();
        }
        return new StatusesByIdLoader(getActivity(), uid, screenName, token, sinceId, null,
                LIMITED_READ_MESSAGE_COUNT);
    }

    @Override
    public void scrollToTop() {
        Utility.stopListViewScrollingAndScrollToTop(getListView());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (isMyself() && isOpenedFromMainPage()) {
            inflater.inflate(R.menu.actionbar_menu_newuserinfofragment_main_page, menu);
            MenuItem edit = menu.findItem(R.id.menu_edit);
            edit.setVisible(GlobalContext.getInstance().getAccountBean().isBlack_magic());
            refreshItem = menu.findItem(R.id.menu_refresh_my_profile);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh_my_profile:
                startRefreshMenuAnimation();
                finishedWatcher = new AtomicInteger(3);
                fetchLastestUserInfoFromServer();
                fetchTopicInfoFromServer();
                loadNewMsg();
                return true;
            case R.id.menu_edit:
                if (isMyself() && isOpenedFromMainPage()) {
                    Intent intent = new Intent(getActivity(), EditMyProfileActivity.class);
                    intent.putExtra("userBean",
                            GlobalContext.getInstance().getAccountBean().getInfo());
                    startActivity(intent);
                    return true;
                } else {
                    return super.onOptionsItemSelected(item);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void startRefreshMenuAnimation() {
        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.newuserinfofragment_refresh_actionbar_view_layout, null);
        refreshItem.setActionView(v);
    }

    private void stopRefreshMenuAnimation() {
        if (refreshItem.getActionView() != null) {
            refreshItem.setActionView(null);
        }
    }

    private void stopRefreshMenuAnimationIfPossible() {
        if (!isMyself() || !isOpenedFromMainPage()) {
            return;
        }

        if (finishedWatcher == null) {
            return;
        }

        finishedWatcher.getAndDecrement();
        if (finishedWatcher.get() == 0) {
            stopRefreshMenuAnimation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!getActivity().isChangingConfigurations() && isMyself() && isOpenedFromMainPage()) {
            savePositionToDB();
        }
    }

    @Override
    protected void onListViewScrollStop() {
        savePositionToPositionsCache();
    }

    private void savePositionToDB() {
        if (position == null) {
            savePositionToPositionsCache();
        }
        MyStatusDBTask
                .asyncUpdatePosition(position, GlobalContext.getInstance().getCurrentAccountId());
    }

    private void savePositionToPositionsCache() {
        position = Utility.getCurrentPositionFromListView(getListView());
    }

    private void setListViewPositionFromPositionsCache() {

        Utility.setListViewAdapterPosition(getListView(),
                position != null ? position.getPosition(bean) : 0,
                position != null ? position.top : 0, null);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        AnimationUtility.forceConvertActivityToTranslucent(getActivity());
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (getActivity() == null) {
            return;
        }

        AnimationUtility.forceConvertActivityFromTranslucent(getActivity());

        displayCoverPicture();

        if (isMyself() && isOpenedFromMainPage()) {
            readDBCache();
        } else {
            fetchLastestUserInfoFromServer();
            loadNewMsg();
            fetchTopicInfoFromServer();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    //http://open.weibo.com/qa/index.php?qa=32088
    private boolean isSinaWeiboBlockWeiciyuanFetchUserInfo(UserBean userInfoFromServer) {
        return userInfoFromServer.getStatuses_count().equals("0") && userInfoFromServer.getFollowers_count().equals("0")
                && userInfoFromServer.getFriends_count().equals("0");
    }

    class HeaderPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = null;
            switch (position) {
                case 0:
                    view = headerFirst;

                    break;
                case 1:
                    view = headerSecond;
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
            UserTopicListDao dao = new UserTopicListDao(
                    GlobalContext.getInstance().getSpecialToken(), userBean.getId());
            try {
                return dao.getGSONMsgList();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(ArrayList<String> strings) {
            super.onCancelled(strings);
            stopRefreshMenuAnimationIfPossible();
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            stopRefreshMenuAnimationIfPossible();

            if (isCancelled()) {
                return;
            }
            if (result == null || result.size() == 0) {
                return;
            }
            topicList = result;
            topicsCount.setText(Utility.convertStateNumberToString(getActivity(),
                    String.valueOf(result.size())));
            ArrayList<String> dbCache = new ArrayList<String>();
            dbCache.addAll(topicList);
            TopicDBTask.asyncReplace(userBean.getId(), dbCache);
        }
    }

    //sina api has bug,so must refresh to get actual data
    public void forceReloadData(UserBean bean) {
        this.userBean = bean;
        fetchLastestUserInfoFromServer();
    }

    private void fetchLastestUserInfoFromServer() {
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
                if (user == null) {
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
                newMsgTipBar.setError(e.getError());
            }
            stopRefreshMenuAnimationIfPossible();
        }

        @Override
        protected void onPostExecute(UserBean userInfoFromServer) {
            if (userInfoFromServer == null || getActivity() == null) {
                return;
            }

            //hack to fix sina weibo issue
            if (isSinaWeiboBlockWeiciyuanFetchUserInfo(userInfoFromServer)) {
                AppLogger.e("Sina Weibo block Weiciyuan fetch user info from server!");
                return;
            }

            UserInfoFragment.this.userBean = userInfoFromServer;
            displayBasicInfo();
            displayCoverPicture();
            if (getActivity() instanceof UserInfoActivity) {
                ((UserInfoActivity) getActivity()).setUser(userInfoFromServer);
                getActivity().invalidateOptionsMenu();
            }
            for (MessageBean msg : bean.getItemList()) {
                msg.setUser(userInfoFromServer);
            }
            if (isMyself()) {
                GlobalContext.getInstance().updateUserInfo(userInfoFromServer);
                AccountDBTask.asyncUpdateMyProfile(GlobalContext.getInstance().getAccountBean(), userInfoFromServer);
            }
            getAdapter().notifyDataSetChanged();
            stopRefreshMenuAnimationIfPossible();
            super.onPostExecute(userInfoFromServer);
        }
    }

    private class DBCacheTask extends MyAsyncTask<Void, ArrayList<String>, MyStatusTimeLineData> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressFooter.setVisibility(View.VISIBLE);
        }

        @Override
        protected MyStatusTimeLineData doInBackground(Void... params) {
            ArrayList<String> topicList = TopicDBTask.get(userBean.getId());
            publishProgress(topicList);
            return MyStatusDBTask.get(userBean.getId());
        }

        @Override
        protected void onProgressUpdate(ArrayList<String>... values) {
            super.onProgressUpdate(values);
            ArrayList<String> result = values[0];
            if (result == null || result.size() == 0) {
                return;
            }
            topicList = result;
            topicsCount.setText(Utility.convertStateNumberToString(getActivity(),
                    String.valueOf(result.size())));
        }

        @Override
        protected void onPostExecute(MyStatusTimeLineData result) {
            super.onPostExecute(result);
            if (getActivity() == null) {
                return;
            }

            if (result != null && getActivity() != null) {
                getListView().removeFooterView(progressFooter);
                getList().addNewData(result.msgList);
                getAdapter().notifyDataSetChanged();
                position = result.position;
                setListViewPositionFromPositionsCache();
                getActivity().invalidateOptionsMenu();
                moreFooter.setVisibility(View.VISIBLE);
            }

            refreshLayout(getList());

            if (getList().getSize() == 0) {
                loadNewMsg();
            }
        }
    }
}


