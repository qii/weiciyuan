package org.qii.weiciyuan.support.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.CommentListBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.bean.UnreadBean;
import org.qii.weiciyuan.support.settinghelper.SettingUtility;

/**
 * User: qii
 * Date: 12-12-5
 */
public class NotificationUtility {

    private NotificationUtility() {
        // Forbidden being instantiated.
    }

    public static int getCount(UnreadBean unreadBean) {
        int count = 0;

        if (SettingUtility.allowMentionToMe()) {
            count += unreadBean.getMention_status();
        }

        if (SettingUtility.allowCommentToMe()) {
            count += unreadBean.getCmt();
        }

        if (SettingUtility.allowMentionCommentToMe()) {
            count += unreadBean.getMention_cmt();
        }

        return count;

    }

    @Deprecated
    public static String getTicker(UnreadBean unreadBean, MessageListBean mentionsWeibo, CommentListBean mentionsComment,
                                   CommentListBean commentsToMe) {
        int mentionCmt = unreadBean.getMention_cmt();
        int mentionStatus = unreadBean.getMention_status();
        int mention = 0;
        if (SettingUtility.allowMentionToMe() && mentionStatus > 0 && mentionsWeibo != null) {
            int actualFetchedSize = mentionsWeibo.getSize();
            if (actualFetchedSize >= Integer.valueOf(SettingUtility.getMsgCount())) {
                mention += mentionStatus;
            } else {
                mention += actualFetchedSize;
            }

        }
        if (SettingUtility.allowMentionCommentToMe() && mentionCmt > 0 && mentionsComment != null) {
            int actualFetchedSize = mentionsComment.getSize();
            if (actualFetchedSize >= Integer.valueOf(SettingUtility.getMsgCount())) {
                mention += mentionCmt;
            } else {
                mention += actualFetchedSize;
            }

        }

        StringBuilder stringBuilder = new StringBuilder();

        if (mention > 0) {
            String txt = String.format(GlobalContext.getInstance().getString(R.string.new_mentions), String.valueOf(mention));
            stringBuilder.append(txt);
        }


        int cmt = 0;

        if (SettingUtility.allowCommentToMe() && unreadBean.getCmt() > 0 && commentsToMe != null) {

            int actualFetchedSize = commentsToMe.getSize();
            if (actualFetchedSize >= Integer.valueOf(SettingUtility.getMsgCount())) {
                cmt = unreadBean.getCmt();
            } else {
                cmt = actualFetchedSize;
            }

            if (mention > 0)
                stringBuilder.append("、");
            String txt = String.format(GlobalContext.getInstance().getString(R.string.new_comments), String.valueOf(cmt));
            stringBuilder.append(txt);
        }
        return stringBuilder.toString();
    }

    public static void show(Notification notification, int id) {
        NotificationManager notificationManager = (NotificationManager) GlobalContext.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    public static void cancel(int id) {
        NotificationManager notificationManager = (NotificationManager) GlobalContext.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

}
