package org.qii.weiciyuan.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountActivity extends AbstractAppActivity implements AdapterView.OnItemClickListener {
    /**
     * Called when the activity is first created.
     */

    private ListView listView;

    private AccountAdapter listAdapter;


    private List<AccountBean> accountList = new ArrayList<AccountBean>();

    private ActionMode mActionMode;

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
        getTask.execute();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        boolean checkAll = false;


        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.accountactivity_menu_contextual, menu);
            return true;
        }


        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            listAdapter.addCheckbox();

            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_select_all:
                    if (!checkAll) {
                        listAdapter.selectAll();
                        checkAll = true;
                        int[] attrs = new int[]{R.attr.accountactivity_select_none};
                        TypedArray ta = AccountActivity.this.obtainStyledAttributes(attrs);
                        Drawable drawableFromTheme = ta.getDrawable(0);
                        item.setIcon(drawableFromTheme);
                    } else {
                        listAdapter.unSelectButRemainCheckBoxAll();
                        checkAll = false;
                        int[] attrs = new int[]{R.attr.accountactivity_select_all};
                        TypedArray ta = AccountActivity.this.obtainStyledAttributes(attrs);
                        Drawable drawableFromTheme = ta.getDrawable(0);
                        item.setIcon(drawableFromTheme);
                    }
                    return true;
                case R.id.menu_remove_account:
                    if (removeTask == null || removeTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                        removeTask = new RemoveAccountDBTask();
                        removeTask.execute();
                    } else if (removeTask.getStatus() == MyAsyncTask.Status.PENDING || removeTask.getStatus() == MyAsyncTask.Status.RUNNING) {
                        removeTask.cancel(true);
                        removeTask = new RemoveAccountDBTask();
                        removeTask.execute();
                    }
                    mode.finish();
                    return true;
                default:
                    Toast.makeText(AccountActivity.this, "删除", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return false;
            }
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            listAdapter.unSelectAll();
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
        inflater.inflate(R.menu.accountactivity_menu_main, menu);
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
            if (getTask == null || getTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                getTask = new GetAccountListDBTask();
                getTask.execute();
            }

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

        Set<String> checkedItemPostion = new HashSet<String>();

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
                            checkedItemPostion.add(uid);
                        } else if (checkedItemPostion.contains(uid)) {
                            checkedItemPostion.remove(uid);
                        }

                    }
                });

                if (allChecked)
                    cb.setChecked(true);

                linearLayout.addView(cb, 0);

            }
            TextView textView = (TextView) mView.findViewById(R.id.account_name);

            textView.setText(accountList.get(i).getUsernick());

            ImageView imageView = (ImageView) mView.findViewById(R.id.imageView_avatar);

            if (!TextUtils.isEmpty(accountList.get(i).getAvatar_url())) {
                AvatarBitmapWorkerTask avatarTask = new AvatarBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), null, imageView, accountList.get(i).getAvatar_url());
                avatarTask.execute();
            }

            return mView;
        }


        public void addCheckbox() {
            needCheckbox = true;
            notifyDataSetChanged();
        }

        public void removeCheckbox() {
            needCheckbox = false;
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

        public Set<String> getCheckedItemPosition() {
            return checkedItemPostion;
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
            return DatabaseManager.getInstance().removeAndGetNewAccountList(listAdapter.getCheckedItemPosition());
        }

        @Override
        protected void onPostExecute(List<AccountBean> accounts) {
            accountList = accounts;
            listAdapter.notifyDataSetChanged();
            Toast.makeText(AccountActivity.this, getString(R.string.remove_successfully), Toast.LENGTH_SHORT).show();

        }
    }


}
