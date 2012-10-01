package org.qii.weiciyuan.ui.userinfo;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.user.RemarkDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.AppFragmentPagerAdapter;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IUserInfo;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.send.StatusNewActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
public class UserInfoActivity extends AbstractAppActivity implements IUserInfo,
        IToken {
    private String token;

    private UserBean bean;

    private ViewPager mViewPager = null;


    @Override
    public String getToken() {
        if (TextUtils.isEmpty(token))
            token = GlobalContext.getInstance().getSpecialToken();
        return token;
    }

    @Override
    public UserBean getUser() {
        return bean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = getIntent().getStringExtra("token");
        bean = (UserBean) getIntent().getSerializableExtra("user");
        if (bean == null) {
            Uri data = getIntent().getData();
            String d = data.toString();
            int index = d.lastIndexOf("/");
            String newValue = d.substring(index + 1);
            bean = new UserBean();
            bean.setScreen_name(newValue);

        }

        if (bean.getScreen_name().equals(GlobalContext.getInstance().getCurrentAccountName())
                || (bean.getId() != null && bean.getId().equals(GlobalContext.getInstance().getCurrentAccountId()))) {
            Intent intent = new Intent(this, MyInfoActivity.class);
            intent.putExtra("token", getToken());

            AccountBean accountBean = new AccountBean();
            accountBean.setAccess_token(token);
            accountBean.setUsernick(GlobalContext.getInstance().getCurrentAccountName());
            accountBean.setUid(GlobalContext.getInstance().getCurrentAccountId());
            UserBean userBean = new UserBean();
            userBean.setId(GlobalContext.getInstance().getCurrentAccountId());
            intent.putExtra("user", bean);
            intent.putExtra("account", accountBean);
            startActivity(intent);
            finish();
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.personal_info));
        setContentView(R.layout.maintimelineactivity_viewpager_layout);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        TimeLinePagerAdapter adapter = new TimeLinePagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.intro))
                .setTabListener(tabListener));

        actionBar.addTab(actionBar.newTab()
                .setText(getString(R.string.weibo))
                .setTabListener(tabListener));


    }

    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        boolean status = false;

        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {
            if (mViewPager.getCurrentItem() != tab.getPosition())
                mViewPager.setCurrentItem(tab.getPosition());

            switch (tab.getPosition()) {

                case 1:
                    status = true;
                    break;

            }
        }

        public void onTabUnselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {

                case 1:
                    status = false;
                    break;

            }
        }

        public void onTabReselected(ActionBar.Tab tab,
                                    FragmentTransaction ft) {
            switch (tab.getPosition()) {

                case 1:
                    if (status)
                        getStatusFragment().getListView().setSelection(0);
                    break;

            }
        }
    };

    ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.infofragment_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(this, MainTimeLineActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.menu_at:
                intent = new Intent(this, StatusNewActivity.class);
                intent.putExtra("token", getToken());
                intent.putExtra("content", "@" + bean.getScreen_name());
                intent.putExtra("accountName", GlobalContext.getInstance().getCurrentAccountName());
                intent.putExtra("accountId", GlobalContext.getInstance().getCurrentAccountId());
                startActivity(intent);
                break;
            case R.id.menu_modify_remark:
                UpdateRemarkDialog dialog = new UpdateRemarkDialog();
                dialog.show(getSupportFragmentManager(), "");
                break;
        }
        return false;
    }

    public void updateRemark(String remark) {

        new UpdateRemarkTask(remark).executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    private AbstractTimeLineFragment getStatusFragment() {
        return ((AbstractTimeLineFragment) getSupportFragmentManager().findFragmentByTag(
                StatusesByIdTimeLineFragment.class.getName()));
    }


    private UserInfoFragment getInfoFragment() {
        return ((UserInfoFragment) getSupportFragmentManager().findFragmentByTag(
                UserInfoFragment.class.getName()));
    }


    class UpdateRemarkTask extends MyAsyncTask<Void, UserBean, UserBean> {

        WeiboException e;
        String remark;

        UpdateRemarkTask(String remark) {
            this.remark = remark;
        }


        @Override
        protected UserBean doInBackground(Void... params) {
            try {
                return new RemarkDao(getToken(), bean.getId(), remark).updateRemark();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (this.e != null) {
                Toast.makeText(UserInfoActivity.this, this.e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(UserBean userBean) {
            super.onPostExecute(userBean);
            bean = userBean;
            if (getInfoFragment() != null)
                getInfoFragment().forceReloadData(userBean);

        }
    }

    class TimeLinePagerAdapter extends
            AppFragmentPagerAdapter {

        List<Fragment> list = new ArrayList<Fragment>();


        public TimeLinePagerAdapter(FragmentManager fm) {
            super(fm);

            list.add(new UserInfoFragment());
            list.add(new StatusesByIdTimeLineFragment(getUser(), getToken()));
        }

        @Override
        public Fragment getItem(int i) {
            return list.get(i);
        }

        @Override
        protected String getTag(int position) {
            List<String> tagList = new ArrayList<String>();
            tagList.add(UserInfoFragment.class.getName());
            tagList.add(StatusesByIdTimeLineFragment.class.getName());
            return tagList.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
