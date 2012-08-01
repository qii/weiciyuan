package org.qii.weiciyuan.ui.backgroundservices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 * Time: 上午9:04
 */
public class MentionsAndCommentsTimeLineService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);
    }


}
