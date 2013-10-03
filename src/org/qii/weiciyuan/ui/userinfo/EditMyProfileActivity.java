package org.qii.weiciyuan.ui.userinfo;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.UserBean;
import org.qii.weiciyuan.dao.show.ShowUserDao;
import org.qii.weiciyuan.dao.user.EditMyProfileDao;
import org.qii.weiciyuan.support.asyncdrawable.ProfileAvatarReadWorker;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 13-2-28
 */
public class EditMyProfileActivity extends AbstractAppActivity implements DialogInterface.OnClickListener {

    private static final int CAMERA_RESULT = 0;
    private static final int PIC_RESULT = 1;

    private UserBean userBean;
    private Layout layout;
    private MenuItem save;

    private ProfileAvatarReadWorker avatarTask;
    private SaveAsyncTask saveAsyncTask;
    private NewProfileAvatarReaderWorker newProfileAvatarReaderWorker;

    private Uri imageFileUri;
    private String picPath;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("picPath", picPath);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.edit_my_profile));
        setContentView(R.layout.editmyprofileactivity_layout);
        initLayout();
        userBean = (UserBean) getIntent().getParcelableExtra("userBean");
        initValue(savedInstanceState);

    }

    private void initLayout() {
        layout = new Layout();
        layout.avatar = (ImageView) findViewById(R.id.avatar);
        layout.avatar.setOnClickListener(avatarOnClickListener);
        layout.nickname = (EditText) findViewById(R.id.nickname);
        layout.website = (EditText) findViewById(R.id.website);
        layout.info = (EditText) findViewById(R.id.info);

    }

    private View.OnClickListener avatarOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            SelectProfilePictureDialog dialog = new SelectProfilePictureDialog();
            dialog.show(getFragmentManager(), "");
        }
    };

    private void initValue(Bundle savedInstanceState) {

        if (savedInstanceState == null || TextUtils.isEmpty(savedInstanceState.getString("picPath"))) {
            String avatarUrl = userBean.getAvatar_large();
            if (!TextUtils.isEmpty(avatarUrl)) {
                avatarTask = new ProfileAvatarReadWorker(layout.avatar, avatarUrl);
                avatarTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else if (!TextUtils.isEmpty(savedInstanceState.getString("picPath"))) {
            displayPic();
        }
        layout.nickname.setText(userBean.getScreen_name());
        layout.nickname.setSelection(layout.nickname.getText().toString().length());
        layout.nickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isNicknameEmpty();
            }
        });
        layout.website.setText(userBean.getUrl());
        layout.info.setText(userBean.getDescription());
    }

    private boolean isNicknameEmpty() {
        int sum = Utility.length(layout.nickname.getText().toString());
        if (sum == 0) {
            layout.nickname.setError(getString(R.string.nickname_cant_be_empty));
        }
        return sum == 0;
    }

    private boolean doesNicknameHaveSpace() {
        boolean result = layout.nickname.getText().toString().contains(" ");
        if (result) {
            layout.nickname.setError(getString(R.string.nickname_cant_have_space));
        }
        return result;
    }

    private void save() {
        if (Utility.isTaskStopped(saveAsyncTask) && !isNicknameEmpty() && !doesNicknameHaveSpace()) {
            saveAsyncTask = new SaveAsyncTask();
            saveAsyncTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0:
                imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new ContentValues());
                if (imageFileUri != null) {
                    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
                    if (Utility.isIntentSafe(EditMyProfileActivity.this, i)) {
                        startActivityForResult(i, CAMERA_RESULT);
                    } else {
                        Toast.makeText(EditMyProfileActivity.this, getString(R.string.dont_have_camera_app), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditMyProfileActivity.this, getString(R.string.cant_insert_album), Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(choosePictureIntent, PIC_RESULT);
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_RESULT:
                    picPath = Utility.getPicPathFromUri(imageFileUri, this);
                    break;
                case PIC_RESULT:
                    Uri imageFileUri = intent.getData();
                    picPath = Utility.getPicPathFromUri(imageFileUri, this);
                    break;
            }
        }
        if (!TextUtils.isEmpty(picPath)) {
            displayPic();
        }
    }


    private void displayPic() {
        if (Utility.isTaskStopped(newProfileAvatarReaderWorker)) {
            newProfileAvatarReaderWorker = new NewProfileAvatarReaderWorker();
            newProfileAvatarReaderWorker.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void startSaveAnimation() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ProgressBar pb = (ProgressBar) inflater.inflate(R.layout.editmyprofileactivity_refresh_actionbar_view_layout, null);
        save.setActionView(pb);
        layout.nickname.setEnabled(false);
        layout.website.setEnabled(false);
        layout.info.setEnabled(false);
        layout.avatar.setOnClickListener(null);
    }

    private void stopSaveAnimation() {
        if (save.getActionView() != null) {
            save.getActionView().clearAnimation();
            save.setActionView(null);
        }

        layout.nickname.setEnabled(true);
        layout.website.setEnabled(true);
        layout.info.setEnabled(true);
        layout.avatar.setOnClickListener(avatarOnClickListener);

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
            startSaveAnimation();
        }

        @Override
        protected UserBean doInBackground(Void... params) {
            EditMyProfileDao dao = new EditMyProfileDao(GlobalContext.getInstance().getSpecialToken(), screenName);
            dao.setUrl(url);
            dao.setDescription(description);
            dao.setAvatar(picPath);

            try {
                return dao.update();
            } catch (WeiboException e) {
                this.e = e;
                e.printStackTrace();
                cancel(true);
            }
            return null;
        }

        /**
         * sina weibo have a bug, after modify your profile, the return UserBean object dont have large avatar url
         * so must refresh to get actual data;
         */
        @Override
        protected void onPostExecute(UserBean userBean) {
            super.onPostExecute(userBean);
            if (userBean != null) {
                Toast.makeText(EditMyProfileActivity.this, R.string.edit_successfully, Toast.LENGTH_SHORT).show();
                new RefreshTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            if (this.e != null) {
                Toast.makeText(EditMyProfileActivity.this, e.getError(), Toast.LENGTH_SHORT).show();
            }
            stopSaveAnimation();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_editmyprofileactivity, menu);
        save = menu.findItem(R.id.menu_save);
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


    private class NewProfileAvatarReaderWorker extends MyAsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... url) {
            if (isCancelled())
                return null;


            int avatarWidth = getResources().getDimensionPixelSize(R.dimen.profile_avatar_width);
            int avatarHeight = getResources().getDimensionPixelSize(R.dimen.profile_avatar_height);

            return ImageUtility.getRoundedCornerPic(picPath, avatarWidth, avatarHeight);

        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {
                layout.avatar.setImageBitmap(bitmap);
            } else {
                layout.avatar.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

        }
    }


    private class RefreshTask extends MyAsyncTask<Object, UserBean, UserBean> {
        WeiboException e;

        @Override
        protected UserBean doInBackground(Object... params) {
            UserBean user = null;
            try {
                ShowUserDao dao = new ShowUserDao(GlobalContext.getInstance().getSpecialToken());
                dao.setUid(GlobalContext.getInstance().getAccountBean().getUid());
                user = dao.getUserInfo();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
            }
            if (user != null) {
                AccountDBTask.updateMyProfile(GlobalContext.getInstance().getAccountBean(), user);
            } else {
                cancel(true);
            }
            return user;
        }

        @Override
        protected void onCancelled(UserBean userBean) {
            super.onCancelled(userBean);
            Toast.makeText(EditMyProfileActivity.this, e.getError(), Toast.LENGTH_SHORT).show();
            stopSaveAnimation();

        }

        @Override
        protected void onPostExecute(UserBean userBean) {
            super.onPostExecute(userBean);
            stopSaveAnimation();
            GlobalContext.getInstance().updateUserInfo(userBean);
            finish();
        }
    }
}
