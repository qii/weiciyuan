package org.qii.weiciyuan.ui.send;

import android.app.ActionBar;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.*;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.othercomponent.SendWeiboService;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.database.DraftDBManager;
import org.qii.weiciyuan.support.database.draftbean.StatusDraftBean;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.maintimeline.SaveDraftDialog;
import org.qii.weiciyuan.ui.search.AtUserActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * User: qii
 * Date: 12-7-29
 */
public class WriteWeiboActivity extends AbstractAppActivity implements DialogInterface.OnClickListener,
        IAccountInfo, ClearContentDialog.IClear, EmotionsGridDialog.IEmotions, SaveDraftDialog.IDraft {

    private static final int CAMERA_RESULT = 0;
    private static final int PIC_RESULT = 1;
    public static final int AT_USER = 3;

    private AccountBean accountBean;
    protected String token = "";
    private StatusDraftBean statusDraftBean;

    private String picPath = "";
    private Uri imageFileUri = null;
    private GeoBean geoBean;
    private String location;

    private ImageView haveGPS = null;
    private ImageView havePic = null;
    private EditText content = null;

    private GetEmotionsTask getEmotionsTask;
    private Map<String, Bitmap> emotionsPic = new HashMap<String, Bitmap>();


    @Override
    public void onClick(DialogInterface dialog, int which) {

        switch (which) {
            case 0:

                imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new ContentValues());
                Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
                startActivityForResult(i, CAMERA_RESULT);
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
                    if (TextUtils.isEmpty(content.getText().toString())) {
                        content.setText(getString(R.string.share_pic));
                        content.setSelection(content.getText().toString().length());
                    }

                    picPath = getPicPathFromUri(imageFileUri);
                    havePic.setVisibility(View.VISIBLE);
                    break;
                case PIC_RESULT:
                    if (TextUtils.isEmpty(content.getText().toString())) {
                        content.setText(getString(R.string.share_pic));
                        content.setSelection(content.getText().toString().length());
                    }

                    Uri imageFileUri = intent.getData();
                    picPath = getPicPathFromUri(imageFileUri);
                    havePic.setVisibility(View.VISIBLE);
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
        if (getEmotionsTask != null)
            getEmotionsTask.cancel(true);

        Set<String> keys = emotionsPic.keySet();
        for (String key : keys) {
            emotionsPic.put(key, null);
        }
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

        if (!TextUtils.isEmpty(content.getText().toString()) && canShowSaveDraftDialog()) {
            SaveDraftDialog dialog = new SaveDraftDialog();
            dialog.show(getFragmentManager(), "");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statusnewactivity_layout);
        initLayout();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            } else if (type.startsWith("image/")) {
                handleSendImage(intent);
            }
        } else {
            handleNormalOperation(intent);
        }
        if (getEmotionsTask == null || getEmotionsTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            getEmotionsTask = new GetEmotionsTask();
            getEmotionsTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void handleNormalOperation(Intent intent) {
        accountBean = (AccountBean) intent.getSerializableExtra("account");
        token = accountBean.getAccess_token();
        getActionBar().setSubtitle(accountBean.getUsernick());
        String contentTxt = intent.getStringExtra("content");
        if (!TextUtils.isEmpty(contentTxt)) {
            content.setText(contentTxt + " ");
            content.setSelection(content.getText().toString().length());
        }

        statusDraftBean = (StatusDraftBean) intent.getSerializableExtra("draft");
        if (statusDraftBean != null) {
            content.setText(statusDraftBean.getContent());
            picPath = statusDraftBean.getPic();
            geoBean = statusDraftBean.getGps();

            if (!TextUtils.isEmpty(picPath))
                havePic.setVisibility(View.VISIBLE);

            if (geoBean != null)
                new GetGoogleLocationInfo(geoBean).execute();

        }
    }


    public Map<String, Bitmap> getEmotionsPic() {
        return emotionsPic;
    }


    private void initLayout() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.write_weibo);

        View title = getLayoutInflater().inflate(R.layout.statusnewactivity_title_layout, null);
        TextView contentNumber = (TextView) title.findViewById(R.id.content_number);
        contentNumber.setVisibility(View.GONE);
        haveGPS = (ImageView) title.findViewById(R.id.have_gps);
        haveGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String geoUriString = "geo:" + geoBean.getLat() + "," + geoBean.getLon() + "?q=" + location;
                Uri geoUri = Uri.parse(geoUriString);
                Intent mapCall = new Intent(Intent.ACTION_VIEW, geoUri);
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(mapCall, 0);
                boolean isIntentSafe = activities.size() > 0;
                if (isIntentSafe) {
                    startActivity(mapCall);
                }
            }
        });
        haveGPS.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                haveGPS.setVisibility(View.GONE);
                geoBean = null;
                return true;
            }
        });
        havePic = (ImageView) title.findViewById(R.id.have_pic);

        havePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(picPath)), "image/png");
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
                boolean isIntentSafe = activities.size() > 0;
                if (isIntentSafe) {
                    startActivity(intent);
                }
            }
        });

        havePic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                havePic.setVisibility(View.GONE);
                picPath = "";
                return true;
            }
        });
        actionBar.setCustomView(title, new ActionBar.LayoutParams(Gravity.RIGHT));
        actionBar.setDisplayShowCustomEnabled(true);
        content = ((EditText) findViewById(R.id.status_new_content));
        content.addTextChangedListener(new TextNumLimitWatcher((TextView) findViewById(R.id.menu_send), content, this));

        View.OnClickListener onClickListener = new BottomButtonClickListener();
        findViewById(R.id.menu_add_gps).setOnClickListener(onClickListener);
        findViewById(R.id.menu_add_pic).setOnClickListener(onClickListener);
        findViewById(R.id.menu_send).setOnClickListener(onClickListener);

        View.OnLongClickListener onLongClickListener = new BottomButtonLongClickListener();
        findViewById(R.id.menu_add_gps).setOnLongClickListener(onLongClickListener);
        findViewById(R.id.menu_add_pic).setOnLongClickListener(onLongClickListener);
        findViewById(R.id.menu_send).setOnLongClickListener(onLongClickListener);
    }


    private void getAccountInfo() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String id = sharedPref.getString("id", "");
        if (!TextUtils.isEmpty(id)) {
            accountBean = DatabaseManager.getInstance().getAccount(id);
            if (accountBean != null) {
                token = accountBean.getAccess_token();
                getActionBar().setSubtitle(accountBean.getUsernick());
            } else {
                List<AccountBean> accountList = DatabaseManager.getInstance().getAccountList();
                if (accountList != null && accountList.size() > 0) {
                    AccountBean account = accountList.get(0);
                    accountBean = account;
                    token = account.getAccess_token();
                    getActionBar().setSubtitle(account.getUsernick());
                }
            }
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
        getAccountInfo();

        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            picPath = getPicPathFromUri(imageUri);
            content.setText(getString(R.string.share_pic));
            content.setSelection(content.getText().toString().length());
            havePic.setVisibility(View.VISIBLE);
        }
    }

    private String getPicPathFromUri(Uri uri) {
        String value = uri.getPath();

        if (value.startsWith("/external")) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else {
            return value;
        }
    }

    private boolean canSend() {

        boolean haveContent = !TextUtils.isEmpty(content.getText().toString());
        boolean haveToken = !TextUtils.isEmpty(token);
        boolean contentNumBelow140 = (content.getText().toString().length() < 140);

        if (haveContent && haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveContent && !haveToken) {
                Toast.makeText(this, getString(R.string.content_cant_be_empty_and_dont_have_account), Toast.LENGTH_SHORT).show();
            } else if (!haveContent) {
                content.setError(getString(R.string.content_cant_be_empty));
            } else if (!haveToken) {
                Toast.makeText(this, getString(R.string.dont_have_account), Toast.LENGTH_SHORT).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                saveToDraft();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                intent = new Intent(this, MainTimeLineActivity.class);
                intent.putExtra("account", getAccount());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.menu_emoticon:
                EmotionsGridDialog dialog = new EmotionsGridDialog();
                dialog.show(getFragmentManager(), "");
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

        }
        return true;
    }


    public void saveToDraft() {
        if (!TextUtils.isEmpty(content.getText().toString())) {

            boolean haveDraft = statusDraftBean != null;
            boolean isDraftChanged = haveDraft && !statusDraftBean.getContent().equals(content.getText().toString());

            if (isDraftChanged) {
                DraftDBManager.getInstance().remove(statusDraftBean.getId());
                DraftDBManager.getInstance().insertStatus(content.getText().toString(), geoBean, picPath, accountBean.getUid());
            } else {
                DraftDBManager.getInstance().insertStatus(content.getText().toString(), geoBean, picPath, accountBean.getUid());

            }

        }
        finish();
    }

//    private String getLastContent() {
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        String draft = sharedPref.getString(TYPE_DRAFT, "");
//        return draft;
//    }

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
        new SelectPictureDialog().show(getFragmentManager(), "");
    }

    protected void executeTask(String content) {

        Intent intent = new Intent(WriteWeiboActivity.this, SendWeiboService.class);
        intent.putExtra("token", token);
        intent.putExtra("picPath", picPath);
        intent.putExtra("accountId", accountBean.getUid());
        intent.putExtra("content", content);
        intent.putExtra("geo", geoBean);
        intent.putExtra("draft", statusDraftBean);
        startService(intent);
        finish();
    }

    @Override
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
                    addPic();
                    break;

                case R.id.menu_add_emotions:
                    EmotionsGridDialog dialog = new EmotionsGridDialog();
                    dialog.show(getFragmentManager(), "");
                    break;

                case R.id.menu_send:
                    send();
                    break;
            }
        }
    }

    private class BottomButtonLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {
                case R.id.menu_add_gps:
                    Toast.makeText(WriteWeiboActivity.this, getString(R.string.add_gps), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_add_pic:
                    Toast.makeText(WriteWeiboActivity.this, getString(R.string.add_pic), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.menu_send:
                    Toast.makeText(WriteWeiboActivity.this, getString(R.string.send), Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    }

    public void insertEmotion(String emotionChar) {
        String ori = content.getText().toString();
        int index = content.getSelectionStart();
        StringBuilder stringBuilder = new StringBuilder(ori);
        stringBuilder.insert(index, emotionChar);
        content.setText(stringBuilder.toString());
        content.setSelection(index + emotionChar.length());
    }


    private void addLocation() {
        LocationManager locationManager = (LocationManager) WriteWeiboActivity.this
                .getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(WriteWeiboActivity.this, getString(R.string.gps_is_searching), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(WriteWeiboActivity.this, getString(R.string.please_open_gps), Toast.LENGTH_SHORT).show();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,
                locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
                locationListener);
    }


    private void updateWithNewLocation(Location result) {
        haveGPS.setVisibility(View.VISIBLE);
        geoBean = new GeoBean();
        geoBean.setLatitude(result.getLatitude());
        geoBean.setLongitude(result.getLongitude());
        new GetGoogleLocationInfo(geoBean).execute();
        ((LocationManager) WriteWeiboActivity.this
                .getSystemService(Context.LOCATION_SERVICE)).removeUpdates(locationListener);

    }

    private class GetEmotionsTask extends MyAsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Map<String, String> emotions = GlobalContext.getInstance().getEmotions();
            List<String> index = new ArrayList<String>();
            index.addAll(emotions.keySet());
            for (String str : index) {
                if (!isCancelled()) {
                    String url = emotions.get(str);
                    String path = FileManager.getFilePathFromUrl(url, FileLocationMethod.emotion);
                    String name = new File(path).getName();
                    AssetManager assetManager = GlobalContext.getInstance().getAssets();
                    InputStream inputStream;
                    try {
                        inputStream = assetManager.open(name);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        emotionsPic.put(str, bitmap);
                    } catch (IOException ignored) {

                    }
                }
            }
            return null;
        }
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

            Geocoder geocoder = new Geocoder(WriteWeiboActivity.this, Locale.getDefault());

            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(geoBean.getLat(), geoBean.getLon(), 1);
            } catch (IOException e) {
                cancel(true);
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                StringBuilder builder = new StringBuilder();
                int size = address.getMaxAddressLineIndex();
                for (int i = 0; i < size; i++) {
                    builder.append(address.getAddressLine(i));
                }
                return builder.toString();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(WriteWeiboActivity.this, s, Toast.LENGTH_SHORT).show();
            location = s;
            haveGPS.setVisibility(View.VISIBLE);
            super.onPostExecute(s);
        }
    }
}
