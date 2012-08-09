package org.qii.weiciyuan.ui.send;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 * Time: 下午3:53
 */
public abstract class AbstractSendActivity extends AbstractAppActivity implements DialogInterface.OnClickListener{
    private static final int CAMERA_RESULT = 0;
    private static final int PIC_RESULT = 1;
    protected String token = "";

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

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Bitmap bmp = (Bitmap) extras.get("data");

//                imv = (ImageView) findViewById(R.id.ReturnedImageView);
//                imv.setImageBitmap(bmp);

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statusnewactivity_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        token = intent.getStringExtra("token");

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
                new MyAlertDialogFragment().show(getFragmentManager(), "");
                break;

            case R.id.menu_send:

                final String content = ((EditText) findViewById(R.id.status_new_content)).getText().toString();

                if (!TextUtils.isEmpty(content)) {


                        executeTask(content);

                }
                break;
        }
        return true;
    }

    class ProgressFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("发送中");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            return dialog;
        }
    }

    class MyAlertDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String[] items = {getString(R.string.take_camera), getString(R.string.select_pic)};

            AlertDialog.Builder builder = new AlertDialog.Builder(AbstractSendActivity.this)
                    .setTitle(getString(R.string.select))
                    .setItems(items, AbstractSendActivity.this);
            return builder.create();
        }
    }

    protected abstract void executeTask(String content);
}
