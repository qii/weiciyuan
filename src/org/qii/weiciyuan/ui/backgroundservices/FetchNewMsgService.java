package org.qii.weiciyuan.ui.backgroundservices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.dao.CommentsTimeLineMsgDao;
import org.qii.weiciyuan.dao.MentionsTimeLineMsgDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 * Time: 上午9:04
 */
public class FetchNewMsgService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new SimpleTask().execute();

        return super.onStartCommand(intent, flags, startId);
    }


    class SimpleTask extends AsyncTask<Void, Void, Integer> {

        AccountBean accountBean;

        @Override
        protected Integer doInBackground(Void... params) {

            List<AccountBean> accountBeans = DatabaseManager.getInstance().getAccountList();
            accountBean=accountBeans.get(0);
            String accountId = accountBean.getUid();
            String token = accountBean.getAccess_token();

            CommentListBean commentLineBean = DatabaseManager.getInstance().getCommentLineMsgList(accountId);
            MessageListBean messageListBean = DatabaseManager.getInstance().getRepostLineMsgList(accountId);

            CommentsTimeLineMsgDao dao = new CommentsTimeLineMsgDao(token);
            if (commentLineBean.getComments().size() > 0) {
                dao.setSince_id(commentLineBean.getComments().get(0).getId());
            }
            CommentListBean result = dao.getGSONMsgList();


            MentionsTimeLineMsgDao mentionsTimeLineMsgDao = new MentionsTimeLineMsgDao(token);
            if (messageListBean.getStatuses().size() > 0) {
                dao.setSince_id(messageListBean.getStatuses().get(0).getId());
            }
            MessageListBean messageListBeanResult = mentionsTimeLineMsgDao.getGSONMsgList();

            return result.getComments().size() + messageListBeanResult.getStatuses().size();
        }

        @Override
        protected void onPostExecute(Integer sum) {
            if (sum > 0) {
                showNotification(sum);
            }
            super.onPostExecute(sum);
        }

        private void showNotification(Integer aVoid) {
            Intent intent = new Intent(FetchNewMsgService.this, MainTimeLineActivity.class);
            intent.putExtra("account",accountBean);
            PendingIntent activity = PendingIntent.getActivity(FetchNewMsgService.this, 0, intent, 0);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification.Builder(FetchNewMsgService.this)
                    .setContentText("new message" + aVoid)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(activity)
                    .getNotification();

            notificationManager.notify(0, notification);


        }
    }


}
