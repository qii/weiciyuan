package org.qii.weiciyuan.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.user.EditMyProfileDao;
import org.qii.weiciyuan.support.asyncdrawable.ProfileAvatarReadWorker;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 13-2-28
 */
public class EditMyProfileActivity extends AbstractAppActivity {

    private UserBean userBean;
    private Layout layout;

    private ProfileAvatarReadWorker avatarTask;
    private SaveAsyncTask saveAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.edit_my_profile));
        setContentView(R.layout.editmyprofileactivity_layout);
        initLayout();
        userBean = (UserBean) getIntent().getSerializableExtra("userBean");
        initValue();

    }

    private void initLayout() {
        layout = new Layout();
        layout.avatar = (ImageView) findViewById(R.id.avatar);
        layout.nickname = (EditText) findViewById(R.id.nickname);
        layout.website = (EditText) findViewById(R.id.website);
        layout.info = (EditText) findViewById(R.id.info);

    }

    private void initValue() {
        String avatarUrl = userBean.getAvatar_large();
        if (!TextUtils.isEmpty(avatarUrl)) {
            avatarTask = new ProfileAvatarReadWorker(layout.avatar, avatarUrl);
            avatarTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
        layout.nickname.setText(userBean.getScreen_name());
        layout.nickname.setSelection(layout.nickname.getText().toString().length());
        layout.website.setText(userBean.getUrl());
        layout.info.setText(userBean.getDescription());
    }

    private void save() {
        if (Utility.isTaskStopped(saveAsyncTask)) {
            saveAsyncTask = new SaveAsyncTask();
            saveAsyncTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class SaveAsyncTask extends MyAsyncTask<Void, UserBean, UserBean> {
        String screenName;
        String url;
        String description;

        WeiboException e;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            screenName = layout.nickname.getText().toString();
            url = layout.website.getText().toString();
            description = layout.info.getText().toString();
        }

        @Override
        protected UserBean doInBackground(Void... params) {
            EditMyProfileDao dao = new EditMyProfileDao(GlobalContext.getInstance().getSpecialToken(), screenName);
            if (!TextUtils.isEmpty(url) && !url.equals(userBean.getUrl()))
                dao.setUrl(url);
            if (!TextUtils.isEmpty(description) && !description.equals(userBean.getDescription()))
                dao.setDescription(description);
            try {
                return dao.update();
            } catch (WeiboException e) {
                this.e = e;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(UserBean userBean) {
            super.onPostExecute(userBean);
            if (userBean != null) {
                Toast.makeText(EditMyProfileActivity.this, R.string.edit_successfully, Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (this.e != null)
                Toast.makeText(EditMyProfileActivity.this, e.getError(), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_editmyprofileactivity, menu);
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
            case R.id.menu_save:
                save();
                return true;
        }
        return false;
    }


    private class Layout {
        ImageView avatar;
        EditText nickname;
        EditText website;
        EditText info;

    }
}
