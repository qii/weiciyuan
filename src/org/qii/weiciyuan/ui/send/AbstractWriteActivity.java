package org.qii.weiciyuan.ui.send;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.file.FileLocationMethod;
import org.qii.weiciyuan.support.file.FileManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.maintimeline.SaveDraftDialog;
import org.qii.weiciyuan.ui.search.AtUserActivity;
import org.qii.weiciyuan.ui.widgets.SendProgressFragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * User: qii
 * Date: 12-9-25
 */
public abstract class AbstractWriteActivity<T> extends AbstractAppActivity implements View.OnClickListener, ClearContentDialog.IClear
        , EmotionsGridDialog.IEmotions, SaveDraftDialog.IDraft {

    private SimpleTask task;
    protected GetEmotionsTask getEmotionsTask;
    private Map<String, Bitmap> emotionsPic = new HashMap<String, Bitmap>();

    protected abstract T sendData() throws WeiboException;

    protected abstract boolean canSend();

    private EditText et;

    public static final int AT_USER = 3;

    protected String token;


    protected EditText getEditTextView() {
        return et;
    }


    @Override
    public void clear() {
        getEditTextView().setText("");
    }

    protected void send() {
        if (canSend()) {
            if (task == null || task.getStatus() == MyAsyncTask.Status.FINISHED) {
                task = new SimpleTask();
                task.execute();
            }
        }
    }

    @Override
    public void insertEmotion(String emotionChar) {
        String ori = getEditTextView().getText().toString();
        int index = getEditTextView().getSelectionStart();
        StringBuilder stringBuilder = new StringBuilder(ori);
        stringBuilder.insert(index, emotionChar);
        getEditTextView().setText(stringBuilder.toString());
        getEditTextView().setSelection(index + emotionChar.length());
    }

    public Map<String, Bitmap> getEmotionsPic() {
        return emotionsPic;
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repostnewactivity_layout);

        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        token = getIntent().getStringExtra("token");


        et = ((EditText) findViewById(R.id.status_new_content));
        et.addTextChangedListener(new TextNumLimitWatcher((TextView) findViewById(R.id.menu_send), et, this));


        findViewById(R.id.menu_topic).setOnClickListener(this);
        findViewById(R.id.menu_at).setOnClickListener(this);
        findViewById(R.id.menu_add_emotions).setOnClickListener(this);
        findViewById(R.id.menu_send).setOnClickListener(this);

        if (getEmotionsTask == null || getEmotionsTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            getEmotionsTask = new GetEmotionsTask();
            getEmotionsTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_add_emotions:
                EmotionsGridDialog dialog = new EmotionsGridDialog();
                dialog.show(getFragmentManager(), "");
                break;

            case R.id.menu_send:
                send();
                break;
            case R.id.menu_topic:
                String ori = getEditTextView().getText().toString();
                String topicTag = "##";
                getEditTextView().setText(ori + topicTag);
                getEditTextView().setSelection(et.getText().toString().length() - 1);
                break;
            case R.id.menu_at:
                Intent intent = new Intent(AbstractWriteActivity.this, AtUserActivity.class);
                intent.putExtra("token", token);
                startActivityForResult(intent, AT_USER);
                break;
        }
    }

    protected void clearContentMenu() {
        ClearContentDialog dialog = new ClearContentDialog();
        dialog.show(getFragmentManager(), "");
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(et.getText().toString()) && canShowSaveDraftDialog()) {
            SaveDraftDialog dialog = new SaveDraftDialog();
            dialog.show(getFragmentManager(), "");
        } else {
            super.onBackPressed();
        }
    }

    protected abstract boolean canShowSaveDraftDialog();

    public abstract void saveToDraft();

    protected abstract void removeDraft();

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AT_USER:
                    String name = intent.getStringExtra("name");
                    String ori = getEditTextView().getText().toString();
                    int index = getEditTextView().getSelectionStart();
                    StringBuilder stringBuilder = new StringBuilder(ori);
                    stringBuilder.insert(index, name);
                    getEditTextView().setText(stringBuilder.toString());
                    getEditTextView().setSelection(index + name.length());
                    break;
            }

        }
    }


    private class SimpleTask extends MyAsyncTask<Void, Void, T> {

        SendProgressFragment progressFragment = new SendProgressFragment();
        WeiboException e;

        @Override
        protected void onPreExecute() {
            progressFragment.onCancel(new DialogInterface() {

                @Override
                public void cancel() {
                    SimpleTask.this.cancel(true);
                }

                @Override
                public void dismiss() {
                    SimpleTask.this.cancel(true);
                }
            });

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected T doInBackground(Void... params) {

            try {
                return sendData();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(T commentBean) {
            super.onCancelled(commentBean);
            if (this.e != null) {
                Toast.makeText(AbstractWriteActivity.this, e.getError(), Toast.LENGTH_SHORT).show();

            }

            if (progressFragment != null)
                progressFragment.dismissAllowingStateLoss();
        }


        @Override
        protected void onPostExecute(T s) {
            progressFragment.dismissAllowingStateLoss();
            if (s != null) {
                removeDraft();
                Toast.makeText(AbstractWriteActivity.this, getString(R.string.send_successfully), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AbstractWriteActivity.this, getString(R.string.send_failed), Toast.LENGTH_SHORT).show();

            }
            super.onPostExecute(s);

        }
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
}
