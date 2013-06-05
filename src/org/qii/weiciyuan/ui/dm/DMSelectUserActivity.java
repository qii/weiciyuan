package org.qii.weiciyuan.ui.dm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.bean.UserListBean;
import org.qii.weiciyuan.bean.android.AsyncTaskLoaderResult;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.basefragment.AbstractFriendsFanListFragment;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.interfaces.IUserInfo;
import org.qii.weiciyuan.ui.loader.FriendUserLoader;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 13-3-2
 */
public class DMSelectUserActivity extends AbstractAppActivity implements IUserInfo {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dmselectuseractivity_layout);
        getActionBar().setTitle(R.string.select_dm_receiver);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.list_content, new SelectFriendsListFragment(GlobalContext.getInstance().getCurrentAccountId()))
                    .commit();
        }
    }

    @Override
    public UserBean getUser() {
        return GlobalContext.getInstance().getAccountBean().getInfo();
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
        }
        return false;
    }

    public static class SelectFriendsListFragment extends AbstractFriendsFanListFragment {


        public SelectFriendsListFragment() {

        }

        public SelectFriendsListFragment(String uid) {
            super(uid);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(false);
            setRetainInstance(false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

        }

        @Override
        protected void newUserOnPostExecute() {
            //empty
        }

        protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
            Intent intent = new Intent();
            intent.putExtra("user", getList().getUsers().get(position));
            getActivity().setResult(0, intent);
            getActivity().finish();
        }

        @Override
        protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateNewMsgLoader(int id, Bundle args) {
            String token = GlobalContext.getInstance().getSpecialToken();
            String cursor = String.valueOf(0);
            return new FriendUserLoader(getActivity(), token, uid, cursor);
        }

        @Override
        protected Loader<AsyncTaskLoaderResult<UserListBean>> onCreateOldMsgLoader(int id, Bundle args) {

            if (getList().getUsers().size() > 0 && Integer.valueOf(getList().getNext_cursor()) == 0) {
                return null;
            }


            String token = GlobalContext.getInstance().getSpecialToken();
            String cursor = String.valueOf(bean.getNext_cursor());

            return new FriendUserLoader(getActivity(), token, uid, cursor);
        }
    }
}
