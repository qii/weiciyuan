package org.qii.weiciyuan;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.ui.HomeActivity;
import org.qii.weiciyuan.ui.OAuthActivity;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    /**
     * Called when the activity is first created.
     */

    private ListView listView;

    private BaseAdapter accountAdapter;

    private List<String> accountList = new ArrayList<String>();

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
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        accountAdapter = new AccountAdapter();
        listView.setAdapter(accountAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login, menu);
        return true;
    }

    public void addAccount(MenuItem menu) {

        Intent intent = new Intent(this, OAuthActivity.class);
        intent.putExtra("url", "http://www.cnbeta.com");

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

            Toast.makeText(this, access_token, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    private class AccountAdapter extends BaseAdapter {

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

            TextView textView = (TextView) mView.findViewById(R.id.textView);

            textView.setText(accountList.get(i));

            return mView;
        }
    }
}
