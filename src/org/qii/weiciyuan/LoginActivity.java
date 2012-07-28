package org.qii.weiciyuan;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.example.TestActivity;
import org.qii.weiciyuan.ui.HomeActivity;
import org.qii.weiciyuan.ui.login.OAuthActivity;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements AdapterView.OnItemClickListener {
    /**
     * Called when the activity is first created.
     */

    private ListView listView;

    private AccountAdapter accountAdapter;

    private List<String> accountList = new ArrayList<String>();

    private ActionMode mActionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        String account = settings.getString("username", "");
        if (!TextUtils.isEmpty(account)) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

        listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        listView.setOnItemClickListener(this);
        accountAdapter = new AccountAdapter();
        listView.setAdapter(accountAdapter);

        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            boolean checkAll = false;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {


            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.menu_select_all:
                        if (!checkAll) {
                            accountAdapter.selectAll();
                            checkAll = true;
                            item.setIcon(R.drawable.account_select_none);
                        } else {
                            accountAdapter.unSelectButRemainCheckBoxAll();
                            checkAll = false;
                            item.setIcon(R.drawable.account_select_all);
                        }
                        return true;
                    default:
                        Toast.makeText(LoginActivity.this, "删除", Toast.LENGTH_SHORT).show();
                        mode.finish();
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.login_menu, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                accountAdapter.unSelectAll();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                accountAdapter.addCheckbox();
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login, menu);
        return true;
    }

    public void addAccount(MenuItem menu) {

        Intent intent = new Intent(this, OAuthActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Bundle values = data.getExtras();

            String access_token = values.getString("access_token");
            String expires_in = values.getString("expires_in");

            accountList.add(access_token);

            accountAdapter.notifyDataSetChanged();

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        String token = accountList.get(i);

        SharedPreferences settings = getPreferences(MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();

        editor.putString("token", token);

        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra("token", token);

        startActivity(intent);


    }


    private class AccountAdapter extends BaseAdapter {

        boolean needCheckbox = false;

        boolean allChecked = false;

        @Override
        public int getCount() {
            return accountList.size();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getItem(int i) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getItemId(int i) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            LayoutInflater layoutInflater = getLayoutInflater();

            View mView = layoutInflater.inflate(R.layout.account_item, viewGroup, false);
            if (needCheckbox) {
                LinearLayout linearLayout = (LinearLayout) mView;

                CheckBox cb = new CheckBox(LoginActivity.this);

//                LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                if (allChecked)
                    cb.setChecked(true);

                linearLayout.addView(cb, 0);

            }
            TextView textView = (TextView) mView.findViewById(R.id.textView);

            textView.setText(accountList.get(i));

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

    }
}
