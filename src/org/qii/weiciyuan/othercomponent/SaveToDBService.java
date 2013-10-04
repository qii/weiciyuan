package org.qii.weiciyuan.othercomponent;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.debug.AppLogger;

import java.io.Serializable;

/**
 * User: qii
 * Date: 12-12-16
 */
@Deprecated
public class SaveToDBService extends IntentService {

    public static final int TYPE_STATUS = 0;

    public SaveToDBService() {
        super("SaveToDBService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int type = intent.getIntExtra("type", 0);
        String accountId = intent.getStringExtra("accountId");
        switch (type) {
            case TYPE_STATUS:
                AppLogger.e("start db");
                MessageListBean value = (MessageListBean) intent.getParcelableExtra("value");
//                FriendsTimeLineDBTask.replace(value, accountId);
                AppLogger.e("end db");
                break;
        }

    }

    public static void save(Context context, int type, Serializable value, String accountId) {
        AppLogger.e("start service");
        Intent intent = new Intent(context, SaveToDBService.class);
        intent.putExtra("type", type);
        intent.putExtra("value", value);
        intent.putExtra("accountId", accountId);
        context.startService(intent);
        AppLogger.e("start service end");
    }
}
