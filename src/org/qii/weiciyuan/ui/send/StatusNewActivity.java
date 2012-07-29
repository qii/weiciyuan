package org.qii.weiciyuan.ui.send;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.dao.StatusNewMsg;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午2:02
 * To change this template use File | Settings | File Templates.
 */
public class StatusNewActivity extends Activity {

    private static final int CAMERA_RESULT = 0;

    private static final int PIC_RESULT = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statusnewactivity_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.statusnewactivity_menu, menu);
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

                break;
            case R.id.menu_add_pic:
                MyAlertDialogFragment myAlertDialogFragment = MyAlertDialogFragment.newInstance();
                myAlertDialogFragment.show(getFragmentManager(), "");
                break;

            case R.id.menu_send:

                final String content = ((EditText) findViewById(R.id.status_new_content)).getText().toString();

                if (!TextUtils.isEmpty(content)) {

                    new StatusNewTask(content).execute();


                }
                break;
        }
        return true;
    }

    static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance() {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            frag.setRetainInstance(true);
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String[] items = {getString(R.string.take_camera), getString(R.string.select_pic)};


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.select))
                    .setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which) {
                                case 0:
                                    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(i, CAMERA_RESULT);
                                    break;
                                case 1:
                                    Intent choosePictureIntent = new Intent(Intent.ACTION_PICK,
                                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(choosePictureIntent, PIC_RESULT);
                                    break;
                            }

                        }
                    });


            return builder.create();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Bitmap bmp = (Bitmap) extras.get("data");

//                imv = (ImageView) findViewById(R.id.ReturnedImageView);
//                imv.setImageBitmap(bmp);

        }
    }


    class StatusNewTask extends AsyncTask<Void, String, String> {
        String content;

        StatusNewTask(String content) {
            this.content = content;
        }

        ProgressFragment progressFragment = ProgressFragment.newInstance();

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
            new StatusNewMsg().sendNewMsg(content);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressFragment.dismissAllowingStateLoss();
            finish();
            Toast.makeText(StatusNewActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
            super.onPostExecute(s);

        }
    }

    static class ProgressFragment extends DialogFragment {

        public static ProgressFragment newInstance() {
            ProgressFragment frag = new ProgressFragment();
            frag.setRetainInstance(true); //注意这句
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("发送中");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);


            return dialog;
        }
    }
}
