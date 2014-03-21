package org.qii.weiciyuan.othercomponent;

import org.qii.weiciyuan.bean.AccountBean;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.bean.android.CommentTimeLineData;
import org.qii.weiciyuan.bean.android.MentionTimeLineData;
import org.qii.weiciyuan.dao.maintimeline.MainCommentsTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MentionsCommentTimeLineDao;
import org.qii.weiciyuan.dao.maintimeline.MentionsWeiboTimeLineDao;
import org.qii.weiciyuan.dao.unread.UnreadDao;
import org.qii.weiciyuan.othercomponent.unreadnotification.NotificationServiceHelper;
import org.qii.weiciyuan.support.database.AccountDBTask;
import org.qii.weiciyuan.support.database.CommentToMeTimeLineDBTask;
import org.qii.weiciyuan.support.database.MentionCommentsTimeLineDBTask;
import org.qii.weiciyuan.support.database.MentionWeiboTimeLineDBTask;
import org.qii.weiciyuan.support.database.NotificationDBTask;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;
import org.qii.weiciyuan.support.utils.AppEventAction;
import org.qii.weiciyuan.support.utils.BundleArgsConstants;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Calendar;
import java.util.List;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */
public class FetchNewMsgService extends IntentService {

    //close service between 1 clock and 8 clock
    private static final int NIGHT_START_TIME_HOUR = 1;

    private static final int NIGHT_END_TIME_HOUR = 7;


    public FetchNewMsgService() {
        super("FetchNewMsgService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (SettingUtility.disableFetchAtNight() && isNowNight()) {
            return;
        }

        List<AccountBean> accountBeanList = AccountDBTask.getAccountList();
        if (accountBeanList.size() == 0) {
            return;
        }
        for (AccountBean account : accountBeanList) {
            try {
                fetchMsg(account);
            } catch (WeiboException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isNowNight() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour >= NIGHT_START_TIME_HOUR && hour <= NIGHT_END_TIME_HOUR;
    }


    private void fetchMsg(AccountBean accountBean) throws WeiboException {
        CommentListBean commentResult = null;
        MessageListBean mentionStatusesResult = null;
        CommentListBean mentionCommentsResult = null;
        UnreadBean unreadBean = null;

        String token = accountBean.getAccess_token();

        UnreadDao unreadDao = new UnreadDao(token, accountBean.getUid());
        unreadBean = unreadDao.getCount();
        if (unreadBean == null) {
            return;
        }
        int unreadCommentCount = unreadBean.getCmt();
        int unreadMentionStatusCount = unreadBean.getMention_status();
        int unreadMentionCommentCount = unreadBean.getMention_cmt();

        if (unreadCommentCount > 0 && SettingUtility.allowCommentToMe()) {
            MainCommentsTimeLineDao dao = new MainCommentsTimeLineDao(token);
            CommentListBean oldData = null;
            CommentTimeLineData commentTimeLineData = CommentToMeTimeLineDBTask
                    .getCommentLineMsgList(accountBean.getUid());
            if (commentTimeLineData != null) {
                oldData = commentTimeLineData.cmtList;
            }
            if (oldData != null && oldData.getSize() > 0) {
                dao.setSince_id(oldData.getItem(0).getId());
            }
            commentResult = dao.getGSONMsgListWithoutClearUnread();
        }

        if (unreadMentionStatusCount > 0 && SettingUtility.allowMentionToMe()) {
            MentionsWeiboTimeLineDao dao = new MentionsWeiboTimeLineDao(token);
            MessageListBean oldData = null;
            MentionTimeLineData mentionStatusTimeLineData = MentionWeiboTimeLineDBTask
                    .getRepostLineMsgList(accountBean.getUid());
            if (mentionStatusTimeLineData != null) {
                oldData = mentionStatusTimeLineData.msgList;
            }
            if (oldData != null && oldData.getSize() > 0) {
                dao.setSince_id(oldData.getItem(0).getId());
            }
            mentionStatusesResult = dao.getGSONMsgListWithoutClearUnread();
        }

        if (unreadMentionCommentCount > 0 && SettingUtility.allowMentionCommentToMe()) {
            MainCommentsTimeLineDao dao = new MentionsCommentTimeLineDao(token);
            CommentListBean oldData = null;
            CommentTimeLineData commentTimeLineData = MentionCommentsTimeLineDBTask
                    .getCommentLineMsgList(accountBean.getUid());
            if (commentTimeLineData != null) {
                oldData = commentTimeLineData.cmtList;
            }
            if (oldData != null && oldData.getSize() > 0) {
                dao.setSince_id(oldData.getItem(0).getId());
            }
            mentionCommentsResult = dao.getGSONMsgListWithoutClearUnread();
        }

        clearDatabaseUnreadInfo(accountBean.getUid(), unreadBean.getMention_status(),
                unreadBean.getMention_cmt(), unreadBean.getCmt());

        boolean mentionsWeibo = (mentionStatusesResult != null
                && mentionStatusesResult.getSize() > 0);
        boolean mentionsComment = (mentionCommentsResult != null
                && mentionCommentsResult.getSize() > 0);
        boolean commentsToMe = (commentResult != null && commentResult.getSize() > 0);

        if (mentionsWeibo || mentionsComment || commentsToMe) {
            sendTwoKindsOfBroadcast(accountBean, commentResult, mentionStatusesResult,
                    mentionCommentsResult, unreadBean);
        } else {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(
                    NotificationServiceHelper.getMentionsWeiboNotificationId(accountBean));
        }
    }

    private void clearDatabaseUnreadInfo(String accountId, int mentionsWeibo, int mentionsComment,
            int cmt) {
        if (mentionsWeibo == 0) {
            NotificationDBTask
                    .asyncCleanUnread(accountId, NotificationDBTask.UnreadDBType.mentionsWeibo);
        }
        if (mentionsComment == 0) {
            NotificationDBTask
                    .asyncCleanUnread(accountId, NotificationDBTask.UnreadDBType.mentionsComment);
        }
        if (cmt == 0) {
            NotificationDBTask
                    .asyncCleanUnread(accountId, NotificationDBTask.UnreadDBType.commentsToMe);
        }
    }

    private void sendTwoKindsOfBroadcast(AccountBean accountBean,
            CommentListBean commentResult,
            MessageListBean mentionStatusesResult,
            CommentListBean mentionCommentsResult,
            UnreadBean unreadBean) {
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
}
