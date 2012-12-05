package org.qii.weiciyuan.othercomponent.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.*;
import org.qii.weiciyuan.support.utils.NotificationUtility;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-12-5
 */
public class JBInboxNotification {

    private Context context;

    private AccountBean accountBean;

    private CommentListBean comment;
    private MessageListBean repost;
    private CommentListBean mentionCommentsResult;

    private UnreadBean unreadBean;

    public JBInboxNotification(Context context,
                               AccountBean accountBean,
                               CommentListBean comment,
                               MessageListBean repost,
                               CommentListBean mentionCommentsResult, UnreadBean unreadBean) {
        this.context = context;
        this.accountBean = accountBean;
        this.comment = comment;
        this.repost = repost;
        this.mentionCommentsResult = mentionCommentsResult;
        this.unreadBean = unreadBean;
    }


    private PendingIntent getPendingIntent() {
        Intent i = new Intent(context, MainTimeLineActivity.class);
        i.putExtra("account", accountBean);
        i.putExtra("comment", comment);
        i.putExtra("repost", repost);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Long.valueOf(accountBean.getUid()).intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public Notification get() {

        Notification.Builder builder = new Notification.Builder(context)
                .setTicker(NotificationUtility.getTicker(unreadBean))
                .setContentText(accountBean.getUsernick())
                .setSmallIcon(R.drawable.notification)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent())
                .setOnlyAlertOnce(true);

        builder.setContentTitle(NotificationUtility.getTicker(unreadBean));

        if (NotificationUtility.getCount(unreadBean) > 1) {
            builder.setNumber(NotificationUtility.getCount(unreadBean));
        }

        Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
        inboxStyle.setBigContentTitle(NotificationUtility.getTicker(unreadBean));
        if (comment != null) {
            for (CommentBean c : comment.getItemList()) {
                inboxStyle.addLine(c.getUser().getScreen_name() + ":" + c.getText());
            }
        }

        if (repost != null) {
            for (MessageBean m : repost.getItemList()) {
                inboxStyle.addLine(m.getUser().getScreen_name() + ":" + m.getText());
            }
        }

        if (mentionCommentsResult != null) {
            for (CommentBean m : mentionCommentsResult.getItemList()) {
                inboxStyle.addLine(m.getUser().getScreen_name() + ":" + m.getText());
            }
        }

        inboxStyle.setSummaryText(accountBean.getUsernick());

        builder.setStyle(inboxStyle);
        Utility.configVibrateLedRingTone(builder);
        return builder.build();
    }


}
