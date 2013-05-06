package org.qii.weiciyuan.othercomponent;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.android.CommentTimeLineData;
import org.qii.weiciyuan.bean.android.MentionTimeLineData;
import org.qii.weiciyuan.dao.maintimeline.MainCommentsTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MainMentionsTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MentionsCommentTimeLineDao;
import org.qii.weiciyuan.dao.unread.UnreadDao;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.database.CommentsTimeLineDBTask;
import org.qii.weiciyuan.support.database.MentionCommentsTimeLineDBTask;
import org.qii.weiciyuan.support.database.MentionsTimeLineDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppEventAction;
import org.qii.weiciyuan.support.utils.AppLogger;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class FetchNewMsgService extends Service {

    //close service between 1 clock and 8 clock
    private static final int NIGHT_START_TIME_HOUR = 1;
    private static final int NIGHT_END_TIME_HOUR = 7;

    private ArrayList<FetchMsgTask> tasks = new ArrayList<FetchMsgTask>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (SettingUtility.disableFetchAtNight() && isNowNight()) {
            stopSelf();
        } else {
            startFetchNewMsg();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean isNowNight() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= NIGHT_START_TIME_HOUR && hour <= NIGHT_END_TIME_HOUR;
    }

    private void startFetchNewMsg() {
        new GetAccountDBTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class GetAccountDBTask extends MyAsyncTask<Void, Void, List<AccountBean>> {

        @Override
        protected List<AccountBean> doInBackground(Void... params) {
            List<AccountBean> accountBeanList = AccountDBTask.getAccountList();
            if (accountBeanList.size() > 0) {
                return accountBeanList;
            } else {
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(List<AccountBean> accountBeans) {
            super.onCancelled(accountBeans);
            stopSelf();
        }

        @Override
        protected void onPostExecute(List<AccountBean> accountBeans) {
            for (AccountBean account : accountBeans) {
                FetchMsgTask task = new FetchMsgTask(account);
                tasks.add(task);
            }

            for (FetchMsgTask task : tasks) {
                task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }


    class FetchMsgTask extends MyAsyncTask<Void, Void, Void> {
        WeiboException e;
        AccountBean accountBean;
        CommentListBean commentResult;
        MessageListBean mentionStatusesResult;
        CommentListBean mentionCommentsResult;
        UnreadBean unreadBean;

        public FetchMsgTask(AccountBean bean) {
            accountBean = bean;
        }

        @Override
        protected Void doInBackground(Void... params) {

            String token = accountBean.getAccess_token();
            try {
                UnreadDao unreadDao = new UnreadDao(token, accountBean.getUid());
                unreadBean = unreadDao.getCount();
                if (unreadBean == null) {
                    cancel(true);
                    return null;
                }
                int unreadCommentCount = unreadBean.getCmt();
                int unreadMentionStatusCount = unreadBean.getMention_status();
                int unreadMentionCommentCount = unreadBean.getMention_cmt();

                if (unreadCommentCount > 0 && SettingUtility.allowCommentToMe()) {
                    MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(token);
                    CommentListBean oldData = null;
                    CommentTimeLineData commentTimeLineData = CommentsTimeLineDBTask.getCommentLineMsgList(accountBean.getUid());
                    if (commentTimeLineData != null) {
                        oldData = commentTimeLineData.cmtList;
                    }
                    if (oldData != null && oldData.getSize() > 0) {
                        dao.setSince_id(oldData.getItem(0).getId());
                    }
                    commentResult = dao.getGSONMsgListWithoutClearUnread();
                }

                if (unreadMentionStatusCount > 0 && SettingUtility.allowMentionToMe()) {
                    MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(token);
                    MessageListBean oldData = null;
                    MentionTimeLineData commentTimeLineData = MentionsTimeLineDBTask.getRepostLineMsgList(accountBean.getUid());
                    if (commentTimeLineData != null) {
                        oldData = commentTimeLineData.msgList;
                    }
                    if (oldData != null && oldData.getSize() > 0) {
                        dao.setSince_id(oldData.getItem(0).getId());
                    }
                    mentionStatusesResult = dao.getGSONMsgListWithoutClearUnread();
                }

                if (unreadMentionCommentCount > 0 && SettingUtility.allowMentionCommentToMe()) {
                    MainCommentsTimeLineDao dao = new MentionsCommentTimeLineDao(token);
                    CommentListBean oldData = null;
                    CommentTimeLineData commentTimeLineData = MentionCommentsTimeLineDBTask.getCommentLineMsgList(accountBean.getUid());
                    if (commentTimeLineData != null) {
                        oldData = commentTimeLineData.cmtList;
                    }
                    if (oldData != null && oldData.getSize() > 0) {
                        dao.setSince_id(oldData.getItem(0).getId());
                    }
                    mentionCommentsResult = dao.getGSONMsgListWithoutClearUnread();
                }

            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
            if (unreadBean == null) {
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void sum) {
            boolean mentionsWeibo = (mentionStatusesResult != null && mentionStatusesResult.getSize() > 0);
            boolean menttinosComment = (mentionCommentsResult != null && mentionCommentsResult.getSize() > 0);
            boolean commentsToMe = (commentResult != null && commentResult.getSize() > 0);
            if (mentionsWeibo || menttinosComment || commentsToMe) {
                sendNewMsgBroadcast();
            }
            checkForFinish();
            super.onPostExecute(sum);
        }

        @Override
        protected void onCancelled(Void stringIntegerMap) {
            checkForFinish();
            super.onCancelled(stringIntegerMap);
        }

        private void sendNewMsgBroadcast() {

            Intent intent = new Intent(AppEventAction.NEW_MSG_PRIORITY_BROADCAST);
            intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
            intent.putExtra(BundleArgsConstants.COMMENTS_TO_ME_EXTRA, commentResult);
            intent.putExtra(BundleArgsConstants.MENTIONS_WEIBO_EXTRA, mentionStatusesResult);
            intent.putExtra(BundleArgsConstants.MENTIONS_COMMENT_EXTRA, mentionCommentsResult);
            intent.putExtra(BundleArgsConstants.UNREAD_EXTRA, unreadBean);
            sendOrderedBroadcast(intent, null);

            intent.setAction(AppEventAction.NEW_MSG_BROADCAST);
            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
        }

        private void checkForFinish() {
            tasks.remove(FetchMsgTask.this);
            if (tasks.size() == 0) {
                stopSelf();
                AppLogger.d("stop fetchnewmsgservice");
            }
        }
    }

}
