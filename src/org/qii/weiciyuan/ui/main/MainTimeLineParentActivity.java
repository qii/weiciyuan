package org.qii.weiciyuan.ui.main;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.view.ViewConfiguration;
import android.widget.Toast;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import org.qii.weiciyuan.support.asyncdrawable.TimeLineBitmapDownloader;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * User: qii
 * Date: 13-1-22
 */
public class MainTimeLineParentActivity extends SlidingFragmentActivity {

    private int theme = 0;


    @Override
    protected void onResume() {
        super.onResume();
        GlobalContext.getInstance().setCurrentRunningActivity(this);


        if (theme == SettingUtility.getAppTheme()) {

        } else {
            reload();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (GlobalContext.getInstance().getCurrentRunningActivity() == this) {
            GlobalContext.getInstance().setCurrentRunningActivity(null);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", theme);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            theme = SettingUtility.getAppTheme();
        } else {
            theme = savedInstanceState.getInt("theme");
        }
        setTheme(theme);
        super.onCreate(savedInstanceState);
        forceShowActionBarOverflowMenu();
        initNFC();
        GlobalContext.getInstance().setActivity(this);
        TimeLineBitmapDownloader.refreshThemePictureBackground();
    }


    private void forceShowActionBarOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private void initNFC() {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            return;
        }

        mNfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                String text = (GlobalContext.getInstance().getCurrentAccountName());

                NdefMessage msg = new NdefMessage(
                        new NdefRecord[]{createMimeRecord(
                                "application/org.qii.weiciyuan.beam", text.getBytes()), NdefRecord.createApplicationRecord(getPackageName())
                        });
                return msg;
            }
        }, this);

    }

    private NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

    public void reload() {

        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }


    protected void dealWithException(WeiboException e) {
        Toast.makeText(this, e.getError(), Toast.LENGTH_SHORT).show();
    }
}
