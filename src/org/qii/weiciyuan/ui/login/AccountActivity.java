package org.qii.weiciyuan.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.util.*;

public class AccountActivity extends AbstractAppActivity implements AdapterView.OnItemClickListener {
    /**
     * Called when the activity is first created.
     */

    private ListView listView;

    private AccountAdapter listAdapter;


    private List<AccountBean> accountList = new ArrayList<AccountBean>();

    private GetAccountListDBTask getTask = null;
    private RemoveAccountDBTask removeTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        jumpToHomeLine();
        GlobalContext.getInstance().startedApp = true;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountactivity_layout);

        listAdapter = new AccountAdapter();
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                startActionMode(mActionModeCallback);
                return true;
            }
        });
        listView.setAdapter(listAdapter);

        getTask = new GetAccountListDBTask();
        getTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        boolean checkAll = false;
        boolean isCancel = true;


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu_accountactivity, menu);
            return true;
        }


        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            listAdapter.addCheckbox();
            isCancel = true;
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_select_all:
                    if (!checkAll) {
                        listAdapter.selectAll();
                        checkAll = true;

                    } else {
                        listAdapter.unSelectButRemainCheckBoxAll();
                        checkAll = false;

                    }
                    return true;
                case R.id.menu_remove_account:
                    isCancel = false;
                    if (removeTask == null || removeTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                        removeTask = new RemoveAccountDBTask();
                        removeTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                    } else if (removeTask.getStatus() == MyAsyncTask.Status.PENDING || removeTask.getStatus() == MyAsyncTask.Status.RUNNING) {
                        removeTask.cancel(true);
                        removeTask = new RemoveAccountDBTask();
                        removeTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    mode.finish();
                    return true;
                default:
                    Toast.makeText(AccountActivity.this, getString(R.string.delete), Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return false;
            }
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            ActionMode mActionMode = null;
            if (isCancel)
                listAdapter.removeCheckbox();


        }
    };

    @Override
    public void onBackPressed() {
        GlobalContext.getInstance().startedApp = false;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getTask != null)
            getTask.cancel(true);

        if (removeTask != null)
            removeTask.cancel(true);
    }


    private void jumpToHomeLine() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean launcher = intent.getBooleanExtra("launcher", true);
            if (launcher) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String id = sharedPref.getString("id", "");
                if (!TextUtils.isEmpty(id)) {
                    AccountBean bean = DatabaseManager.getInstance().getAccount(id);
                    if (bean != null) {
                        Intent start = new Intent(AccountActivity.this, MainTimeLineActivity.class);
                        start.putExtra("account", bean);
                        startActivity(start);
                        finish();
                    }
                }
            }
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu_accountactivity, menu);
        menu.findItem(R.id.menu_add_account).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                addAccount(item);
                return true;
            }
        });
        return true;
    }

    public void addAccount(MenuItem menu) {

        Intent intent = new Intent(this, OAuthActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            refresh();

        }
    }

    private void refresh() {
        if (getTask == null || getTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            getTask = new GetAccountListDBTask();
            getTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        String token = accountList.get(i).getAccess_token();

        SharedPreferences settings = getPreferences(MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        editor.putString("token", token);

        editor.commit();

        Intent intent = new Intent(this, MainTimeLineActivity.class);
        intent.putExtra("account", accountList.get(i));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();


    }


    private class AccountAdapter extends BaseAdapter {

        boolean needCheckbox = false;

        boolean allChecked = false;

        Set<String> checkedItemId = new HashSet<String>();

        Set<Integer> checkedItemListViewPosition = new HashSet<Integer>();

        @Override
        public int getCount() {
            return accountList.size();
        }

        @Override
        public Object getItem(int i) {
            return accountList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            LayoutInflater layoutInflater = getLayoutInflater();

            View mView = layoutInflater.inflate(R.layout.accountactivity_listview_item_layout, viewGroup, false);
            if (needCheckbox) {
                LinearLayout linearLayout = (LinearLayout) mView;

                CheckBox cb = new CheckBox(AccountActivity.this);

                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        AccountBean account = accountList.get(i);
                        String uid = account.getUid();
                        if (isChecked) {
                            checkedItemId.add(uid);
                            checkedItemListViewPosition.add(i);
                        } else if (checkedItemId.contains(uid)) {
                            checkedItemId.remove(uid);
                            checkedItemListViewPosition.remove(i);
                        }

                    }
                });

                if (allChecked)
                    cb.setChecked(true);

                cb.setId(R.id.webView);
                linearLayout.addView(cb, 0);

            }
            TextView textView = (TextView) mView.findViewById(R.id.account_name);

            textView.setText(accountList.get(i).getUsernick());

            ImageView imageView = (ImageView) mView.findViewById(R.id.imageView_avatar);

            if (!TextUtils.isEmpty(accountList.get(i).getAvatar_url())) {

                commander.downloadAvatar(imageView, accountList.get(i).getAvatar_url(), i, listView,false);
            }

            return mView;
        }


        public void addCheckbox() {
            needCheckbox = true;
            notifyDataSetChanged();
        }

        public void removeCheckbox() {
            needCheckbox = false;
            unSelectAll();
            notifyDataSetChanged();
        }

        public void selectAll() {
            needCheckbox = true;
            allChecked = true;
            notifyDataSetChanged();
        }

        public void unSelectAll() {
            needCheckbox = false;
            allChecked = false;
            notifyDataSetChanged();
        }

        public void unSelectButRemainCheckBoxAll() {
            needCheckbox = true;
            allChecked = false;
            notifyDataSetChanged();
        }

        public Set<String> getCheckedItemId() {
            return checkedItemId;
        }

        public Set<Integer> getCheckedItemListViewPosition() {
            return checkedItemListViewPosition;
        }

    }

    class GetAccountListDBTask extends MyAsyncTask<Void, List<AccountBean>, List<AccountBean>> {

        @Override
        protected List<AccountBean> doInBackground(Void... params) {
            return DatabaseManager.getInstance().getAccountList();
        }

        @Override
        protected void onPostExecute(List<AccountBean> accounts) {
            accountList = accounts;
            listAdapter.notifyDataSetChanged();

        }
    }

    class RemoveAccountDBTask extends MyAsyncTask<Void, List<AccountBean>, List<AccountBean>> {

        @Override
        protected List<AccountBean> doInBackground(Void... params) {
            return DatabaseManager.getInstance().removeAndGetNewAccountList(listAdapter.getCheckedItemId());
        }

        @Override
        protected void onPostExecute(List<AccountBean> accounts) {
            accountList = accounts;

            Animation anim = AnimationUtils.loadAnimation(
                    AccountActivity.this, R.anim.account_delete_slide_out_right
            );

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    listAdapter.removeCheckbox();
                    listAdapter.notifyDataSetChanged();
//                    refresh();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            Set<Integer> position = listAdapter.getCheckedItemListViewPosition();
            int start = listView.getFirstVisiblePosition();
            int end = listView.getLastVisiblePosition();

            Iterator<Integer> iterator = position.iterator();
            while (iterator.hasNext()) {
                Integer i = iterator.next();
                if (i < start || i > end) {
                    iterator.remove();
                }
            }
            Integer[] array = position.toArray(new Integer[1]);
            int arraySize = array.length;
            for (int i = 0; i < arraySize; i++) {
                array[i] = array[i] - start;
            }

            if (arraySize <= listView.getChildCount()) {
                for (int i = 0; i < arraySize; i++) {
                    listView.getChildAt(array[i]).startAnimation(anim);
                }
            }

            int listViewChildCount = listView.getChildCount();
            for (int i = 0; i < listViewChildCount; i++) {
                listView.getChildAt(i).findViewById(R.id.webView).startAnimation(anim);
            }


        }
    }


}
