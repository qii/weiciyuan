package org.qii.weiciyuan.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.WeiboAccountBean;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.AbstractMainActivity;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountActivity extends AbstractMainActivity implements AdapterView.OnItemClickListener {
    /**
     * Called when the activity is first created.
     */

    private ListView listView;

    private AccountAdapter listAdapter;


    private List<WeiboAccountBean> weiboAccountList = new ArrayList<WeiboAccountBean>();

    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //jumpToHomeLine();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountactivity_layout);

        listAdapter = new AccountAdapter();
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setAdapter(listAdapter);


        listView.setMultiChoiceModeListener(multiChoiceModeLinstener);

        new GetAccountListDBTask().execute(null, null, null);

    }

    private AbsListView.MultiChoiceModeListener multiChoiceModeLinstener = new AbsListView.MultiChoiceModeListener() {

        boolean checkAll = false;

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {


        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_select_all:
                    if (!checkAll) {
                        listAdapter.selectAll();
                        checkAll = true;
                        item.setIcon(R.drawable.accountactivity_select_none);
                    } else {
                        listAdapter.unSelectButRemainCheckBoxAll();
                        checkAll = false;
                        item.setIcon(R.drawable.accountactivity_select_all);
                    }
                    return true;
                case R.id.menu_remove_account:
                    new RemoveAccountDBTask().execute();
                    mode.finish();
                    return true;
                default:
                    Toast.makeText(AccountActivity.this, "删除", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.accountactivity_menu_contextual, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            listAdapter.unSelectAll();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            listAdapter.addCheckbox();
            return false;
        }
    };

    private void jumpToHomeLine() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String username = settings.getString("username", "");
        String token = settings.getString("token", "");
        String expires = settings.getString("expires", "");

        boolean haveToken = !TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires);

        boolean haveUsername = !TextUtils.isEmpty(username);

        if (haveToken) {
            Intent intent = new Intent(AccountActivity.this, MainTimeLineActivity.class);
            intent.putExtra("token", token);
            intent.putExtra("expires", expires);

            if (haveUsername) {
                intent.putExtra("username", username);
            }

            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.accountactivity_menu_main, menu);
        return true;
    }

    public void addAccount(MenuItem menu) {

        Intent intent = new Intent(this, OAuthActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            new GetAccountListDBTask().execute();

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        String token = weiboAccountList.get(i).getAccess_token();

        SharedPreferences settings = getPreferences(MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        editor.putString("token", token);

        editor.commit();

        Intent intent = new Intent(this, MainTimeLineActivity.class);
        intent.putExtra("account", weiboAccountList.get(i));
        intent.putExtra("uid", weiboAccountList.get(i).getUid());
        startActivity(intent);


    }


    private class AccountAdapter extends BaseAdapter {

        boolean needCheckbox = false;

        boolean allChecked = false;

        Set<String> checkedItemPostion = new HashSet<String>();

        @Override
        public int getCount() {
            return weiboAccountList.size();
        }

        @Override
        public Object getItem(int i) {
            return weiboAccountList.get(i);
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
                        WeiboAccountBean account = weiboAccountList.get(i);
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

            textView.setText(weiboAccountList.get(i).getUsernick());

            ImageView imageView = (ImageView) mView.findViewById(R.id.imageView_avatar);

            if (!TextUtils.isEmpty(weiboAccountList.get(i).getAvatar_url())) {
                AvatarBitmapWorkerTask avatarTask = new AvatarBitmapWorkerTask(GlobalContext.getInstance().getAvatarCache(), null, imageView, listView, i);
                avatarTask.execute(weiboAccountList.get(i).getAvatar_url());
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

    class GetAccountListDBTask extends AsyncTask<Void, List<WeiboAccountBean>, List<WeiboAccountBean>> {

        @Override
        protected List<WeiboAccountBean> doInBackground(Void... params) {
            return DatabaseManager.getInstance().getAccountList();
        }

        @Override
        protected void onPostExecute(List<WeiboAccountBean> weiboAccounts) {
            weiboAccountList = weiboAccounts;
            listAdapter.notifyDataSetChanged();

        }
    }

    class RemoveAccountDBTask extends AsyncTask<Void, List<WeiboAccountBean>, List<WeiboAccountBean>> {

        @Override
        protected List<WeiboAccountBean> doInBackground(Void... params) {
            return DatabaseManager.getInstance().removeAndGetNewAccountList(listAdapter.getCheckedItemPosition());
        }

        @Override
        protected void onPostExecute(List<WeiboAccountBean> weiboAccounts) {
            weiboAccountList = weiboAccounts;
            listAdapter.notifyDataSetChanged();
            Toast.makeText(AccountActivity.this, "remove successfully", Toast.LENGTH_SHORT).show();

        }
    }
}
