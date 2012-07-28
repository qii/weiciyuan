package org.qii.weiciyuan;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import org.qii.weiciyuan.example.AuthorizeActivity;
import org.qii.weiciyuan.ui.HomeActivity;

public class LoginActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    /**
     * Called when the activity is first created.
     */

    private ListView listView;

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
        listView.setAdapter(new AccountAdapter());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login, menu);
        return true;
    }

    public void addAccount(MenuItem menu) {
        startActivity(new Intent(this, AuthorizeActivity.class));
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
            return 20;  //To change body of implemented methods use File | Settings | File Templates.
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


            return mView;
        }
    }
}
