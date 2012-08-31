package org.qii.weiciyuan.ui.send;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.GeoBean;
import org.qii.weiciyuan.dao.location.LocationInfoDao;
import org.qii.weiciyuan.dao.send.StatusNewMsgDao;
import org.qii.weiciyuan.othercomponent.PhotoUploadService;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.widgets.SendProgressFragment;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * User: qii
 * Date: 12-7-29
 */
public class StatusNewActivity extends AbstractAppActivity implements DialogInterface.OnClickListener {


    private static final int CAMERA_RESULT = 0;
    private static final int PIC_RESULT = 1;
    protected String token = "";

    private String picPath = "";

    private String imageFilePath = "";

    private GeoBean geoBean;

    private Bitmap pic = null;

    private TextView contentNumber = null;
    private EditText content = null;

    private boolean canSend = false;


    @Override
    public void onClick(DialogInterface dialog, int which) {

        switch (which) {
            case 0:

                imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/myfavoritepicture.jpg";
                File imageFile = new File(imageFilePath);
                Uri imageFileUri = Uri.fromFile(imageFile);
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

            if (TextUtils.isEmpty(content.getText().toString())) {
                content.setText(getString(R.string.share_pic));
                content.setSelection(content.getText().toString().length());
            }

            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inSampleSize = 8;
            Bitmap bmp = null;
            switch (requestCode) {
                case CAMERA_RESULT:

                    bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);
                    pic = bmp;
                    invalidateOptionsMenu();
                    picPath = imageFilePath;

                    break;
                case PIC_RESULT:
                    Uri imageFileUri = intent.getData();

                    try {
                        bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileUri), null, bmpFactoryOptions);
                    } catch (FileNotFoundException e) {
                        AppLogger.e(e.getMessage());
                    }
                    pic = bmp;
                    invalidateOptionsMenu();
                    String[] proj = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(imageFileUri, proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();

                    picPath = cursor.getString(column_index);

                    break;


            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statusnewactivity_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.write_weibo);

        View title = getLayoutInflater().inflate(R.layout.statusnewactivity_title_layout, null);
        contentNumber = (TextView) title.findViewById(R.id.content_number);
        actionBar.setCustomView(title, new ActionBar.LayoutParams(Gravity.RIGHT));
        actionBar.setDisplayShowCustomEnabled(true);

        Intent intent = getIntent();
        token = intent.getStringExtra("token");

        content = ((EditText) findViewById(R.id.status_new_content));
        content.addTextChangedListener(onEditorActionListener);
    }

    private TextWatcher onEditorActionListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int num = 140 - content.getText().toString().length();
            contentNumber.setText(String.valueOf(num));
            if (num < 0) {
                canSend = false;
            } else {
                canSend = true;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.statusnewactivity_menu, menu);
//        if (pic != null) {
//            Drawable drawable = new BitmapDrawable(getResources(), pic);
//            menu.findItem(R.id.menu_add_pic).setIcon(drawable);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive())
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                onBackPressed();
                break;
            case R.id.menu_add_gps:
                getLocation();
                break;
            case R.id.menu_add_pic:
                new MyAlertDialogFragment().show(getFragmentManager(), "");
                break;

            case R.id.menu_send:
                String value = content.getText().toString();
                if (canSend && !TextUtils.isEmpty(value)) {

                    executeTask(value);

                }
                break;
        }
        return true;
    }

    protected void executeTask(String content) {

        if (TextUtils.isEmpty(picPath)) {
            new StatusNewTask(content).execute();
        } else {
            Intent intent = new Intent(StatusNewActivity.this, PhotoUploadService.class);
            intent.putExtra("token", token);
            intent.putExtra("picPath", picPath);
            intent.putExtra("content", content);
            intent.putExtra("geo", geoBean);
            startService(intent);
            finish();
        }
    }

    class StatusNewTask extends AsyncTask<Void, String, String> {
        String content;

        StatusNewTask(String content) {
            this.content = content;
        }

        SendProgressFragment progressFragment = new SendProgressFragment();

        @Override
        protected void onPreExecute() {
            progressFragment.onCancel(new DialogInterface() {

                @Override
                public void cancel() {
                    StatusNewTask.this.cancel(true);
                }

                @Override
                public void dismiss() {
                    StatusNewTask.this.cancel(true);
                }
            });

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected String doInBackground(Void... params) {
            boolean result = new StatusNewMsgDao(token).setGeoBean(geoBean).sendNewMsg(content);

            return null;
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            progressFragment.dismissAllowingStateLoss();
            Toast.makeText(StatusNewActivity.this, getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String s) {
            progressFragment.dismissAllowingStateLoss();
            finish();
            Toast.makeText(StatusNewActivity.this, getString(R.string.send_successfully), Toast.LENGTH_SHORT).show();
            super.onPostExecute(s);

        }
    }


    private class MyAlertDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String[] items = {getString(R.string.take_camera), getString(R.string.select_pic)};

            AlertDialog.Builder builder = new AlertDialog.Builder(StatusNewActivity.this)
                    .setTitle(getString(R.string.select))
                    .setItems(items, StatusNewActivity.this);
            return builder.create();
        }
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) StatusNewActivity.this
                .getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(StatusNewActivity.this, getString(R.string.gps_is_searching), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(StatusNewActivity.this, getString(R.string.please_open_gps), Toast.LENGTH_SHORT).show();
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,
                locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
                locationListener);
    }


    private void updateWithNewLocation(Location result) {
        geoBean = new GeoBean();
        geoBean.setLatitude(result.getLatitude());
        geoBean.setLongitude(result.getLongitude());
        new GetGoogleLocationInfo().execute();
        ((LocationManager) StatusNewActivity.this
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


    private class GetGoogleLocationInfo extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... params) {
            return new LocationInfoDao(geoBean).getInfo();
        }

        @Override
        protected void onPostExecute(String s) {
            contentNumber.setText(s);
            Toast.makeText(StatusNewActivity.this, s, Toast.LENGTH_SHORT).show();
            super.onPostExecute(s);
        }
    }
}
