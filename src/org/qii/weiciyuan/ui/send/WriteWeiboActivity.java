package org.qii.weiciyuan.ui.send;

import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.bean.android.MusicInfo;
import org.qii.weiciyuan.dao.location.BaiduGeoCoderDao;
import org.qii.weiciyuan.dao.location.GoogleGeoCoderDao;
import org.qii.weiciyuan.othercomponent.sendweiboservice.SendWeiboService;
import org.qii.weiciyuan.support.database.DraftDBManager;
import org.qii.weiciyuan.support.database.draftbean.StatusDraftBean;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.imageutility.ImageEditUtility;
import org.qii.weiciyuan.support.imageutility.ImageUtility;
import org.qii.weiciyuan.support.lib.CheatSheet;
import org.qii.weiciyuan.support.lib.KeyboardControlEditText;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.smileypicker.SmileyPicker;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.SmileyPickerUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.support.utils.ViewUtility;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.login.AccountActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.maintimeline.SaveDraftDialog;
import org.qii.weiciyuan.ui.search.AtUserActivity;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * User: qii
 * Date: 12-7-29
 */
public class WriteWeiboActivity extends AbstractAppActivity
        implements DialogInterface.OnClickListener,
        ClearContentDialog.IClear, SaveDraftDialog.IDraft {

    private static final int CAMERA_RESULT = 0;
    private static final int PIC_RESULT = 1;
    private static final int PIC_RESULT_KK = 2;
    public static final int AT_USER = 3;

    public static final String ACTION_DRAFT = "org.qii.weiciyuan.DRAFT";
    public static final String ACTION_SEND_FAILED = "org.qii.weiciyuan.SEND_FAILED";

    private AccountBean accountBean;
    protected String token = "";

    private StatusDraftBean statusDraftBean;

    private String picPath = "";
    private Uri imageFileUri = null;

    private GeoBean geoBean;
    private String location;

    private ImageView haveGPS = null;
    private KeyboardControlEditText content = null;
    private ImageView preview = null;
    private SmileyPicker smiley = null;
    private RelativeLayout container = null;

    private String2PicTask string2PicTask;
    private GetGoogleLocationInfo locationTask;

    public static Intent newIntent(AccountBean accountBean) {
        Intent intent = new Intent(GlobalContext.getInstance(), WriteWeiboActivity.class);
        intent.putExtra("token", accountBean.getAccess_token());
        intent.putExtra("account", accountBean);
        return intent;
    }

    public static Intent startBecauseSendFailed(Context context,
            AccountBean accountBean,
            String content,
            String picPath,
            GeoBean geoBean,
            StatusDraftBean statusDraftBean,
            String failedReason) {
        Intent intent = new Intent(context, WriteWeiboActivity.class);
        intent.setAction(WriteWeiboActivity.ACTION_SEND_FAILED);
        intent.putExtra("account", accountBean);
        intent.putExtra("content", content);
        intent.putExtra("failedReason", failedReason);
        intent.putExtra("picPath", picPath);
        intent.putExtra("geoBean", geoBean);
        intent.putExtra("statusDraftBean", statusDraftBean);
        return intent;
    }

    private void handleFailedOperation(Intent intent) {
        accountBean = (AccountBean) intent.getParcelableExtra("account");
        token = accountBean.getAccess_token();
        getActionBar().setSubtitle(accountBean.getUsernick());
        String stringExtra = intent.getStringExtra("content");
        content.setText(stringExtra);
        String failedReason = intent.getStringExtra("failedReason");
        content.setError(failedReason);
        picPath = intent.getStringExtra("picPath");
        if (!TextUtils.isEmpty(picPath)) {
            enablePicture();
        }

        geoBean = (GeoBean) intent.getSerializableExtra("geoBean");
        if (geoBean != null) {
            enableGeo();
        }

        statusDraftBean = (StatusDraftBean) intent.getParcelableExtra("statusDraftBean");
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0:
                Uri lastUri = Utility.getLatestCameraPicture(WriteWeiboActivity.this);
                if (lastUri != null) {
                    imageFileUri = lastUri;
                    createTmpUploadFileFromUri();
                    return;
                }

                Toast.makeText(WriteWeiboActivity.this,
                        getString(R.string.dont_have_the_last_picture), Toast.LENGTH_SHORT).show();

                break;
            case 1:
                imageFileUri = getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                new ContentValues());
                if (imageFileUri != null) {
                    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
                    if (Utility.isIntentSafe(WriteWeiboActivity.this, i)) {
                        startActivityForResult(i, CAMERA_RESULT);
                    } else {
                        Toast.makeText(WriteWeiboActivity.this,
                                getString(R.string.dont_have_camera_app), Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    Toast.makeText(WriteWeiboActivity.this, getString(R.string.cant_insert_album),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (Utility.isKK()) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    /**
                     * not all 4.4 version devices have system app which accept Intent.ACTION_OPEN_DOCUMENT
                     */
                    if (Utility.isIntentSafe(WriteWeiboActivity.this, intent)) {
                        startActivityForResult(intent, PIC_RESULT_KK);
                    } else {
                        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(choosePictureIntent, PIC_RESULT);
                    }
                } else {
                    Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(choosePictureIntent, PIC_RESULT);
                }
                break;
        }
    }

    private void createTmpUploadFileFromUri() {
        ConvertUriToCachePathAsyncTaskFragment fragment = ConvertUriToCachePathAsyncTaskFragment
                .newInstance(imageFileUri);
        getSupportFragmentManager().beginTransaction().add(fragment, "").commit();
    }

    public void picConvertSucceedKK(String path) {
        if (TextUtils.isEmpty(content.getText().toString())) {
            content.setText(getString(R.string.share_pic));
            content.setSelection(content.getText().toString().length());
        }

        picPath = path;
        enablePicture();
    }

    private void enablePicture() {
        Bitmap bitmap = ImageUtility.getWriteWeiboPictureThumblr(picPath);
        if (bitmap != null) {
            ((ImageButton) findViewById(R.id.menu_add_pic)).setImageBitmap(bitmap);
        }
        bitmap = ImageUtility.decodeBitmapFromSDCard(picPath, Utility.getScreenWidth(),
                Utility.getScreenHeight());
        if (bitmap != null) {
            preview.setVisibility(View.VISIBLE);
            preview.setImageBitmap(bitmap);
        }
    }

    private void disablePicture() {
        if (picPath != null) {
            new File(picPath).delete();
        }

        if (content.getText().toString().equals(getString(R.string.share_pic))) {
            content.setText("");
        }

        ((ImageButton) findViewById(R.id.menu_add_pic))
                .setImageDrawable(getResources().getDrawable(R.drawable.camera_light));

        preview.setVisibility(View.INVISIBLE);
        preview.setImageBitmap(null);
    }

    private boolean picture() {
        int level = ((ImageButton) findViewById(R.id.menu_add_pic)).getDrawable().getLevel();
        return level == 1;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_RESULT:
                    createTmpUploadFileFromUri();
                    break;
                case PIC_RESULT:
                case PIC_RESULT_KK:
                    imageFileUri = intent.getData();
                    createTmpUploadFileFromUri();
                    break;
                case AT_USER:
                    String name = intent.getStringExtra("name");
                    String ori = content.getText().toString();
                    int index = content.getSelectionStart();
                    StringBuilder stringBuilder = new StringBuilder(ori);
                    stringBuilder.insert(index, name);
                    content.setText(stringBuilder.toString());
                    content.setSelection(index + name.length());
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.cancelTasks(string2PicTask, locationTask);
    }

    protected boolean canShowSaveDraftDialog() {
        if (statusDraftBean == null) {
            return true;
        } else if (!statusDraftBean.getContent().equals(content.getText().toString())) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (smiley.isShown()) {
            hideSmileyPicker(false);
        } else if (!TextUtils.isEmpty(content.getText().toString()) && canShowSaveDraftDialog()) {
            SaveDraftDialog dialog = new SaveDraftDialog();
            dialog.show(getFragmentManager(), "");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("picPath", picPath);
        outState.putParcelable("geoBean", geoBean);
        outState.putString("location", location);
        outState.putParcelable("imageFileUri", imageFileUri);
        outState.putParcelable("statusDraftBean", statusDraftBean);
        outState.putParcelable("accountBean", accountBean);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            picPath = savedInstanceState.getString("picPath");
            if (!TextUtils.isEmpty(picPath)) {
                enablePicture();
            }
            geoBean = (GeoBean) savedInstanceState.getParcelable("geoBean");
            location = savedInstanceState.getString("location");
            if (geoBean != null && !TextUtils.isEmpty(location)) {
                enableGeo();
            } else {
                disableGeo();
            }

            imageFileUri = savedInstanceState.getParcelable("imageFileUri");
            statusDraftBean = (StatusDraftBean) savedInstanceState.getParcelable("statusDraftBean");
            accountBean = (AccountBean) savedInstanceState.getParcelable("accountBean");
            token = accountBean.getAccess_token();

            getActionBar().setSubtitle(getAccount().getUsernick());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!GlobalContext.getInstance().checkUserIsLogin()) {
            Toast.makeText(this, this.getString(R.string.share_failed_because_of_no_account),
                    Toast.LENGTH_SHORT).show();
            Intent intent = AccountActivity.newIntent();
            startActivity(intent);
            finish();
            return;
        }

        buildInterface();

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String action = intent.getAction();
            String type = intent.getType();
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(Intent.ACTION_SEND) && !TextUtils.isEmpty(type)) {
                    if ("text/plain".equals(type)) {
                        handleSendText(intent);
                    } else if (type.startsWith("image/")) {
                        handleSendImage(intent);
                    }
                } else if (action.equals(WriteWeiboActivity.ACTION_DRAFT)) {
                    handleDraftOperation(intent);
                } else if (action.equals(WriteWeiboActivity.ACTION_SEND_FAILED)) {
                    handleFailedOperation(intent);
                }
            } else {
                handleNormalOperation(intent);
            }
        }
    }

    private void handleDraftOperation(Intent intent) {
        accountBean = (AccountBean) intent.getParcelableExtra("account");
        token = accountBean.getAccess_token();
        getActionBar().setSubtitle(accountBean.getUsernick());

        statusDraftBean = (StatusDraftBean) intent.getParcelableExtra("draft");
        if (statusDraftBean != null) {
            content.setText(statusDraftBean.getContent());
            picPath = statusDraftBean.getPic();
            geoBean = statusDraftBean.getGps();

            if (!TextUtils.isEmpty(picPath)) {
                enablePicture();
            }

            if (geoBean != null) {
                if (Utility.isTaskStopped(locationTask)) {
                    locationTask = new GetGoogleLocationInfo(geoBean);
                    locationTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }

    private void handleNormalOperation(Intent intent) {
        accountBean = (AccountBean) intent.getParcelableExtra("account");
        token = accountBean.getAccess_token();
        getActionBar().setSubtitle(accountBean.getUsernick());
        String contentStr = intent.getStringExtra("content");
        if (!TextUtils.isEmpty(contentStr)) {
            content.setText(contentStr + " ");
            content.setSelection(content.getText().toString().length());
        }
    }

    private void buildInterface() {
        setContentView(R.layout.writeweiboactivity_layout);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(R.string.write_weibo);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        int avatarWidth = getResources().getDimensionPixelSize(R.dimen.timeline_avatar_width);
        int avatarHeight = getResources().getDimensionPixelSize(R.dimen.timeline_avatar_height);

        Bitmap bitmap = ImageUtility.getWriteWeiboRoundedCornerPic(
                GlobalContext.getInstance().getAccountBean().getInfo().getAvatar_large(),
                avatarWidth, avatarHeight, FileLocationMethod.avatar_large);
        if (bitmap == null) {
            bitmap = ImageUtility.getWriteWeiboRoundedCornerPic(
                    GlobalContext.getInstance().getAccountBean().getInfo().getProfile_image_url(),
                    avatarWidth, avatarHeight, FileLocationMethod.avatar_small);
        }
        if (bitmap != null) {
            actionBar.setIcon(new BitmapDrawable(getResources(), bitmap));
        }

        View title = getLayoutInflater().inflate(R.layout.writeweiboactivity_title_layout, null);
        TextView contentNumber = (TextView) title.findViewById(R.id.content_number);
        contentNumber.setVisibility(View.GONE);

        haveGPS = (ImageView) title.findViewById(R.id.have_gps);
        final PopupMenu popupMenu = new PopupMenu(this, haveGPS);
        popupMenu.inflate(R.menu.popmenu_gps);
        haveGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        popupMenu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_view:

                                StringBuilder geoUriString = new StringBuilder()
                                        .append("geo:" + geoBean.getLat() + "," + geoBean
                                                .getLon());
                                if (!TextUtils.isEmpty(location)) {
                                    geoUriString.append("?q=").append(location);
                                }
                                Uri geoUri = Uri.parse(geoUriString.toString());
                                Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
                                if (Utility.isIntentSafe(WriteWeiboActivity.this, mapCall)) {
                                    startActivity(mapCall);
                                } else {
                                    Toast.makeText(WriteWeiboActivity.this,
                                            R.string.your_device_dont_have_any_map_app_to_open_gps_info,
                                            Toast.LENGTH_SHORT).show();
                                }

                                break;
                            case R.id.menu_delete:
                                haveGPS.setVisibility(View.GONE);
                                geoBean = null;
                                break;
                        }

                        return true;
                    }
                });

        actionBar.setCustomView(title, new ActionBar.LayoutParams(Gravity.RIGHT));
        actionBar.setDisplayShowCustomEnabled(true);
        content = ((KeyboardControlEditText) findViewById(R.id.status_new_content));
        content.addTextChangedListener(
                new TextNumLimitWatcher((TextView) findViewById(R.id.menu_send), content, this));
        content.setDrawingCacheEnabled(true);
        AutoCompleteAdapter adapter = new AutoCompleteAdapter(this, content,
                (ProgressBar) title.findViewById(R.id.have_suggest_progressbar));
        content.setAdapter(adapter);

        preview = ViewUtility.findViewById(this, R.id.status_image_preview);

        View.OnClickListener onClickListener = new BottomButtonClickListener();
        findViewById(R.id.menu_at).setOnClickListener(onClickListener);
        findViewById(R.id.menu_emoticon).setOnClickListener(onClickListener);
        findViewById(R.id.menu_add_pic).setOnClickListener(onClickListener);
        findViewById(R.id.menu_send).setOnClickListener(onClickListener);

        CheatSheet.setup(WriteWeiboActivity.this, findViewById(R.id.menu_at), R.string.at_other);
        CheatSheet.setup(WriteWeiboActivity.this, findViewById(R.id.menu_emoticon),
                R.string.add_emoticon);
        CheatSheet
                .setup(WriteWeiboActivity.this, findViewById(R.id.menu_add_pic), R.string.add_pic);
        CheatSheet.setup(WriteWeiboActivity.this, findViewById(R.id.menu_send), R.string.send);

        smiley = (SmileyPicker) findViewById(R.id.smiley_picker);
        smiley.setEditText(WriteWeiboActivity.this, ((LinearLayout) findViewById(R.id.root_layout)),
                content);
        container = (RelativeLayout) findViewById(R.id.container);
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSmileyPicker(true);
            }
        });
    }

    private void getAccountInfo() {
        AccountBean account = GlobalContext.getInstance().getAccountBean();
        if (account != null) {
            accountBean = account;
            token = account.getAccess_token();
            getActionBar().setSubtitle(account.getUsernick());
        }
    }

    private void handleSendText(Intent intent) {
        getAccountInfo();
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (!TextUtils.isEmpty(sharedText)) {
            content.setText(sharedText);
            content.setSelection(content.getText().toString().length());
        }
    }

    private void handleSendImage(Intent intent) {
        handleSendText(intent);

        getAccountInfo();

        Uri sharedImageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (sharedImageUri != null) {
            this.imageFileUri = sharedImageUri;
            createTmpUploadFileFromUri();
        }
    }

    private boolean canSend() {
        boolean haveContent = !TextUtils.isEmpty(content.getText().toString());
        boolean haveToken = !TextUtils.isEmpty(token);

        int sum = Utility.length(content.getText().toString());
        int num = 140 - sum;

        boolean contentNumBelow140 = (num >= 0);

        if (haveContent && haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveContent && !haveToken) {
                Toast.makeText(this,
                        getString(R.string.content_cant_be_empty_and_dont_have_account),
                        Toast.LENGTH_SHORT).show();
            } else if (!haveContent) {
                content.setError(getString(R.string.content_cant_be_empty));
            } else if (!haveToken) {
                Toast.makeText(this, getString(R.string.dont_have_account), Toast.LENGTH_SHORT)
                        .show();
            }

            if (!contentNumBelow140) {
                content.setError(getString(R.string.content_words_number_too_many));
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_statusnewactivity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String contentStr = content.getText().toString();
        if (!TextUtils.isEmpty(contentStr)) {
            menu.findItem(R.id.menu_clear).setVisible(true);
        } else {
            menu.findItem(R.id.menu_clear).setVisible(false);
        }

        MusicInfo musicInfo = GlobalContext.getInstance().getMusicInfo();
        if (!musicInfo.isEmpty()) {
            MenuItem musicMenu = menu.findItem(R.id.menu_add_now_playing);
            musicMenu.setVisible(true);
            musicMenu.setTitle(musicInfo.toString());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                saveToDraft();
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                intent = MainTimeLineActivity.newIntent(getAccount());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case R.id.menu_topic:
                String ori = content.getText().toString();
                String topicTag = "##";
                content.setText(ori + topicTag);
                content.setSelection(content.getText().toString().length() - 1);
                break;
            case R.id.menu_at:
                intent = new Intent(WriteWeiboActivity.this, AtUserActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, AT_USER);
                break;
            case R.id.menu_txt_to_pic:
                convertStringToBitmap();
                break;
            case R.id.menu_clear:
                clearContentMenu();
                break;
            case R.id.menu_add_gps:
                addLocation();
                break;
            case R.id.menu_add_now_playing:
                MusicInfo musicInfo = GlobalContext.getInstance().getMusicInfo();
                if (!musicInfo.isEmpty()) {
                    content.append(musicInfo.toString());
                }
                break;
        }
        return true;
    }

    public void saveToDraft() {
        if (!TextUtils.isEmpty(content.getText().toString())) {
            boolean haveDraft = statusDraftBean != null;
            boolean isDraftChanged = haveDraft && !statusDraftBean.getContent()
                    .equals(content.getText().toString());

            if (isDraftChanged) {
                DraftDBManager.getInstance().remove(statusDraftBean.getId());
                DraftDBManager.getInstance()
                        .insertStatus(content.getText().toString(), geoBean, picPath,
                                accountBean.getUid());
            } else {
                DraftDBManager.getInstance()
                        .insertStatus(content.getText().toString(), geoBean, picPath,
                                accountBean.getUid());
            }
        }
        finish();
    }

    private void convertStringToBitmap() {
        boolean haveContent = !TextUtils.isEmpty(content.getText().toString());

        if (!haveContent) {
            content.setError(getString(R.string.content_cant_be_empty));
            return;
        }
        if (Utility.isTaskStopped(string2PicTask)) {
            string2PicTask = new String2PicTask();
            string2PicTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    protected void clearContentMenu() {
        ClearContentDialog dialog = new ClearContentDialog();
        dialog.show(getFragmentManager(), "");
    }

    public void clear() {
        content.setText("");
    }

    private void send() {
        String value = content.getText().toString();
        if (canSend()) {
            executeTask(value);
        }
    }

    private void addPic() {
        SelectPictureDialog.newInstance().show(getFragmentManager(), "");
    }

    private void showPic() {
        DeleteSelectedPictureDialog.newInstance().show(getFragmentManager(), "");
    }

    protected void executeTask(String contentString) {
        Intent intent = new Intent(WriteWeiboActivity.this, SendWeiboService.class);
        intent.putExtra("token", token);
        intent.putExtra("picPath", picPath);
        intent.putExtra("account", accountBean);
        intent.putExtra("content", contentString);
        intent.putExtra("geo", geoBean);
        intent.putExtra("draft", statusDraftBean);
        startService(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationListener != null) {
            ((LocationManager) WriteWeiboActivity.this
                    .getSystemService(Context.LOCATION_SERVICE)).removeUpdates(locationListener);
        }
    }

    public AccountBean getAccount() {
        return accountBean;
    }

    private class BottomButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_add_gps:
                    addLocation();
                    break;
                case R.id.menu_add_pic:
                    if (TextUtils.isEmpty(picPath)) {
                        addPic();
                    } else {
                        showPic();
                    }
                    break;
                case R.id.menu_emoticon:
                    if (smiley.isShown()) {
                        hideSmileyPicker(true);
                    } else {
                        showSmileyPicker(
                                SmileyPickerUtility.isKeyBoardShow(WriteWeiboActivity.this));
                    }
                    break;

                case R.id.menu_send:
                    send();
                    break;
                case R.id.menu_at:
                    Intent intent = new Intent(WriteWeiboActivity.this, AtUserActivity.class);
                    intent.putExtra("token", token);
                    startActivityForResult(intent, AT_USER);
                    break;
            }
        }
    }

    private void showSmileyPicker(boolean showAnimation) {
        this.smiley.show(WriteWeiboActivity.this, showAnimation);
        lockContainerHeight(SmileyPickerUtility.getAppContentHeight(WriteWeiboActivity.this));
    }

    public void hideSmileyPicker(boolean showKeyBoard) {
        if (this.smiley.isShown()) {
            if (showKeyBoard) {
                //this time softkeyboard is hidden
                LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this
                        .container.getLayoutParams();
                localLayoutParams.height = smiley.getTop();
                localLayoutParams.weight = 0.0F;
                this.smiley.hide(WriteWeiboActivity.this);

                SmileyPickerUtility.showKeyBoard(content);
                content.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        unlockContainerHeightDelayed();
                    }
                }, 200L);
            } else {
                this.smiley.hide(WriteWeiboActivity.this);
                unlockContainerHeightDelayed();
            }
        }
    }

    private void lockContainerHeight(int paramInt) {
        LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this.container
                .getLayoutParams();
        localLayoutParams.height = paramInt;
        localLayoutParams.weight = 0.0F;
    }

    public void unlockContainerHeightDelayed() {
        ((LinearLayout.LayoutParams) WriteWeiboActivity.this.container.getLayoutParams()).weight
                = 1.0F;
    }

    private void addLocation() {
        LocationManager locationManager = (LocationManager) WriteWeiboActivity.this
                .getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(WriteWeiboActivity.this, getString(R.string.please_open_gps),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(WriteWeiboActivity.this, getString(R.string.gps_is_searching),
                Toast.LENGTH_SHORT).show();

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,
                    locationListener);
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
                    locationListener);
        }
    }

    private void updateWithNewLocation(Location result) {
        haveGPS.setVisibility(View.VISIBLE);
        geoBean = new GeoBean();
        geoBean.setLatitude(result.getLatitude());
        geoBean.setLongitude(result.getLongitude());
        if (Utility.isTaskStopped(locationTask)) {
            locationTask = new GetGoogleLocationInfo(geoBean);
            locationTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
        ((LocationManager) WriteWeiboActivity.this
                .getSystemService(Context.LOCATION_SERVICE)).removeUpdates(locationListener);
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status,
                Bundle extras) {
        }
    };

    private class GetGoogleLocationInfo extends MyAsyncTask<Void, String, String> {

        GeoBean geoBean;

        public GetGoogleLocationInfo(GeoBean geoBean) {
            this.geoBean = geoBean;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = new GoogleGeoCoderDao(WriteWeiboActivity.this, geoBean).get();

            if (!TextUtils.isEmpty(result)) {
                return result;
            }

            try {
                result = new BaiduGeoCoderDao(geoBean.getLat(), geoBean.getLon()).get();
                if (!TextUtils.isEmpty(result)) {
                    return result;
                }
            } catch (WeiboException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!TextUtils.isEmpty(result)) {
                Toast.makeText(WriteWeiboActivity.this, result, Toast.LENGTH_SHORT).show();
                location = result;
            }
            enableGeo();
        }
    }

    private void enableGeo() {
        haveGPS.setVisibility(View.VISIBLE);
        CheatSheet.setup(WriteWeiboActivity.this, haveGPS, location);
    }

    private void disableGeo() {
        haveGPS.setVisibility(View.GONE);
    }

    private class String2PicTask extends MyAsyncTask<Void, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            content.destroyDrawingCache();
            content.buildDrawingCache();
        }

        @Override
        protected String doInBackground(Void... params) {
            return ImageEditUtility.convertStringToBitmap(WriteWeiboActivity.this, content);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!TextUtils.isEmpty(s)) {
                imageFileUri = Uri.fromFile(new File(s));
                enablePicture();
                Toast.makeText(WriteWeiboActivity.this, getString(R.string.convert_successfully),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WriteWeiboActivity.this, getString(R.string.convert_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deletePicture() {
        imageFileUri = null;
        picPath = null;
        disablePicture();
    }
}
