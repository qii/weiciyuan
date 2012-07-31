package org.qii.weiciyuan.ui;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 * Time: 上午9:30
 */
public class AbstractMainActivity extends FragmentActivity {

    private boolean isStartThirdAppActivity = false;

    public boolean isStartThirdAppActivity() {
        return isStartThirdAppActivity;
    }

    public void setStartThirdAppActivityFlag() {
        isStartThirdAppActivity = true;
    }

    public void removeStartThirdAppActivityFlag() {
        isStartThirdAppActivity = false;
    }

    @Override
    public void startActivity(Intent intent) {
        GlobalContext.getInstance().setAppForegroundFlag();
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        GlobalContext.getInstance().setAppForegroundFlag();
        setStartThirdAppActivityFlag();
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        removeStartThirdAppActivityFlag();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        GlobalContext.getInstance().setAppForegroundFlag();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        if (!isStartThirdAppActivity()) {
            if (GlobalContext.getInstance().isAppForeground()) {
                GlobalContext.getInstance().removeAppForegroundFlag();
            } else {
                Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
            }
        }

        super.onStop();
    }

}
