package org.qii.weiciyuan.othercomponent;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.dao.maintimeline.MainCommentsTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MainMentionsTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 * Time: 上午9:04
 */
public class FetchNewMsgService extends Service {
    CommentListBean commentResult;
    MessageListBean repostResult;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new SimpleTask().execute();

        return super.onStartCommand(intent, flags, startId);
    }


    class SimpleTask extends AsyncTask<Void, Void, Map<String, Integer>> {

        AccountBean accountBean;

        @Override
        protected Map<String, Integer> doInBackground(Void... params) {
            Map<String, Integer> map = new HashMap<String, Integer>();
            List<AccountBean> accountBeans = DatabaseManager.getInstance().getAccountList();
            accountBean = accountBeans.get(0);
            String accountId = accountBean.getUid();
            String token = accountBean.getAccess_token();

            CommentListBean commentLineBean = DatabaseManager.getInstance().getCommentLineMsgList(accountId);
            MessageListBean messageListBean = DatabaseManager.getInstance().getRepostLineMsgList(accountId);

            MainCommentsTimeLineDao commentDao = new MainCommentsTimeLineDao(token);
            if (commentLineBean.getComments().size() > 0) {
                commentDao.setSince_id(commentLineBean.getComments().get(0).getId());
            }
            commentResult = commentDao.getGSONMsgList();
            if (commentResult != null) {
                map.put("comment", commentResult.getComments().size());
            } else {
                cancel(true);
            }

            MainMentionsTimeLineDao mentionDao = new MainMentionsTimeLineDao(token);
            if (messageListBean.getStatuses().size() > 0) {
                mentionDao.setSince_id(messageListBean.getStatuses().get(0).getId());
            }
            repostResult = mentionDao.getGSONMsgList();
            if (repostResult != null) {
                map.put("repost", repostResult.getStatuses().size());
            } else {
                cancel(true);
            }
            return map;
        }

        @Override
        protected void onPostExecute(Map<String, Integer> sum) {

            showNotification(sum);
            stopSelf();
            super.onPostExecute(sum);
        }

        @Override
        protected void onCancelled(Map<String, Integer> stringIntegerMap) {
            stopSelf();
            super.onCancelled(stringIntegerMap);
        }

        private void showNotification(Map<String, Integer> sum) {

            Intent intent = new Intent(MentionsAndCommentsReceiver.ACTION);
            intent.putExtra("account", accountBean);
            intent.putExtra("commentsum", sum.get("comment"));
            intent.putExtra("repostsum", sum.get("repost"));
            intent.putExtra("comment", commentResult);
            intent.putExtra("repost", repostResult);
            sendOrderedBroadcast(intent, null);

        }
    }


}
