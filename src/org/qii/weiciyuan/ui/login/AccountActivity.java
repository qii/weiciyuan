package org.qii.weiciyuan.ui.login;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.*;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.lib.changelogdialog.ChangeLogDialog;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.blackmagic.BlackMagicActivity;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AccountActivity extends AbstractAppActivity implements LoaderManager.LoaderCallbacks<List<AccountBean>> {

    private ListView listView = null;
    private AccountAdapter listAdapter = null;
    private List<AccountBean> accountList = new ArrayList<AccountBean>();
    private final int ADD_ACCOUNT_REQUEST_CODE = 0;
    private final int LOADER_ID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        GlobalContext.getInstance().startedApp = true;
        jumpToHomeActivity();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountactivity_layout);
        getActionBar().setTitle(getString(R.string.app_name));
        listAdapter = new AccountAdapter();
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AccountListItemClickListener());
        listView.setAdapter(listAdapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AccountMultiChoiceModeListener());
        getLoaderManager().initLoader(LOADER_ID, null, this);

        if (SettingUtility.firstStart())
            showChangeLogDialog();
    }


    @Override
    public void onBackPressed() {
        GlobalContext.getInstance().startedApp = false;
        super.onBackPressed();
    }

    private void showChangeLogDialog() {
        ChangeLogDialog changeLogDialog = new ChangeLogDialog(this);
        changeLogDialog.show();
    }


    private void jumpToHomeActivity() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean launcher = intent.getBooleanExtra("launcher", true);
            if (launcher) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                String id = sharedPref.getString("id", "");
                if (!TextUtils.isEmpty(id)) {
                    AccountBean bean = AccountDBTask.getAccount(id);
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
        getMenuInflater().inflate(R.menu.actionbar_menu_accountactivity, menu);
        menu.findItem(R.id.menu_hack_login).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_add_account:
                String[] values;
                if (getResources().getBoolean(R.bool.blackmagic)) {
                    values = new String[3];
                    values[0] = getString(R.string.oauth_login);
                    values[1] = getString(R.string.official_app_login);
                    values[2] = getString(R.string.hack_login);
                } else {
                    values = new String[2];
                    values[0] = getString(R.string.oauth_login);
                    values[1] = getString(R.string.official_app_login);
                }
                new AlertDialog.Builder(this).setItems(values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent;
                        if (which == 0)
                            intent = new Intent(AccountActivity.this, OAuthActivity.class);
                        else if (which == 1)
                            intent = new Intent(AccountActivity.this, SSOActivity.class);
                        else
                            intent = new Intent(AccountActivity.this, BlackMagicActivity.class);
                        startActivityForResult(intent, ADD_ACCOUNT_REQUEST_CODE);
                    }
                }).show();

                break;
//            case R.id.menu_hack_login:
//                intent = new Intent(this, BlackMagicActivity.class);
//                startActivityForResult(intent, ADD_ACCOUNT_REQUEST_CODE);
//                break;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_ACCOUNT_REQUEST_CODE && resultCode == RESULT_OK) {
            refresh();
            if (data == null)
                return;
            String expires_time = data.getExtras().getString("expires_in");
            long expiresDays = TimeUnit.SECONDS.toDays(Long.valueOf(expires_time));

            String content = String.format(getString(R.string.token_expires_in_time), String.valueOf(expiresDays));
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage(content)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            builder.show();

        }
    }

    private void refresh() {
        getLoaderManager().getLoader(LOADER_ID).forceLoad();
    }

    @Override
    public Loader<List<AccountBean>> onCreateLoader(int id, Bundle args) {
        return new AccountDataLoader(AccountActivity.this, args);
    }

    @Override
    public void onLoadFinished(Loader<List<AccountBean>> loader, List<AccountBean> data) {
        accountList = data;
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<AccountBean>> loader) {
        accountList = new ArrayList<AccountBean>();
        listAdapter.notifyDataSetChanged();
    }

    private class AccountListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            Intent intent = new Intent(AccountActivity.this, MainTimeLineActivity.class);
            intent.putExtra("account", accountList.get(i));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private class AccountMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_menu_accountactivity, menu);
            mode.setTitle(getString(R.string.account_management));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_remove_account:
                    remove();
                    mode.finish();
                    return true;
            }
            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            listAdapter.notifyDataSetChanged();
        }
    }


    private class AccountAdapter extends BaseAdapter {

        int checkedBG;
        int defaultBG;

        public AccountAdapter() {
            defaultBG = getResources().getColor(R.color.transparent);

            int[] attrs = new int[]{R.attr.listview_checked_color};
            TypedArray ta = obtainStyledAttributes(attrs);
            checkedBG = ta.getColor(0, 430);
        }

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
            return Long.valueOf(accountList.get(i).getUid());
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            LayoutInflater layoutInflater = getLayoutInflater();

            View mView = layoutInflater.inflate(R.layout.accountactivity_listview_item_layout, viewGroup, false);
            mView.findViewById(R.id.listview_root).setBackgroundColor(defaultBG);

            if (listView.getCheckedItemPositions().get(i)) {
                mView.findViewById(R.id.listview_root).setBackgroundColor(checkedBG);
            }

            TextView textView = (TextView) mView.findViewById(R.id.account_name);
            if (accountList.get(i).getInfo() != null)
                textView.setText(accountList.get(i).getInfo().getScreen_name());
            else
                textView.setText(accountList.get(i).getUsernick());
            ImageView imageView = (ImageView) mView.findViewById(R.id.imageView_avatar);

            if (!TextUtils.isEmpty(accountList.get(i).getAvatar_url())) {
                commander.downloadAvatar(imageView, accountList.get(i).getInfo(), false);
            }

            TextView token = (TextView) mView.findViewById(R.id.token_expired);
            if (!Utility.isTokenValid(accountList.get(i))) {
                token.setVisibility(View.VISIBLE);
            } else {
                token.setVisibility(View.GONE);
            }

            return mView;
        }
    }

    private static class AccountDataLoader extends AsyncTaskLoader<List<AccountBean>> {
        public AccountDataLoader(Context context, Bundle args) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        public List<AccountBean> loadInBackground() {
            return AccountDBTask.getAccountList();
        }
    }


    private void remove() {
        Set<String> set = new HashSet<String>();
        long[] ids = listView.getCheckedItemIds();
        for (long id : ids) {
            set.add(String.valueOf(id));
        }
        accountList = AccountDBTask.removeAndGetNewAccountList(set);
        listAdapter.notifyDataSetChanged();
    }


}
